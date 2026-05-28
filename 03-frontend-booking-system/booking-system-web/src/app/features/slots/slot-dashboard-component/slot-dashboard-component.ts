import { Component, computed, inject, Input, OnDestroy, OnInit, signal } from '@angular/core';
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
import { NotificationService } from '@core/services/notification-service';
import { EventWithSlotCountResponseDto } from '@features/events/dtos/event-with-slot-count-response-dto';

@Component({
  standalone: true,
  selector: 'app-slot-dashboard-component',
  imports: [CommonModule, RouterLink, SlotEditWizard, DatePipe, FullCalendarView, BookingManagerDashboard, InvitationEditWizard, InvitationDashboard],
  templateUrl: './slot-dashboard-component.html',
  styleUrl: './slot-dashboard-component.css',
})
export class SlotDashboardComponent implements OnInit, OnDestroy {

  // Service Inject
  private slotService = inject(SlotService);

  // class
  protected readonly EventType = EventTypeModel;

  // show or hide component
  showSlotWizard: boolean = false;
  showBookingManagerDashboard: boolean = false;
  showInvitationWizard: boolean = false;
  showInvitationDashboard: boolean = false;
  openCalendarView: boolean = false;

  // IO for component
  updateSlotName: string | null = null;
  updateSlotId: number | null = null;
  modeSlotWizard!: 'CREATE' | 'UPDATE';

  // for destroy usage
  private destroy$ = new Subject<void>();

  // sort and filter field
  activeSortMode = signal<'LATEST' | 'EARLIEST' | 'NAME_ASC' | 'NAME_DSC'>('LATEST');
  searchText = signal<string>('');

  // data
  allSlot = toSignal(this.slotService.slotListByEventId$, { initialValue: [] });
  eventId!: number;
  event = signal<EventWithSlotCountResponseDto | null>(null);

  slotList = computed(() => {
    let list = [...this.allSlot()];

    const searchTexts = this.searchText().trim().toLowerCase().split(/\s+/);
    const sortMode = this.activeSortMode();

    if (searchTexts) {
      list = list.filter(slot => {
        const searchableText = [
          slot.slotName,
          slot.slotDescription,
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase();

        return searchTexts.every(word => searchableText.includes(word));
      })
    }

    if (sortMode === 'LATEST') {
      list.sort((a, b) => 
        new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      )
    }

    if (sortMode === 'EARLIEST') {
      list.sort((a, b) => 
        new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime()
      )
    }

    if (sortMode === 'NAME_ASC') {
      list.sort((a, b) => 
        a.slotName.localeCompare(b.slotName)
      )
    }

    if (sortMode === 'NAME_DSC') {
      list.sort((a, b) => 
        b.slotName.localeCompare(a.slotName)
      )
    }

    return list;
  })


  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private logger: LoggerService,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.refreshSlotListWithEventId();
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
            this.getEventInfo();
          }
        }
      );
  }

  private getEventInfo() {
    this.logger.debug(`[SlotDashboardComponent] Sending eventService.getEventById request`);
    this.eventService.getEventById(this.eventId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.event.set(res);
        },
        error: () => {
        }
      });
  }

  confirmDeleteSlot(slotId: number) {
    this.logger.debug(`[SlotDashboardComponent] Sending slotService.slotDeleteValidation request`);
    this.slotService.slotDeleteValidation(this.eventId, slotId).subscribe({
      next: (res) => {
        if (res.canDelete == false) {
          this.notificationService.warning(
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
    this.updateSlotId = null;
    this.showSlotWizard = true;
  }

  openUpdateSlotWizard(slotId: number) {
    this.modeSlotWizard = 'UPDATE';
    this.updateSlotId = slotId;
    this.showSlotWizard = true;
  }

  closeSlotWizard() {
    this.showSlotWizard = false;
  }

  openCalendar(slotId: number) {
    this.updateSlotId = slotId;
    console.log('Opening Calendar View', this.updateSlotId);
    this.openCalendarView = true;
  }

  closeCalendar() {
    this.openCalendarView = false;
  }

  openBookingManagerDashboard(slotId: number, slotName: string) {
    this.updateSlotName = slotName;
    this.updateSlotId = slotId;
    this.showBookingManagerDashboard = true;
  }

  closeBookingManagerDashboard() {
    this.showBookingManagerDashboard = false;
  }

  closeInvitationWizard() {
    this.showInvitationWizard = false;
  }

  openInvitationWizard(slotId: number) {
    this.updateSlotId = slotId;

    this.showInvitationWizard = true;
  }

  openInvitationDashboard(slotId: number) {
    this.updateSlotId = slotId;
    this.showInvitationDashboard = true;
  }

  closeInvitationDashboard() {
    this.showInvitationDashboard = false;
  }

  searchSlots(value:string) {
    this.searchText.set(value);
  }

  sortEvents(type: 'LATEST' | 'EARLIEST' | 'NAME_ASC' | 'NAME_DSC' ) {
    this.activeSortMode.set(type);
  }

}
