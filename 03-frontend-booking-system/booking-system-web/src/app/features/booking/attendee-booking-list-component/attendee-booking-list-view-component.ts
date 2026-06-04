import { ChangeDetectorRef, Component, ElementRef, EventEmitter, OnDestroy, OnInit, Output, signal, Signal, ViewChild } from '@angular/core';
import { BookingService } from '../booking-service';
import { Subject, takeUntil } from 'rxjs';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { Router } from '@angular/router';
import { AttendeeBookingDetailComponent } from '../attendee-booking-detail-component/attendee-booking-detail-component';

@Component({
  selector: 'app-attendee-booking-list-view-component',
  imports: [DatePipe, NgClass, CommonModule, AttendeeBookingDetailComponent],
  templateUrl: './attendee-booking-list-view-component.html',
  styleUrl: './attendee-booking-list-view-component.css',
  standalone: true
})

export class AttendeeBookingListViewComponent implements OnInit, OnDestroy {

  @ViewChild('listScrollContainer')
  listScrollContainer?: ElementRef<HTMLDivElement>;


  @Output() close = new EventEmitter<void>();

  currentView = signal<'list' | 'details'>('list');
  private listScrollTop = 0;


  bookingList = signal<AttendeeBookingResponseDto[]>([]);
  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  selectedBooking = signal<AttendeeBookingResponseDto | null>(null);

  private destroy$ = new Subject<void>();

  constructor(
    private bookingService: BookingService,
    private cdr: ChangeDetectorRef,
    private logger: LoggerService,
  ) { }

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
          },
        })
    }
  }

  closeWizard() {
    this.close.emit();
  }

  viewBookingDetails(booking: AttendeeBookingResponseDto) {
    this.listScrollTop = this.listScrollContainer?.nativeElement.scrollTop ?? 0;

    this.selectedBooking.set(booking);
    this.currentView.set('details');

    setTimeout(() => {
      this.listScrollContainer?.nativeElement.scrollTo({
        top: 0,
        behavior: 'auto'
      });
    });

  }

  backToList() {
    this.currentView.set('list');
    setTimeout(() => {
      this.listScrollContainer?.nativeElement.scrollTo({
        top: this.listScrollTop,
        behavior: 'auto'
      });
    });
  }

}
