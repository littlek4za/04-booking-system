import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@features/auth/auth-service';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotIncludeMode } from '@features/invitations/dtos/slot-include-mode';
import { InvitationService } from '@features/invitations/invitation-service';
import { BookingConfirmationWizard } from '../booking-confirmation-wizard/booking-confirmation-wizard';
import { InvitationResponseDto } from '@features/invitations/dtos/invitation-response-dto';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { NotificationService } from '@core/services/notification-service';

@Component({
  selector: 'app-booking-confirmation-dashboard',
  imports: [DatePipe, BookingConfirmationWizard],
  templateUrl: './booking-confirmation-dashboard.html',
  styleUrl: './booking-confirmation-dashboard.css',
})
export class BookingConfirmationDashboard {

  //service
  private readonly invitationService = inject(InvitationService);

  // show component
  showBookingConfirmationWizard = false;

  protected readonly SlotIncludeMode = SlotIncludeMode;
  protected readonly EventTypeModel = EventTypeModel;

  // Output to component
  slotField: SlotResponseDto | null = null;
  invitationField: InvitationResponseDto | null = null;

  // signal data
  invitation = toSignal(this.invitationService.invitationByToken$, { initialValue: null });

  // html field
  businessDays = [0, 1, 2, 3, 4, 5, 6];
  dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

  constructor(
    private route: ActivatedRoute, 
    private router: Router, 
    private authService: AuthService,
    private logger: LoggerService,
    private notificationService:NotificationService
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.paramMap.get('invitationToken');
    this.validateToken(token);
  }

  validateToken(token: string | null) {
    if (token == null) {
      this.notificationService.error("No invitation token detected, please try again. If problem persist, please contact administrator");
      this.logger.warn(`[BookingConfirmationDashboard] No invitation token detected in URL`);
      this.router.navigate(['/invitation']);
      return;
    }
    if (token) {
      this.logger.debug(`[BookingConfirmationDashboard] Invitation token detected in URL`);
      this.invitationService.validateInvitation(token).subscribe({
        next: (res) => {
          if (res.valid == false) {
            this.logger.warn(`[BookingConfirmationDashboard] Invitation invalid, reason:`, res.reason);
            this.notificationService.warning(res.reason);
            this.router.navigate(['/invitation']);
          }
          else if (res.requiredLogin == false) {
            this.logger.debug(`[BookingConfirmationDashboard] Invitation validation success`);
            this.loadInvitation(token);
          }
          else if (res.requiredLogin && !this.authService.hasUserValidToken()) {
            this.logger.debug(`[BookingConfirmationDashboard] Login required for invitation access`);
            this.notificationService.info("redirecting to login page");
            const currentUrl = this.router.url;
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: currentUrl }
            });
          }
          else if (res.requiredLogin && this.authService.hasUserValidToken()) {
            this.logger.debug(`[BookingConfirmationDashboard] Invitation validation success`);
            this.loadInvitation(token);
          }
          else {
            this.logger.warn("[BookingConfirmationDashboard] Unexpected invitation validation response:",res);
            this.notificationService.error("Unexpected error occur. Please contact administrator.");
          }
        },
        error: () => {
          this.router.navigate(['/invitation']);
        }
      })
    }
  }

  loadInvitation(token: string) {
    this.invitationService.triggerRefreshForInvitationByToken(token);
  }

  openBookingConfirmationWizard(slot: SlotResponseDto) {
    this.slotField = slot;
    this.invitationField = this.invitation();
    this.showBookingConfirmationWizard = true;
  }

  closeBookingConfirmationWizard() {
    this.showBookingConfirmationWizard = false;
  }

  getSlotBusinessDaysHoursRange(day: number, slot: SlotResponseDto) {
    return slot.businessDaysHours?.[day] ?? [];
  }

}
