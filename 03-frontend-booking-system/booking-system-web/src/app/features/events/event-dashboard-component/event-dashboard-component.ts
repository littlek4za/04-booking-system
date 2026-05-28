import { Component, computed, inject, signal } from '@angular/core';
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
import { EventWithSlotCountResponseDto } from '../dtos/event-with-slot-count-response-dto';
import { NotificationService } from '@core/services/notification-service';

@Component({
  standalone: true,
  selector: 'app-event-dashboard-component',
  imports: [EventEditWizard, DatePipe, RouterLink, FullCalendarView, InvitationEditWizard, InvitationDashboard],
  templateUrl: './event-dashboard-component.html',
  styleUrl: './event-dashboard-component.css',
})
export class EventDashboardComponent {

  // Service Inject
  private eventService = inject(EventService);
  private logger = inject(LoggerService);

  // component mode
  modeEventWizard!: 'CREATE' | 'UPDATE';

  // show/hide component
  showEventWizard: boolean = false;
  showCalendarView: boolean = false;
  showInvitationWizard: boolean = false;
  showInvitationDashboard: boolean = false;

  // IO for component
  updateEventId: number | null = null;
  updateEvent: EventWithSlotCountResponseDto | null = null;
  updateEventType: EventTypeModel | null = null;
  
  // class
  EventTypeModel = EventTypeModel;

  // sort and filter field
  allEvents = toSignal(this.eventService.eventList$, { initialValue: [] });
  activeSortMode = signal<'LATEST' | 'EARLIEST' | 'NAME_ASC' | 'NAME_DSC' | 'SLOT_HIGH' | 'SLOT_LOW'>('LATEST');
  activeEventTypeFilter = signal<EventTypeModel | null>(null);
  activeSlotFilter = signal<'ALL' | 'HAS_SLOT' | 'NO_SLOT'>('ALL');
  searchText = signal<string>('');

  // data
  eventList = computed(() => {
    let list = [...this.allEvents()];

    const searchTexts = this.searchText().trim().toLowerCase().split(/\s+/);
    const eventTypeFilter = this.activeEventTypeFilter();
    const slotFilter = this.activeSlotFilter();
    const sortMode = this.activeSortMode();

    // Search filter
    if (searchTexts) {
      list = list.filter(event => {
        const searchableText = [
          event.eventName,
          event.eventDescription,
          event.eventLocationAddress,
          event.eventType
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase();

        return searchTexts.every(word => searchableText.includes(word));
      })
    }

    // Event Type filter
    if (eventTypeFilter !== null) {
      list = list.filter(e => e.eventType === eventTypeFilter);
    }

    // Slot filter
    if(slotFilter === 'HAS_SLOT'){
      list = list.filter(event => event.slotCount > 0);
    }

    if(slotFilter === 'NO_SLOT'){
      list = list.filter(event => event.slotCount === 0);
    }

    // Sorting
    if (sortMode === 'LATEST') {
      list.sort((a, b) =>
        new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      );
    }

    if (sortMode === 'EARLIEST') {
      list.sort((a, b) =>
        new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime()
      );
    }

    if (sortMode === 'NAME_ASC') {
      list.sort((a, b) =>
        a.eventName.localeCompare(b.eventName)
      );
    }

    if (sortMode === 'NAME_DSC') {
      list.sort((a, b) =>
        b.eventName.localeCompare(a.eventName)
      );
    }

    if (sortMode === 'SLOT_HIGH') {
      list.sort((a, b) =>
        b.slotCount - a.slotCount
      );
    }

    if (sortMode === 'SLOT_LOW') {
      list.sort((a, b) =>
        a.slotCount - b.slotCount
      );
    }


    return list;
  })

  constructor(private notificationService: NotificationService) { }

  confirmDeleteEvent(eventId: number, eventSlotCount: number) {

    this.eventService.eventDeleteValidation(eventId).subscribe({
      next: (res) => {
        if (res.canDelete == false) {
          this.logger.debug('[EventDashboardComponent] Event delete validation: not allowed delete');
          this.notificationService.warning(
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
      }
    });
  }

  private deleteEventById(eventId: number) {
    this.logger.debug('[EventDashboardComponent] Sending eventService.deleteEventById request');
    this.eventService.deleteEventById(eventId).subscribe({
      next: () => {
        this.eventService.triggerRefresh();
      },
      error: () => {
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

  openInvitationDashboard(event: EventWithSlotCountResponseDto) {
    this.updateEvent = event;
    this.showInvitationDashboard = true;
  }

  closeInvitationDashboard() {
    this.showInvitationDashboard = false;
  }

  openCalendar(eventId: number, eventType: EventTypeModel) {
    this.updateEventId = eventId;
    this.updateEventType = eventType;
    this.showCalendarView = true;
  }

  closeCalendar() {
    this.showCalendarView = false;
  }

  openInvitationWizard(eventId: number, eventType: EventTypeModel) {
    this.updateEventId = eventId;
    this.updateEventType = eventType;
    this.showInvitationWizard = true;
  }

  closeInvitationWizard() {
    this.showInvitationWizard = false;
  }

  sortEvents(type: 'LATEST' | 'EARLIEST' | 'NAME_ASC' | 'NAME_DSC' | 'SLOT_HIGH' | 'SLOT_LOW') {
    this.activeSortMode.set(type);
  }

  filterEventsByEventType(eventType: EventTypeModel | null) {
    this.activeEventTypeFilter.set(eventType);
  }

  filterEventsBySlot(type: 'ALL' | 'HAS_SLOT' | 'NO_SLOT') {
    this.activeSlotFilter.set(type);
  }

  searchEvents(value:string) {
    this.searchText.set(value);
  }

}
