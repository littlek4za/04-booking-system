import { Component, EventEmitter, input, Output } from '@angular/core';
import { InvitationResponseDto } from '../dtos/invitation-response-dto';
import { NotificationService } from '@core/services/notification-service';
import { Clipboard } from '@angular/cdk/clipboard'
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { EventWithSlotCountResponseDto } from '@features/events/dtos/event-with-slot-count-response-dto';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-invitation-success-detail-wizard',
  imports: [DatePipe],
  templateUrl: './invitation-success-detail-wizard.html',
  styleUrl: './invitation-success-detail-wizard.css',
})
export class InvitationSuccessDetailWizard {

  @Output() close = new EventEmitter<void>();

  invitationResponseDto = input.required<InvitationResponseDto>();
  eventData = input.required<EventWithSlotCountResponseDto>();
  SlotInludeMode = SlotIncludeMode;

  constructor(
    private clipboard: Clipboard,
    private notificationService: NotificationService,
  ) { }

  copyLink() {
    this.clipboard.copy(`${window.location.origin}/invitation/${this.invitationResponseDto().accessToken}`);
    this.notificationService.success('Invitation access link copied to clipboard!');
  }

  copyCode() {
    this.clipboard.copy(`${this.invitationResponseDto().accessToken}`);
    this.notificationService.success('Invitation code copied to clipboard!');
  }

  closeWizard() {
    this.close.emit();
  }

  shareViaWhatsapp() {
    const invitationLink =
      `${window.location.origin}/invitation/${this.invitationResponseDto().accessToken}`;

    const messageLines: string[] = [
      `*You are invited!*`,
      `*Event:* ${this.eventData().eventName}`,
    ];

    if (this.eventData().eventDescription) {
      messageLines.push(
        `*Event Description:* ${this.eventData().eventDescription}`
      );
    }

    messageLines.push(
      `*Event Location:* ${this.eventData().eventLocationAddress}`
    );

    if (this.eventData().includePosition) {
      messageLines.push(
        `*Google Map:* https://www.google.com/maps/place/${this.eventData().latitude},${this.eventData().longitude}`
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

}
