import { Component, inject } from '@angular/core';
import { EventEditWizard } from '../event-edit-wizard/event-edit-wizard';
import { EventService } from '../event-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink } from "@angular/router";
import { FullCalendarView } from '@shared/components/full-calendar-view/full-calendar-view';
import { EventTypeModel } from '../dtos/event-type-model';
import { InvitationEditWizard } from "@features/invitations/invitation-edit-wizard/invitation-edit-wizard";
import { InvitationDashboard } from "@features/invitations/invitation-dashboard/invitation-dashboard";
import { LoggerService } from '@core/services/logger-service';

@Component({
  standalone: true,
  selector: 'app-event-dashboard-component',
  imports: [EventEditWizard, DatePipe, RouterLink, FullCalendarView, InvitationEditWizard, InvitationDashboard],
  templateUrl: './event-dashboard-component.html',
  styleUrl: './event-dashboard-component.css',
})
export class EventDashboardComponent {

  private eventService = inject(EventService);
  private logger = inject(LoggerService);

  // show component
  showEventWizard: boolean = false;
  showCalendarView: boolean = false;
  showInvitationWizard: boolean = false;
  showInvitationDashboard: boolean = false;

  modeEventWizard!: 'CREATE' | 'UPDATE';

  // field value
  updateEventId: number | null = null;
  updateEventName: string | null = null;
  eventId: number | null = null;
  eventType: EventTypeModel | null = null;

  eventList = toSignal(this.eventService.eventList$, { initialValue: [] });

  confirmDeleteEvent(eventId: number, eventSlotCount: number) {

    this.eventService.eventDeleteValidation(eventId).subscribe({
      next: (res) => {
        if (res.canDelete == false) {
          this.logger.debug('[EventDashboardComponent] Event delete validation: not allowed delete');
          alert(
            `This event cannot be deleted because there are active booking(s):\n\n` +
            `• Upcoming booking(s): ${res.upcomingBookingCount}\n` +
            `• Ongoing booking(s): ${res.ongoingBookingCount}\n\n` +
            `Please cancel or complete all bookings before deleting this event.`
          );
          return;
        }
        const message = eventSlotCount > 0
          ? `Are you sure you want to delete this event?\n${eventSlotCount} slot(s) under this event will be deleted too`
          : 'Are you sure you want to delete this event?';

        if (confirm(message)) {
          this.deleteEventById(eventId);
        }
      },
      error: () => {
        alert('An error occurred. Please try again. If the problem persists, please contact the administrator.');
      }
    });
  }

  private deleteEventById(eventId: number) {
    this.logger.debug('[EventDashboardComponent] Sending eventService.deleteEventById request');
    this.eventService.deleteEventById(eventId).subscribe({
      next: (res) => {
        this.eventService.triggerRefresh();
      },
      error: (err) => {
        alert('An error occurred. Please try again. If the problem persists, please contact the administrator.');
      }
    });
  }

  openUpdateEventWizard(eventId: number) {
    this.modeEventWizard = 'UPDATE';
    this.updateEventId = eventId;
    this.showEventWizard = true;
  }

  openCreateEventWizard() {
    this.modeEventWizard = 'CREATE';
    this.updateEventId = null;
    this.showEventWizard = true;
  }

  closeEventWizard() {
    this.showEventWizard = false;
  }

  openInvitationDashboard(eventId: number, eventName: string) {
    this.updateEventId = eventId;
    this.updateEventName = eventName;
    this.showInvitationDashboard = true;
  }

  closeInvitationDashboard() {
    this.showInvitationDashboard = false;
  }

  openCalendar(eventId: number, eventType: EventTypeModel) {
    this.eventId = eventId;
    this.eventType = eventType;
    console.log(this.eventId, this.eventType);
    this.showCalendarView = true;
  }

  closeCalendar() {
    this.showCalendarView = false;
  }

  openInvitationWizard(eventId: number, eventType: EventTypeModel) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.showInvitationWizard = true;
  }

  closeInvitationWizard() {
    this.showInvitationWizard = false;
  }

}
