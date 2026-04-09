import { Component, inject, OnDestroy, OnInit } from '@angular/core';
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
  slotList = toSignal(this.slotService.slotList$, { initialValue: [] });
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

  // for destroy usage
  private destroy$ = new Subject<void>();


  constructor(private route: ActivatedRoute, private eventService: EventService) { }

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
          this.slotService.triggerRefresh(this.eventId);
        }
      }
    );
  }

  private subscribeEventType() {
    this.eventService.getEventById(this.eventId)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (res) => {
        console.log('GET Event Succesful', res);
        this.eventType = res.eventType;
      },
      error: (err) => {
        console.log('GET Event Failed');
      }
    });
  }

  confirmDeleteSlot(slotId: number) {
    if (confirm("Are you sure you want to delete this Slot?")) {
      this.deleteSlotById(slotId);
    }
  }

  private deleteSlotById(slotId: number) {
    this.slotService.deleteSlotByIdAndEvent(this.eventId, slotId)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (res) => {
        console.log('Delete Slot Succesfully');
        this.slotService.triggerRefresh(this.eventId);
      },
      error: (err) => {
        console.error('Delete Slot Failed');
      }
    })
  }

  //Slot Wizard

  formatDuration(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;

    if (h > 0 && m > 0) return `${h} hour ${m} minute`;
    if (h > 0) return `${h} hour`;
    return `${m} minute`;
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

  openBookingManagerDashboard(slotId: number) {
    this.showBookingManagerDashboard = true;
    this.slotId = slotId;
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
