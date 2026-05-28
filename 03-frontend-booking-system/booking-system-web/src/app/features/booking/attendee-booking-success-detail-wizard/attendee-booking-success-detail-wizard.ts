import { Component, EventEmitter, input, Output } from '@angular/core';
import { LoggerService } from '@core/services/logger-service';
import { Clipboard } from '@angular/cdk/clipboard'
import { NotificationService } from '@core/services/notification-service';
import { BookingService } from '../booking-service';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '@features/auth/auth-service';

@Component({
  selector: 'app-attendee-booking-success-detail-wizard',
  imports: [DatePipe, CommonModule],
  templateUrl: './attendee-booking-success-detail-wizard.html',
  styleUrl: './attendee-booking-success-detail-wizard.css',
})
export class AttendeeBookingSuccessDetailWizard {

  @Output() close = new EventEmitter<void>();

  attendeeBookingResponseDto = input.required<AttendeeBookingResponseDto>();

  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  constructor(
    private clipboard: Clipboard,
    private router: Router,
    private notificationService: NotificationService,
    private authService: AuthService
  ) { }

  get hasUserValidToken() {
    return this.authService.hasUserValidToken();
  }

  copyLink() {
    this.clipboard.copy(`${window.location.origin}/bookingAccess/${this.attendeeBookingResponseDto().bookingToken}`);
    this.notificationService.success('Booking access link copied to clipboard!');
  }

  closeWizard() {
    this.close.emit();
    this.router.navigate(['/invitation']);
  }

  shareViaWhatsapp() {
    const bookingLink =
      `${window.location.origin}/bookingAccess/${this.attendeeBookingResponseDto().bookingToken}`;

    const message =
      `Here is my booking link:\n${bookingLink}`;

    const whatsappUrl =
      `https://wa.me/?text=${encodeURIComponent(message)}`;

    window.open(whatsappUrl, '_blank');
  }

}
