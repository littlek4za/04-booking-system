import { AfterViewInit, Component, computed, effect, EventEmitter, inject, Input, OnChanges, OnInit, Output, signal, SimpleChanges, ViewChild } from '@angular/core';
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

@Component({
  selector: 'app-full-calendar-view',
  imports: [CommonModule, FullCalendarModule],
  templateUrl: './full-calendar-view.html',
  styleUrl: './full-calendar-view.css',
})
export class FullCalendarView {

  private slotService = inject(SlotService);
  private destroy$ = new Subject<void>();

  @Input() eventType!: EventTypeModel;
  @Input() eventId!: number;
  @Input() slotId: number | null = null;
  @Input() initialView: string = 'dayGridMonth';
  @Input() viewDate: Date = new Date();
  @Input() mode!: 'EDIT' | 'VIEW';

  @Output() dateChange = new EventEmitter<Date>();
  @Output() close = new EventEmitter<void>();

  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;

  // field for slots and slot
  slotList = toSignal(this.slotService.slot$, { initialValue: [] });
  singleSlot = signal<SlotResponseDto | null>(null);

  normalizedSlots = computed<SlotResponseDto[]>(() => {
    const slot = this.singleSlot();
    return slot ? [slot] : this.slotList();
  })

  selectedSlots = new Set<number>();

  // field for timezone
  timeZoneOption: TimeZoneOption[] = [
    this.buildTimeZoneOption('local', 'Local'),
    this.buildTimeZoneOption('UTC', 'UTC'),
    this.buildTimeZoneOption('Asia/Tokyo', 'Asia/Tokyo'),
    this.buildTimeZoneOption('Australia/Perth', 'Australia/Perth')
  ];

  // calendar option
  calendarOptions: CalendarOptions = {
    initialView: this.initialView,
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
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
    if (changes['eventId'] || changes['slotId']) {
      if (!this.eventId) return;
      if (this.slotId) {
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
      } else {
        this.singleSlot.set(null);
        this.slotService.triggerRefresh(this.eventId);
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  constructor() {
    effect(() => {
      const slots = this.normalizedSlots();

      if (!this.eventId) return;
      if (!this.eventType) return;
      if (this.mode !== 'VIEW') return;
      if (slots.length === 0) return;
      console.log('View before initClendarData', this.normalizedSlots());
      slots.forEach(slot=> this.selectedSlots.add(slot.id));
      this.initCalendarData();
    })
  }

  private initCalendarData() {
    const eventInputList: EventInput[] =
      this.mapSlotToCalendarEvent(this.normalizedSlots(), this.eventId, this.eventType);
    this.calendarApi.removeAllEvents();
    this.calendarApi.addEventSource(eventInputList);
  }

  private get calendarApi() {
    return this.calendarComponent.getApi();
  }

  // Addtional UI Function - toggle Weekend
  toggleWeekends() {
    this.calendarOptions = {
      ...this.calendarOptions,
      weekends: !this.calendarOptions.weekends
    };
  }

  // Additional UI Function - filter slot
  toggleSlotFilter(slotId: number, checked: boolean){
    if(checked){
      this.selectedSlots.add(slotId);
    } else {
      this.selectedSlots.delete(slotId);
    }

    const filteredSlots = this.normalizedSlots().filter(slot => this.selectedSlots.has(slot.id));
    const events = this.mapSlotToCalendarEvent(filteredSlots, this.eventId, this.eventType);

    this.calendarApi.removeAllEvents();
    this.calendarApi.addEventSource(events);
  }

  // Addtional UI Function - Chooseable TimeZone
  onTimeZoneChange(timeZone: string) {
    this.calendarApi.setOption('timeZone', timeZone);
  }

  getUtcOffsetForTimeZone(timeZone: string): string {
    const date = new Date();

    const parts = new Intl.DateTimeFormat('en-GB', {
      timeZone,
      timeZoneName: 'short' // 'long' -> Greenwich Mean Time, Pacific Standard Time
    }).formatToParts(date);
    // .format(date) -> "1/30/2026, GMT+8"
    // .formatToParts(date) ->
    // [
    //   { type: "month", value: "1" },
    //   { type: "literal", value: "/" },
    //   { type: "day", value: "30" },
    //   { type: "literal", value: "/" },
    //   { type: "year", value: "2026" },
    //   { type: "literal", value: ", " },
    //   { type: "timeZoneName", value: "GMT+8" }
    // ]

    const tzPart = parts.find(p => p.type === 'timeZoneName');

    return tzPart?.value ?? '';
  }

  buildTimeZoneOption(value: string, label: string): TimeZoneOption {
    if (value === 'local') {
      const localTz = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const offset = this.getUtcOffsetForTimeZone(localTz);

      return {
        value: 'local',
        label: `${label} (${offset})`,
        offset: +offset
      };
    }

    const offset = this.getUtcOffsetForTimeZone(value);

    return {
      value,
      label: `${label} (${offset})`,
      offset: +offset
    };
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

      if(eventType ==  EventTypeModel.BUSINESS) {
        if (!slot.businessDaysHours || Object.keys(slot.businessDaysHours).length === 0) continue;

        Object.entries(slot.businessDaysHours).forEach(([dayOfWeek,ranges])=>{
          ranges.forEach((range,index)=>{
            events.push({
              ...baseEvent,
              id: `event-${eventId}-slot-${slot.id}-${dayOfWeek}-${index}`,
              daysOfWeek:[Number(dayOfWeek)],
              startTime: range.open,
              endTime: range.close,
              display: 'auto'
            });
          });
        }); 
      }
    }

    return events;
  }

  minutestoFullCalendarDuration(minutes: number) {
    const hrs = Math.floor(minutes / 60).toString().padStart(2, '0');
    const mins = (minutes % 60).toString().padStart(2, '0');
    return `${hrs}:${mins}:00`;
  } // 00:05:00

  closeCalendar() {
    this.close.emit();
  }
}
