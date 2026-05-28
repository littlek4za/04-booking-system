import { Component, computed, EventEmitter, inject, Input, OnChanges, OnDestroy, Output, signal, SimpleChanges } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { InvitationService } from '../invitation-service';
import { Clipboard } from '@angular/cdk/clipboard'
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { InvitationResponseDto } from '../dtos/invitation-response-dto';
import { Subject, takeUntil } from 'rxjs';
import { LoggerService } from '@core/services/logger-service';
import { NotificationService } from '@core/services/notification-service';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { EventService } from '@features/events/event-service';
import { EventWithSlotCountResponseDto } from '@features/events/dtos/event-with-slot-count-response-dto';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-invitation-dashboard',
  imports: [CommonModule],
  templateUrl: './invitation-dashboard.html',
  styleUrl: './invitation-dashboard.css',
})
export class InvitationDashboard implements OnChanges, OnDestroy {

  private invitationService = inject(InvitationService);

  // IO
  @Input() slotId!: number;
  @Input() event!: EventWithSlotCountResponseDto;

  @Output() close = new EventEmitter<void>();

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
  protected readonly EventTypeModel = EventTypeModel;

  // for destroy usage
  private destroy$ = new Subject<void>();

  constructor(
    private clipboard: Clipboard,
    private logger: LoggerService,
    private notificationService: NotificationService
  ) { }

  ngOnChanges(changes: SimpleChanges): void {

    const eventChanged = changes['event'];
    const slotIdChanged = changes['slotId'];

    if (eventChanged || slotIdChanged) {
      this.logger.debug(`[InvitationDashboard] Changes detected for input "eventId" or "slotId"`);
      if (this.slotId) {
        this.logger.debug(`[InvitationDashboard] Sending invitationService.getInvitationsByEventIdAndSlotId request`);
        this.invitationService.getInvitationsByEventIdAndSlotId(this.event.id, this.slotId)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (res) => {
              this.invitationListByEventIdAndSlotId.set(res);
            },
            error: () => {
            }
          })
      } else {
        this.invitationService.triggerRefreshForInvitationListByEventId(this.event.id);
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  copyLink(accessToken: string) {
    this.clipboard.copy(`${window.location.origin}/invitation/${accessToken}`);
    this.notificationService.success('Invitation link copied to clipboard!');
  }

  copyCode(accessToken: string) {
    this.clipboard.copy(`${accessToken}`);
    this.notificationService.success('Invitation code copied to clipboard!');
  }

  shareViaWhatsapp(accessToken: string) {
    const invitationLink =
      `${window.location.origin}/invitation/${accessToken}`;

    const messageLines: string[] = [
      `*You are invited!*`,
      `*Event:* ${this.event.eventName}`,
    ];

    if (this.event.eventDescription) {
      messageLines.push(
        `*Event Description:* ${this.event.eventDescription}`
      );
    }

    messageLines.push(
      `*Event Location:* ${this.event.eventLocationAddress}`
    );


    if (this.event.includePosition) {
      messageLines.push(
        `*Google Map:* https://www.google.com/maps/place/${this.event.latitude},${this.event.longitude}`
      );
    }

    messageLines.push(
      `*Join Link:* ${invitationLink}`,
      `\nWe look forward to having you!`
    );


    const message = messageLines.join('\n');

    const whatsappUrl =
      `https://wa.me/?text=${encodeURIComponent(message)}`;

    window.open(whatsappUrl, '_blank');
  }

  deleteInvitation(invitationId: number) {
    this.logger.debug(`[InvitationDashboard] Sending invitationService.deleteInvitation request`);
    this.invitationService.deleteInvitation(this.event.id, invitationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.success('Invitation delete succeed');
          this.invitationService.triggerRefreshForInvitationListByEventId(this.event.id);
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
