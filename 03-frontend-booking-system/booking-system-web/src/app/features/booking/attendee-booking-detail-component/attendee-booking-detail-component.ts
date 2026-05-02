import { Component, Input, OnDestroy, OnInit, signal } from '@angular/core';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { ActivatedRoute } from '@angular/router';
import { BookingService } from '../booking-service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-attendee-booking-detail-component',
  imports: [],
  templateUrl: './attendee-booking-detail-component.html',
  styleUrl: './attendee-booking-detail-component.css',
})
export class AttendeeBookingDetailComponent implements OnInit, OnDestroy {

  attendeeBookingDto = signal<AttendeeBookingResponseDto | null>(null);

  private destroy$ = new Subject<void>();

  constructor(private route: ActivatedRoute,
    private bookingService: BookingService
  ) { }

  ngOnInit(): void {
    const bookingToken = this.route.snapshot.paramMap.get('bookingToken');

    if (bookingToken) {
      this.retrieveBookingInfo(bookingToken);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  retrieveBookingInfo(bookingToken: string) {
    this.bookingService.getBookingByBookingToken(bookingToken)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.attendeeBookingDto.set(res);
          console.log("GET Booking success", this.attendeeBookingDto());
        },
        error: (err) => {
          alert("GET Booking failed.");
        }
      });
  }

}
