import { Component } from '@angular/core';
import { InvitationAccessComponent } from "@features/invitations/invitation-access-component/invitation-access-component";
import { BookingAccessComponent } from "@features/booking/booking-access-component/booking-access-component";
import { AuthService } from '@features/auth/auth-service';
import { AttendeeBookingListViewComponent } from "@features/booking/attendee-booking-list-component/attendee-booking-list-view-component";

@Component({
  selector: 'app-attendee-access-component',
  imports: [InvitationAccessComponent, BookingAccessComponent, AttendeeBookingListViewComponent],
  templateUrl: './attendee-access-component.html',
  styleUrl: './attendee-access-component.css',
})
export class AttendeeAccessComponent {

  showBookingAccessPage: boolean = false;
  showUserBookingListDashboard: boolean = false;
  showInvitationAccessPage: boolean = false;

  constructor(private authService: AuthService) { }

  get loggedInUser() {
    return this.authService.hasValidToken() && this.authService.isLoggedInUser();
  }

  viewBookingAccess() {
    this.showBookingAccessPage = true;
  }

  viewBookingList() {
    this.showUserBookingListDashboard = true;
  }

  viewInvitationAccess() {
    this.showInvitationAccessPage = true;
  }

  closeInvitationAccessPage() {
    this.showInvitationAccessPage = false;
  }

  closeBookingAccessPage() {
    this.showBookingAccessPage = false;
  }

  closeUserBookingListDashboard() {
    this.showUserBookingListDashboard = false;
  }

}
