import { DatePipe } from '@angular/common';
import { Component, computed, effect, inject, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@features/auth/auth-service';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotIncludeMode } from '@features/invitations/dtos/slot-include-mode';
import { InvitationService } from '@features/invitations/invitation-service';
import { BookingConfirmationWizard } from '../booking-confirmation-wizard/booking-confirmation-wizard';
import { InvitationResponseDto } from '@features/invitations/dtos/invitation-response-dto';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';
import { SlotService } from '@features/slots/slot-service';

@Component({
  selector: 'app-booking-dashboard',
  imports: [DatePipe, BookingConfirmationWizard],
  templateUrl: './booking-dashboard.html',
  styleUrl: './booking-dashboard.css',
})
export class BookingDashboard {

  //service
  private invitationService = inject(InvitationService);
  private slotService = inject(SlotService);

  // show component
  showBookingConfirmationWizard = false;

  protected readonly SlotIncludeMode = SlotIncludeMode;
  protected readonly EventTypeModel = EventTypeModel;

  // Output to component
  slotField: SlotResponseDto | null = null;
  invitationField: InvitationResponseDto | null = null;

  // signal data
  invitation = toSignal(this.invitationService.invitation$, { initialValue: null });
  slotList = toSignal(this.slotService.slotList$, { initialValue: [] })

  // html field
  businessDays = [0, 1, 2, 3, 4, 5, 6];
  dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  slotListToDisplay = computed(() => {
    const invitation = this.invitation();
    const allSlots = this.slotList();

    if (invitation?.slotIncludeMode === SlotIncludeMode.ALL_AND_FUTURE) {
      return allSlots;
    } else {
      return invitation?.slotList;
    }
  });

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
            this.router.navigate(['/invitation']);
          }
          else if (res.requiredLogin == false) {
            this.loadInvitation(token);
          }
          else if (res.requiredLogin && !this.authService.hasValidToken()) {
            alert("redirecting to login page");
            const currentUrl = this.router.url;
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: currentUrl }
            });
          }
          else if (res.requiredLogin && this.authService.hasValidToken()) {
            this.loadInvitation(token);
          }
          else {
            console.warn("Unexpected invitation validation response:", res);
            alert("Unexpected invitation status. Please contact administrator.");
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
  
  constructor(private route: ActivatedRoute, private router: Router, private authService: AuthService) {
    effect(() => {
      const inv = this.invitation();

      if (inv?.slotIncludeMode === SlotIncludeMode.ALL_AND_FUTURE && inv?.event.id) {
        untracked(() => { //safety net to prevent infinite
          this.slotService.triggerRefresh(inv.event.id);
          // How infinite happen
          // triggerRefresh(id: string) {
          //   // 1. SIGNAL READ: Because the effect is running, 
          //   // Angular records 'slotList' as a dependency of the EFFECT.
          //   if (this.slotList().length > 0) return;

          //   this.http.get(...).subscribe(data => {
          //     // 2. SIGNAL UPDATE: When this finishes, it triggers the effect 
          //     // to run again because of the 'Hidden Read' above.
          //     this.slotList.set(data);
          //   });
          // }
        })
      }
    })
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
