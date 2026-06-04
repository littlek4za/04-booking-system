import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { LoggerService } from '@core/services/logger-service';
import { AttendeeBookingDetailComponent } from "../attendee-booking-detail-component/attendee-booking-detail-component";

@Component({
  selector: 'app-booking-view-page-component',
  imports: [AttendeeBookingDetailComponent],
  templateUrl: './booking-view-page-component.html',
  styleUrl: './booking-view-page-component.css',
})
export class BookingViewPageComponent implements OnInit, OnDestroy {

  attendeeBookingDto = signal<AttendeeBookingResponseDto | null>(null);

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private logger: LoggerService,
  ) { }

  ngOnInit(): void {
    const bookingToken = this.route.snapshot.paramMap.get('bookingToken');

    if (bookingToken) {
      this.logger.debug(`[BookingViewPageComponent] Booking token detected in URL`);
      this.retrieveBookingInfo(bookingToken);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  retrieveBookingInfo(bookingToken: string) {
    this.logger.debug('[BookingViewPageComponent] Sending bookingService.getBookingByBookingToken request');
    this.bookingService.getBookingByBookingToken(bookingToken)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.attendeeBookingDto.set(res);
        },
        error: () => {
        }
      });
  }

  redirectPage() {
    this.router.navigate(['/attendeeAccess']);
  }

}
