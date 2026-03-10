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

@Component({
  selector: 'app-booking-dashboard',
  imports: [DatePipe,BookingConfirmationWizard],
  templateUrl: './booking-dashboard.html',
  styleUrl: './booking-dashboard.css',
})
export class BookingDashboard {

  private invitationService = inject(InvitationService);

  // show component
  showBookingConfirmationWizard = false;

  protected readonly SlotIncludeMode = SlotIncludeMode;
  protected readonly EventTypeModel = EventTypeModel;

  slotField: SlotResponseDto | null = null;
  invitationField: InvitationResponseDto | null = null;

  invitation = toSignal(this.invitationService.invitation$, { initialValue: null });

  constructor(private route: ActivatedRoute, private router: Router, private authService: AuthService) { }

  ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token');
    this.validateToken(token);
  }

  validateToken(token: string | null) {
    if (token == null) {
      alert("No Token defined");
      this.router.navigate(['/invitation']);
    }
    if (token) {
      this.invitationService.validateInvitation(token).subscribe({
        next: (res) => {
          if (res.valid == false) {
            alert(res.reason);
          }
          if (res.requiredLogin == false) {
            this.loadInvitation(token);
          }
          if (res.requiredLogin && !this.authService.hasValidToken()) {
            alert("redirecting to login page");
            const currentUrl = this.router.url;
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: currentUrl }
            });
          }
          if (res.requiredLogin && this.authService.hasValidToken()) {
            this.loadInvitation(token);
          }
        },
        error: () => {
          alert("Token invalid");
          this.router.navigate(['/invitation']);
        }
      })
    }
  }

  loadInvitation(token: string) {
    this.invitationService.triggerRefreshForInvitation(token);
  }

  openBookingConfirmationWizard(slot: SlotResponseDto) {
    this.slotField = slot;
    this.invitationField = this.invitation();
    this.showBookingConfirmationWizard = true;
  }

  closeBookingConfirmationWizard() {
    this.showBookingConfirmationWizard = false;
  }

}
