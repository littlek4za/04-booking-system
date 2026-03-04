import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-invitation-dashboard',
  imports: [],
  templateUrl: './invitation-dashboard.html',
  styleUrl: './invitation-dashboard.css',
})
export class InvitationDashboard {

  @Input() eventId!: number;


}
