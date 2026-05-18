import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SlotService } from '../slot-service';
import { SlotEditWizard } from '../slot-edit-wizard/slot-edit-wizard';
import { CommonModule, DatePipe } from '@angular/common';
import { EventService } from '@features/events/event-service';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { FullCalendarView } from '@shared/components/full-calendar-view/full-calendar-view';
import { BookingManagerDashboard } from "@features/booking/booking-manager-dashboard/booking-manager-dashboard";
import { InvitationEditWizard } from '@features/invitations/invitation-edit-wizard/invitation-edit-wizard';
import { Subject, takeUntil } from 'rxjs';
import { InvitationDashboard } from '@features/invitations/invitation-dashboard/invitation-dashboard';
import { LoggerService } from '@core/services/logger-service';

@Component({
  standalone: true,
  selector: 'app-slot-dashboard-component',
  imports: [CommonModule, RouterLink, SlotEditWizard, DatePipe, FullCalendarView, BookingManagerDashboard, InvitationEditWizard, InvitationDashboard],
  templateUrl: './slot-dashboard-component.html',
  styleUrl: './slot-dashboard-component.css',
})
export class SlotDashboardComponent implements OnInit, OnDestroy {

  private slotService = inject(SlotService);

  updateSlotWizard: boolean = false;
  slotList = toSignal(this.slotService.slotListByEventId$, { initialValue: [] });
  eventId!: number;
  eventType!: EventTypeModel;
  slotId: number | null = null;
  modeSlotWizard!: 'CREATE' | 'UPDATE';
  openCalendarView: boolean = false;
  protected readonly EventType = EventTypeModel;

  // show or hide component
  showSlotWizard: boolean = false;
  showBookingManagerDashboard: boolean = false;
  showInvitationWizard: boolean = false;
  showInvitationDashboard: boolean = false;

  // IO for component
  slotName: string | null = null;
  eventName: string | null = null;

  // for destroy usage
  private destroy$ = new Subject<void>();


  constructor(private route: ActivatedRoute, private eventService: EventService, private logger: LoggerService) { }

  ngOnInit(): void {
    this.refreshSlotListWithEventId();
    this.subscribeEventType();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private refreshSlotListWithEventId() {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        paramMap => {
          this.eventId = +paramMap.get('id')!;
          if (this.eventId) {
            this.logger.debug(`[SlotDashboardComponent] ParmMap eventId detected`);
            this.logger.debug(`[SlotDashboardComponent] Sending slotService.triggerRefreshForSlotListByEventId request`);
            this.slotService.triggerRefreshForSlotListByEventId(this.eventId);
          }
        }
      );
  }

  private subscribeEventType() {
    this.logger.debug(`[SlotDashboardComponent] Sending eventService.getEventById request`);
    this.eventService.getEventById(this.eventId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.eventType = res.eventType;
          this.eventName = res.eventName;
        },
        error: () => {
          alert('Fail to load event info. Please try again. If the problem persists, please contact the administrator.');
        }
      });
  }

  confirmDeleteSlot(slotId: number) {
    this.logger.debug(`[SlotDashboardComponent] Sending slotService.slotDeleteValidation request`);
    this.slotService.slotDeleteValidation(this.eventId, slotId).subscribe({
      next: (res) => {
        if (res.canDelete == false) {
          alert(
            `This slot cannot be deleted because there are active booking(s):\n\n` +
            `• Upcoming booking(s): ${res.upcomingBookingCount}\n` +
            `• Ongoing booking(s): ${res.ongoingBookingCount}\n\n` +
            `Please cancel or complete all bookings before deleting this slot.`
          );
          return;
        }
        if (confirm("Are you sure you want to delete this Slot?")) {
          this.deleteSlotById(slotId);
        }
      },
      error: () => {
        alert('An error occurred. Please try again. If the problem persists, please contact the administrator.');
      }   
    });

  }

  private deleteSlotById(slotId: number) {
    this.logger.debug(`[SlotDashboardComponent] Sending slotService.deleteSlotByIdAndEvent request`);
    this.slotService.deleteSlotByIdAndEvent(this.eventId, slotId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.logger.debug(`[SlotDashboardComponent] Sending slotService.triggerRefreshForSlotListByEventId request`);
          this.slotService.triggerRefreshForSlotListByEventId(this.eventId);
        },
        error: () => {
          alert('Fail to delete slot. Please try again. If the problem persists, please contact the administrator.');
        }
      });
  }

  //Slot Wizard

  formatDuration(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;

    const hourText = h === 1 ? 'hour' : 'hours';
    const minuteText = m === 1 ? 'minute' : 'minutes';

    if (h > 0 && m > 0) return `${h} ${hourText} ${m} ${minuteText}`;
    if (h > 0) return `${h} ${hourText}`;
    return `${m} ${minuteText}`;
  }

  openCreateSlotWizard() {
    this.modeSlotWizard = 'CREATE';
    this.slotId = null;
    this.showSlotWizard = true;
  }

  openUpdateSlotWizard(slotId: number) {
    this.modeSlotWizard = 'UPDATE';
    this.slotId = slotId;
    this.showSlotWizard = true;
  }

  closeSlotWizard() {
    this.showSlotWizard = false;
  }

  openCalendar(slotId: number) {
    this.slotId = slotId;
    console.log('Opening Calendar View', this.slotId);
    this.openCalendarView = true;
  }

  closeCalendar() {
    this.openCalendarView = false;
  }

  openBookingManagerDashboard(slotId: number, slotName: string) {
    this.slotName = slotName;
    this.slotId = slotId;
    this.showBookingManagerDashboard = true;
  }

  closeBookingManagerDashboard() {
    this.showBookingManagerDashboard = false;
  }

  closeInvitationWizard() {
    this.showInvitationWizard = false;
  }

  openInvitationWizard(slotId: number) {
    this.slotId = slotId;

    this.showInvitationWizard = true;
  }

  openInvitationDashboard(slotId: number) {
    this.slotId = slotId;
    this.showInvitationDashboard = true;
  }

  closeInvitationDashboard() {
    this.showInvitationDashboard = false;
  }
}
