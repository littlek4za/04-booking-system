import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { AddEventWizard } from '../add-event-wizard/add-event-wizard';
import { AddSlotWizard } from '../../slots/add-slot-wizard/add-slot-wizard';
import { EventService } from '../event-service';
import { UpdateEventWizard } from '../update-event-wizard/update-event-wizard';
import { toSignal } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink } from "@angular/router";

@Component({
  standalone: true,
  selector: 'app-event-dashboard-component',
  imports: [AddEventWizard, UpdateEventWizard, DatePipe, RouterLink],
  templateUrl: './event-dashboard-component.html',
  styleUrl: './event-dashboard-component.css',
})
export class EventDashboardComponent {

  private eventService = inject(EventService);
  createEventWizard: boolean = false;
  updateEventWizard: boolean = false;
  slotWizard: boolean = false;
  // OPTIONAL TO USE async to update form
  // eventList$?: Observable<EventResponseDto[]>;
  eventList = toSignal(this.eventService.eventList$, { initialValue: [] });
  updateEventId!: number;

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

  deleteEventById(eventId: number) {
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

  openAddEventWizard() {
    this.createEventWizard = true;
  }

  closeAddEventWizard() {
    this.createEventWizard = false;
  }

  openUpdateEventWizard(eventId: number) {
    this.updateEventWizard = true;
    this.updateEventId = eventId;
  }

  closeUpdateEventWizard() {
    this.updateEventWizard = false;
  }
}
