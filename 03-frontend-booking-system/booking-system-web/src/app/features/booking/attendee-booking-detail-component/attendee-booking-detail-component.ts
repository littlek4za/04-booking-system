import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { Subject, takeUntil } from 'rxjs';
import { LoggerService } from '@core/services/logger-service';
import { NotificationService } from '@core/services/notification-service';
import { CommonModule, DatePipe } from '@angular/common';
import { Clipboard } from '@angular/cdk/clipboard';

@Component({
  selector: 'app-attendee-booking-detail-component',
  imports: [DatePipe, CommonModule],
  templateUrl: './attendee-booking-detail-component.html',
  styleUrl: './attendee-booking-detail-component.css',
})
export class AttendeeBookingDetailComponent implements OnInit, OnDestroy {

  attendeeBookingDto = signal<AttendeeBookingResponseDto | null>(null);

  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private bookingService: BookingService,
    private router: Router,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private clipboard:Clipboard
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
          console.log("dto", this.attendeeBookingDto());
        },
        error: () => {
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
            next: () => {
              this.notificationService.success("Cancel booking sucessful");
              this.router.navigate(['/attendeeAccess']);
            },
            error: () => {
            },
          })
      }
    }
  }

  shareViaWhatsapp() {
    const dto = this.attendeeBookingDto();

    if (!dto?.bookingToken) return;

    const bookingLink =
      `${window.location.origin}/bookingAccess/${dto.bookingToken}`;

    const message =
      `Here is my booking link:\n${bookingLink}`;

    const whatsappUrl =
      `https://wa.me/?text=${encodeURIComponent(message)}`;

    window.open(whatsappUrl, '_blank');
  }

  copyLink() {
    const dto = this.attendeeBookingDto();

    if (!dto?.bookingToken) return;

    this.clipboard.copy(`${window.location.origin}/bookingAccess/${dto.bookingToken}`);

    this.notificationService.success('Booking access link copied to clipboard!');
  }

  closeWizard() {
    this.router.navigate(['/attendeeAccess']);
  }

}
