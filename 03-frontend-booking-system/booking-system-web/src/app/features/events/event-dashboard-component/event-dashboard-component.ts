import { Component, inject} from '@angular/core';
import { EventEditWizard } from '../event-edit-wizard/event-edit-wizard';
import { EventService } from '../event-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink } from "@angular/router";

@Component({
  standalone: true,
  selector: 'app-event-dashboard-component',
  imports: [EventEditWizard, DatePipe, RouterLink],
  templateUrl: './event-dashboard-component.html',
  styleUrl: './event-dashboard-component.css',
})
export class EventDashboardComponent {

  private eventService = inject(EventService);
  openEventWizard: boolean = false;
  modeEventWizard!: 'CREATE' | 'UPDATE';
  updateEventId: number|null = null;

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
    this.openEventWizard = true;
  }

  openCreateEventWizard(){
    this.modeEventWizard = 'CREATE';
    this.updateEventId = null;
    this.openEventWizard = true;
  }
  closeEventWizard() {
    this.openEventWizard = false;
  }

}
