import { ChangeDetectorRef, Component, EventEmitter, OnDestroy, OnInit, Output, signal, Signal } from '@angular/core';
import { BookingService } from '../booking-service';
import { Subject, takeUntil } from 'rxjs';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { LoggerService } from '@core/services/logger-service';

@Component({
  selector: 'app-attendee-booking-list-view-component',
  imports: [DatePipe, NgClass, CommonModule],
  templateUrl: './attendee-booking-list-view-component.html',
  styleUrl: './attendee-booking-list-view-component.css',
  standalone: true
})
export class AttendeeBookingListViewComponent implements OnInit, OnDestroy {

  @Output() close = new EventEmitter<void>();

  bookingList = signal<AttendeeBookingResponseDto[]>([]);
  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  private destroy$ = new Subject<void>();

  constructor(private bookingService: BookingService, private cdr: ChangeDetectorRef, private logger: LoggerService) { }

  ngOnInit(): void {
    this.initBookingList();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initBookingList() {
    this.logger.debug('[AttendeeBookingListViewComponent] Sending bookingService.getAttendeeBookings request');
    this.bookingService.getAttendeeBookings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.bookingList.set(res);
          this.cdr.detectChanges();
        },
        error: () => {
          alert('Get booking list failed');
        }
      })
  }

  comfirmDeleteUserBooking(bookingId: number) {
    if (confirm("Confirm Delete Booking?")) {
      this.logger.debug('[AttendeeBookingListViewComponent] Sending bookingService.softDeleteBookingAsAttendee request');
      this.bookingService.softDeleteBookingAsAttendee(bookingId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.initBookingList();
          },
          error: () => {
            alert("Delete unsuccessful, please contact admin");
          },
        })
    }
  }

  closeWizard() {
    this.close.emit();
  }

}
