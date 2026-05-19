import { Component, computed, EventEmitter, inject, Input, OnChanges, OnDestroy, Output, signal, SimpleChanges } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { InvitationService } from '../invitation-service';
import { Clipboard } from '@angular/cdk/clipboard'
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { InvitationResponseDto } from '../dtos/invitation-response-dto';
import { Subject, takeUntil } from 'rxjs';
import { LoggerService } from '@core/services/logger-service';

@Component({
  selector: 'app-invitation-dashboard',
  imports: [],
  templateUrl: './invitation-dashboard.html',
  styleUrl: './invitation-dashboard.css',
})
export class InvitationDashboard implements OnChanges, OnDestroy {

  private invitationService = inject(InvitationService);

  // IO
  @Input() eventId!: number;
  @Input() slotId!: number;
  @Input() eventName!: string | null;
  @Output() close = new EventEmitter<void>();
  invitationUrl = `${window.location.origin}/invitation`

  // signal from service
  invitationListByEventId = toSignal(this.invitationService.invitationListByEventId$, { initialValue: [] as InvitationResponseDto[] });

  invitationListByEventIdAndSlotId = signal<InvitationResponseDto[]>([] as InvitationResponseDto[]);

  invitationList = computed(() => {
    const invitationListByEventId = this.invitationListByEventId();
    const invitationListByEventIdAndSlotId = this.invitationListByEventIdAndSlotId();

    if (invitationListByEventIdAndSlotId.length >= 1) {
      return invitationListByEventIdAndSlotId;
    } else {
      return invitationListByEventId;
    }
  });

  // html field
  protected readonly SlotIncludeMode = SlotIncludeMode;

  // for destroy usage
  private destroy$ = new Subject<void>();

  constructor(private clipboard: Clipboard, private logger: LoggerService) { }

  ngOnChanges(changes: SimpleChanges): void {

    const eventChanged = changes['eventId'];
    const slotChanged = changes['slotId'];

    if (eventChanged || slotChanged) {
      this.logger.debug(`[InvitationDashboard] Changes detected for input "eventId" and "slotId"`);
      if (this.slotId) {
        this.logger.debug(`[InvitationDashboard] Sending invitationService.getInvitationsByEventIdAndSlotId request`);
        this.invitationService.getInvitationsByEventIdAndSlotId(this.eventId, this.slotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (res) => {
            this.invitationListByEventIdAndSlotId.set(res);
          },
          error: () => {
          }
        })
      } else {
        this.invitationService.triggerRefreshForInvitationListByEventId(this.eventId);
      }

    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  shareInvitation(accessToken: string) {
    this.clipboard.copy(`${this.invitationUrl}/${accessToken}`);
    alert('Invitation link copied to clipboard!');
  }

  deleteInvitation(invitationId: number) {
    this.logger.debug(`[InvitationDashboard] Sending invitationService.deleteInvitation request`);
    this.invitationService.deleteInvitation(this.eventId, invitationId)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: () => {
        alert('Invitation delete succeed');
        this.invitationService.triggerRefreshForInvitationListByEventId(this.eventId);
      },
      error: () => {
      }
    });
  }

  getSlotNames(invitation: InvitationResponseDto): string {
    // Check if slotList exists
    if (!invitation.slotList) return '';
    return invitation.slotList.map(slot => slot.slotName).join(', ');
  }

  closeDashboard() {
    this.close.emit();
  }
}
