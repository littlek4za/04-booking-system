import { AfterViewInit, Component, computed, effect, EventEmitter, inject, Input, OnChanges, OnDestroy, OnInit, Output, signal, SimpleChanges, untracked, ViewChild } from '@angular/core';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, DateSelectArg, EventClickArg, EventInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import { CommonModule } from '@angular/common';
import interactionPlugin, { DateClickArg } from '@fullcalendar/interaction';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { TimeZoneOption } from '@shared/model/time-zone-option';
import { SlotService } from '@features/slots/slot-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { Subject, takeUntil } from 'rxjs';
import momentTimeZonePlugin from '@fullcalendar/moment-timezone';
import { TimeZoneService } from '@shared/model/time-zone-service';
import { FormsModule } from '@angular/forms';
import moment from 'moment-timezone';
import { InvitationResponseDto } from '@features/invitations/dtos/invitation-response-dto';

@Component({
  selector: 'app-full-calendar-view',
  imports: [CommonModule, FullCalendarModule, CommonModule, FormsModule],
  templateUrl: './full-calendar-view.html',
  styleUrl: './full-calendar-view.css',
})
export class FullCalendarView implements OnChanges, OnDestroy, AfterViewInit {

  private slotService = inject(SlotService);
  private timeZoneService = inject(TimeZoneService);
  private destroy$ = new Subject<void>();

  // Input from parent, For View Usage
  @Input() eventType?: EventTypeModel;
  @Input() eventId?: number;
  @Input() slotId: number | null = null;

  // Input from parent, For Edit Usage
  @Input() slot?: SlotResponseDto;
  @Input() invitation?: InvitationResponseDto;

  @Input() initialView: string = 'dayGridMonth';
  @Input() viewDate: Date = new Date();
  @Input() mode!: 'EDIT' | 'VIEW';

  @Output() dateChange = new EventEmitter<Date>();
  @Output() close = new EventEmitter<void>();

  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;
  calendarReady = signal(false);

  // signal field for slots and slot
  slotList = toSignal(this.slotService.slotList$, { initialValue: [] });
  singleSlot = signal<SlotResponseDto | null>(null);

  normalizedSlots = computed<SlotResponseDto[]>(() => {
    const slot = this.singleSlot();
    return slot ? [slot] : this.slotList();
  })

  filteredSlots = computed<SlotResponseDto[]>(() => {
    const slots = this.normalizedSlots();
    const selected = this.selectedSlots();

    return slots.filter(slot => selected.has(slot.id));
  });

  // html field/logic field
  selectedSlots = signal<Set<number>>(new Set());
  selectedTimeZone = signal<string>('local');

  // html field for timezone
  timeZoneOption: TimeZoneOption[] = [];

  // logic field (signal) resulted from view / edit
  eventIdFromMode!: number;
  eventTypeFromMode!: EventTypeModel;

  // logic field
  // firstload = true;

  // calendar option
  calendarOptions: CalendarOptions = {
    initialView: this.initialView,
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin, momentTimeZonePlugin],
    dateClick: (info) => { this.calendarApi.changeView('timeGridDay', info.date) },
    selectable: false,
    slotDuration: '00:30:00',
    select: (info: DateSelectArg) => { alert('event click! ' + info.view) },
    weekends: true,
    timeZone: 'local',
    events: [],
    eventClick: (info: EventClickArg) => {
      alert(
        `Event Name: ${info.event.title}\n` +
        `Event Id: ${info.event.id}\n` +
        `Start Time: ${info.event.start}\n` +
        `End Time: ${info.event.end}`
      )
    },
    // datesSet: (info) => alert('dateSet click! ' + info.start),
    headerToolbar: {
      left: 'prev,next,today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    eventTimeFormat: {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    }
  };

  ngOnChanges(changes: SimpleChanges): void {
    console.log("1");
    if (this.mode == 'VIEW' && (changes['eventId'] || changes['eventType'])) {
      console.log("2a");
      if (!this.eventId || !this.eventType) return;

      this.eventIdFromMode = this.eventId;
      this.eventTypeFromMode = this.eventType;

      // slot id provided, get single slot
      if (this.slotId) {
        console.log("2a.1");
        this.slotService.getSlotByIdAndEventId(this.eventId, this.slotId)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (res) => {
              console.log("Get Slot successfully");
              this.singleSlot.set(res);
            },
            error: (err) => {
              console.log("Get Slot failed");
            }
          });
      } else { // slot id not provided, get slotList
        console.log("2a.2");
        this.singleSlot.set(null);
        this.slotService.triggerRefresh(this.eventId);
      }
    }

    if (this.mode == 'EDIT' && (changes['slot'] || changes['invitation'])) {
      console.log("2b");
      if (!this.slot || !this.invitation) return;

      this.eventIdFromMode = this.invitation?.event.id;
      this.eventTypeFromMode = this.invitation?.event.eventType;

      this.singleSlot.set(this.slot);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private get calendarApi() {
    return this.calendarComponent.getApi();
  }

  constructor() {
    this.timeZoneOption = this.timeZoneService.getAllTimeZones();
    this.selectedTimeZone.set(this.timeZoneService.getUserTimeZone());
    // initialized the selectedslot everytime there is mode change
    effect(() => {
      const slots = this.normalizedSlots();
      this.selectedSlots.set(new Set(slots.map(s => s.id)));

    });
    // init the calendar data
    effect(() => {
      const ready = this.calendarReady(); // trigger when calendarComponent ready
      if (!ready) { // check calendarComponent is ready, if not stop the code, then wait for trigger again
        return;
      }
      const slots = this.filteredSlots(); // trigger when normalize slot update
      const timeZone = this.selectedTimeZone();// trigger when selectedTimeZone update

      const eventId = this.eventIdFromMode;
      const eventType = this.eventTypeFromMode;

      console.log("4", ready);
      console.log(eventId, eventType);

      if (eventId !== undefined && eventType !== undefined) {
        untracked(() => {
          this.calendarApi.setOption('timeZone', timeZone);
          this.initCalendarData(slots, eventId, eventType);
          console.log("5");
        });
      }

    });
  }

  ngAfterViewInit(): void {
    this.calendarReady.set(true);
  }

  private initCalendarData(slots: SlotResponseDto[], eventId: number, eventType: EventTypeModel) {
    const eventInputList: EventInput[] =
      this.mapSlotToCalendarEvent(slots, eventId, eventType);
    if(eventType == EventTypeModel.BUSINESS){
      this.calendarApi.changeView('timeGridWeek');
    }
    this.calendarApi.removeAllEvents();
    this.calendarApi.addEventSource(eventInputList);
  }

  // Addtional UI Function - toggle Weekend
  toggleWeekends() {
    this.calendarOptions = {
      ...this.calendarOptions,
      weekends: !this.calendarOptions.weekends
    };
  }

  // Additional UI Function - filter slot
  toggleSlotFilter(slotId: number, checked: boolean) {
    const updatedFilterSlotList = new Set(this.selectedSlots());
    if (checked) {
      updatedFilterSlotList.add(slotId);
    } else {
      updatedFilterSlotList.delete(slotId);
    }
    this.selectedSlots.set(updatedFilterSlotList);
  }

  // Addtional UI Function - Chooseable TimeZone
  onTimeZoneChange(timeZone: string) {
    this.selectedTimeZone.set(timeZone);
  }

  // core conversion
  private mapSlotToCalendarEvent(slotList: SlotResponseDto[], eventId: number, eventType: EventTypeModel): EventInput[] {

    const events: EventInput[] = [];

    for (let slot of slotList) {
      const baseEvent: EventInput = {
        title: slot.slotName,
        extendedProps: {
          eventId: eventId,
          slotId: slot.id,
          slotDescription: slot.slotDescription,
          maxBookPerInterval: slot.maxBookPerInterval,
          slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
        }
      }

      if (eventType == EventTypeModel.FIXED) {
        events.push({
          ...baseEvent,
          id: `event-${eventId}-slot-${slot.id}`,
          start: slot.slotStartTime,
          end: slot.slotEndTime,
        });
      }

      if (eventType == EventTypeModel.FLEXIBLE) {
        if (!slot.flexibleDaysHours?.length) continue;

        slot.flexibleDaysHours.forEach((range, index) => {
          events.push({
            ...baseEvent,
            id: `event-${eventId}-slot-${slot.id}-${index}`,
            start: range.open,
            end: range.close,
            extendedProps: {
              ...baseEvent.extendedProps,
              slotIntervalMinutes: slot.slotIntervalMinutes,
              slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes
            }
          });
        });
      }

      if (eventType == EventTypeModel.BUSINESS) {
        if (!slot.businessDaysHours || Object.keys(slot.businessDaysHours).length === 0) continue;

        if (!slot.businessTimeZone) {
          throw new Error('No Business Time Zone found, please contact administrator');
        }

        const businessTz = slot.businessTimeZone;

        Object.entries(slot.businessDaysHours).forEach(([dayOfWeek, ranges]) => {
          ranges.forEach((range, index) => {
            const start = moment.tz(range.open, 'HH:mm', businessTz)
              .tz(this.selectedTimeZone())
              .format('HH:mm');
            const end = moment.tz(range.close, 'HH:mm', businessTz)
              .tz(this.selectedTimeZone())
              .format('HH:mm');

            events.push({
              ...baseEvent,
              id: `event-${eventId}-slot-${slot.id}-${dayOfWeek}-${index}`,
              daysOfWeek: [Number(dayOfWeek)],
              startTime: start,
              endTime: end,
              display: 'auto'
            });
          });
        });
      }
    }

    return events;
  }

  closeCalendar() {
    this.close.emit();
  }
}