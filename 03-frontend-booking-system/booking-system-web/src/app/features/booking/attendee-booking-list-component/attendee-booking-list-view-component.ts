import { ChangeDetectorRef, Component, EventEmitter, OnDestroy, OnInit, Output, signal, Signal } from '@angular/core';
import { BookingService } from '../booking-service';
import { Subject, takeUntil } from 'rxjs';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';

@Component({
  selector: 'app-attendee-booking-list-view-component',
  imports: [DatePipe, NgClass, CommonModule],
  templateUrl: './attendee-booking-list-view-component.html',
  styleUrl: './attendee-booking-list-view-component.css',
  standalone: true
})
export class AttendeeBookingListViewComponent implements OnInit, OnDestroy{

  @Output() close = new EventEmitter<void>();

  bookingList= signal<AttendeeBookingResponseDto[]>([]);
  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  private destroy$ = new Subject<void>();

  constructor(private bookingService:BookingService, private cdr:ChangeDetectorRef){}

  ngOnInit(): void {
    this.initBookingList();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initBookingList() {
    this.bookingService.getUserBookings()
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (res) => {
        this.bookingList.set(res);
        console.log('GET BookingList successfull',res);
        this.cdr.detectChanges();
      },
      error: () => {
        console.log('GET BookingList failed');
      }
    })
  }

  comfirmDeleteUserBooking(bookingId: number) {
    this.bookingService.softDeleteBookingAsUserAttendee(bookingId).subscribe({
      next:(res)=> {
        console.log("Delete booking succesful");
        this.initBookingList();
      },
      error:(err)=> {
        console.log("Delete unsucessful");
        alert("Delete unsucessful, please contact admin");
      },
    })
  }

}
