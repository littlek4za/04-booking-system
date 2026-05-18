import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { Subject, takeUntil } from 'rxjs';
import { LoggerService } from '@core/services/logger-service';

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
    private bookingService: BookingService,
    private router: Router,
    private logger: LoggerService
  ) { }

  ngOnInit(): void {
    const bookingToken = this.route.snapshot.paramMap.get('bookingToken');

    if (bookingToken) {
      this.logger.debug(`[AttendeeBookingDetailComponent] Booking token detected in URL`);
      this.retrieveBookingInfo(bookingToken);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  retrieveBookingInfo(bookingToken: string) {
    this.logger.debug('[AttendeeBookingDetailComponent] Sending bookingService.getBookingByBookingToken request');
    this.bookingService.getBookingByBookingToken(bookingToken)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.attendeeBookingDto.set(res);
        },
        error: () => {
          alert("Retrieve booking info failed");
        }
      });
  }

  deleteBooking() {
    if (confirm("Confirm Delete Booking?")) {
      const booking = this.attendeeBookingDto();

      if (booking) {
        this.logger.debug('[AttendeeBookingDetailComponent] Sending bookingService.softDeleteBookingAsAttendee request');
        this.bookingService.softDeleteBookingAsAttendee(booking.bookingId)
          .subscribe({
            next: (res) => {
              alert("Cancel booking sucessfull");
              this.router.navigate(['/attendeeAccess']);
            },
            error: (err) => {
              alert("Cancel unsuccessful, please contact admin");
            },
          })
      }
    }
  }

}
