import { Component, signal } from '@angular/core';
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
  showUserAttendeeBookingListDashboard: boolean = false;
  showInvitationAccessPage: boolean = false;

  modalView = signal<
    | 'NONE'
    | 'INVITATION'
    | 'BOOKING'
    | 'BOOKING_LIST'
  >('NONE');

  constructor(private authService: AuthService) { }

  get loggedInUser() {
    return this.authService.hasUserValidToken();
  }

  viewBookingAccess() {
    this.modalView.set('BOOKING');
  }

  viewUserAttendeeBookingList() {
    this.modalView.set('BOOKING_LIST');
  }

  viewInvitationAccess() {
     this.modalView.set('INVITATION');
  }

  closeModal() {
    this.modalView.set('NONE');
  }

}
