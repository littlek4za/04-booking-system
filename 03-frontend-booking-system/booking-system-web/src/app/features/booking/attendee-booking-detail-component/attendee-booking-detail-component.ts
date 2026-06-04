import { Component, EventEmitter, Input, Output} from '@angular/core';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { Router } from '@angular/router';
import { BookingService } from '../booking-service';
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
export class AttendeeBookingDetailComponent {

  @Input() attendeeBookingDto: AttendeeBookingResponseDto | null = null;

  @Output() close = new EventEmitter<void>();

  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  constructor(
    private bookingService: BookingService,
    private router: Router,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private clipboard:Clipboard
  ) { }

  deleteBooking() {
    if (confirm("Confirm Delete Booking?")) {
      const booking = this.attendeeBookingDto;

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
    const dto = this.attendeeBookingDto;

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
    const dto = this.attendeeBookingDto;

    if (!dto?.bookingToken) return;

    this.clipboard.copy(`${window.location.origin}/bookingAccess/${dto.bookingToken}`);

    this.notificationService.success('Booking access link copied to clipboard!');
  }

  copyCode() {
    const dto = this.attendeeBookingDto;

    if (!dto?.bookingToken) return;
    this.clipboard.copy(`${dto.bookingToken}`);

    this.notificationService.success('Booking code copied to clipboard!')
  }

  closeWizard() {
    this.close.emit();
  }

}
