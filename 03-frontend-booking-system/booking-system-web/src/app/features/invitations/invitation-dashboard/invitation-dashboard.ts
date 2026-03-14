import { Component, EventEmitter, inject, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { InvitationService } from '../invitation-service';
import {Clipboard} from '@angular/cdk/clipboard'
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { InvitationResponseDto } from '../dtos/invitation-response-dto';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-invitation-dashboard',
  imports: [],
  templateUrl: './invitation-dashboard.html',
  styleUrl: './invitation-dashboard.css',
})
export class InvitationDashboard implements OnChanges {

  private invitationService = inject(InvitationService);

  // IO
  @Input() eventId!: number;
  @Output() close = new EventEmitter<void>();
  invitationUrl = `http://localhost:4300/invitation`

  // signal from service
  invitationList = toSignal(this.invitationService.invitationList$, { initialValue: [] });

  // html field
  protected readonly SlotIncludeMode = SlotIncludeMode;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['eventId']) {
      this.invitationService.triggerRefreshForInvitationList(this.eventId);
    }
  }

  constructor(private clipboard:Clipboard){}
  
  shareInvitation(accessToken: string) {
    this.clipboard.copy(`${this.invitationUrl}/${accessToken}`);
    alert('Invitation link copied to clipboard!');
  }

  deleteInvitation(invitationId: number) {
    this.invitationService.deleteInvitation(this.eventId,invitationId).subscribe({
      next: (res) => {
        console.log('Invitation delete succeed');
        alert('Invitation delete succeed');
        this.invitationService.triggerRefreshForInvitationList(this.eventId);
      },
      error: (err) => {
        console.log('Invitation delete failed');
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
