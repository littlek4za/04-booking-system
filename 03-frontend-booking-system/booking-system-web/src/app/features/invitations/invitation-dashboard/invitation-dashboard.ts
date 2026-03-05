import { Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { InvitationService } from '../invitation-service';
import {Clipboard} from '@angular/cdk/clipboard'

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
  invitationUrl = `http://localhost:4300/invite`

  // signal from service
  invitationList = toSignal(this.invitationService.invitation$, { initialValue: [] });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['eventId']) {
      this.invitationService.triggerRefresh(this.eventId);
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
        this.invitationService.triggerRefresh(this.eventId);
      },
      error: (err) => {
        console.log('Invitation delete failed');
      }
    });
  }

  closeDashboard() {
    this.close.emit();
  }
}
