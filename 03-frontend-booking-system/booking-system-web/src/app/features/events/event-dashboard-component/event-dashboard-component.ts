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

@Component({
  standalone: true,
  selector: 'app-event-dashboard-component',
  imports: [EventEditWizard, DatePipe, RouterLink, FullCalendarView, InvitationEditWizard, InvitationDashboard],
  templateUrl: './event-dashboard-component.html',
  styleUrl: './event-dashboard-component.css',
})
export class EventDashboardComponent {

  private eventService = inject(EventService);

  // show component
  showEventWizard: boolean = false;
  showCalendarView: boolean = false;
  showInvitationWizard: boolean = false;
  showInvitationDashboard: boolean = false;

  modeEventWizard!: 'CREATE' | 'UPDATE';
  
  // field value
  updateEventId: number | null = null;
  eventId: number | null = null;
  eventType: EventTypeModel | null = null;

  // OPTIONAL TO USE async to update form
  // eventList$?: Observable<EventResponseDto[]>;
  eventList = toSignal(this.eventService.eventList$, { initialValue: [] });


  // this.eventList$ = this.eventService.getEventsForUser().pipe(
  //   tap(res => console.log('GET Event List succeed', this.eventList$)),
  //   catchError(err => {
  //     console.log('GET Event List failed', err);
  //     extractFieldErrorMessage(err);
  //     return of([]);
  //   }));

  // browse page utility

  confirmDeleteEvent(eventId: number, eventSlotCount: number) {
    const message = eventSlotCount > 0
      ? `Are you sure you want to delete this event?\n${eventSlotCount} slot(s) under this event will be deleted too`
      : 'Are you sure you want to delete this event?';

    if (confirm(message)) {
      this.deleteEventById(eventId);
    }
    // if (eventSlotCount > 0) {
    //   if (confirm(`Are you sure you want to delete this event? 
    //         \n ${eventSlotCount} number of slot under this event will be delted too`)) {
    //     this.deleteEventById(eventId);
    //   }
    // } else {
    //   if (confirm(`Are you sure you want to delete this event?`)) {
    //     this.deleteEventById(eventId);
    //   }
    // }
  }

  private deleteEventById(eventId: number) {
    this.eventService.deleteEventById(eventId).subscribe({
      next: (res) => {
        console.log('Delete Event Succesfully');
        this.eventService.triggerRefresh();
      },
      error: (err) => {
        console.error('Delete Event Failed');
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

  openInvitationDashboard(eventId: number){
    this.updateEventId = eventId;
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

  openInvitationWizard(eventId: number, eventType: EventTypeModel){
    this.eventId = eventId;
    this.eventType = eventType;
    this.showInvitationWizard = true;
  }

  closeInvitationWizard() {
    this.showInvitationWizard = false;
  }

}
