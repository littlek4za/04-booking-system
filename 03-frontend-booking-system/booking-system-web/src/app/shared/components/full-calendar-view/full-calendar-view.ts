import { AfterViewInit, Component, computed, effect, EventEmitter, inject, Input, OnChanges, OnDestroy, OnInit, Output, signal, SimpleChanges, untracked, ViewChild } from '@angular/core';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, DateSelectArg, DurationInput, EventClickArg, EventInput } from '@fullcalendar/core';
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
import { BookingService } from '@features/booking/booking-service';
import { BookingResponseDto } from '@features/booking/dtos/booking-response-dto';
import { BookingRequestDto } from '@features/booking/dtos/booking-request-dto';

@Component({
  selector: 'app-full-calendar-view',
  imports: [CommonModule, FullCalendarModule, CommonModule, FormsModule],
  templateUrl: './full-calendar-view.html',
  styleUrl: './full-calendar-view.css',
})
export class FullCalendarView implements OnChanges, OnDestroy, AfterViewInit {

  private slotService = inject(SlotService);
  private bookingService = inject(BookingService);
  private destroy$ = new Subject<void>();

  // Input from parent, For View Usage
  @Input() eventType: EventTypeModel | null = null;
  @Input() eventId: number | null = null;
  @Input() slotId: number | null = null;
  showBookings = signal<boolean>(false);

  // Input from parent, For Edit Usage
  @Input() slot?: SlotResponseDto;
  @Input() invitation?: InvitationResponseDto;

  // Output to Parent, For Edit Usage
  @Output() selectedStartTime = new EventEmitter<Date>();
  @Output() timeZone = new EventEmitter<string>();

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

  // singal field for booking list
  bookingListBySlotId = toSignal(this.bookingService.bookingListBySlotId$, { initialValue: [] as BookingResponseDto[] });
  bookingListByEventId = toSignal(this.bookingService.bookingListByEventId$, { initialValue: [] as BookingResponseDto[] });
  bookingList = computed<BookingResponseDto[]>(() => {
    const mode = this.mode;
    const slotIdFromView = this.slotId;
    const bookingListByEventId = this.bookingListByEventId();
    const bookingListBySlotId = this.bookingListBySlotId();
    if (mode === 'EDIT') {
      return bookingListBySlotId;
    }
    if (mode === 'VIEW') {
      if (slotIdFromView) {
        return bookingListBySlotId;
      }
      return bookingListByEventId;
    }
    return bookingListBySlotId ? bookingListBySlotId : bookingListByEventId;
  });

  filteredBookingList = computed<BookingResponseDto[]>(() => {
    const bookingList = this.bookingList();
    const selectedBookingList = this.selectedBookingList();

    return bookingList.filter(booking => selectedBookingList.has(booking.slot.id));
  })

  distinctBookingSlots = computed<SlotResponseDto[]>(() => {
    const bookingList = this.bookingList();
    const map = new Map<number, SlotResponseDto>();

    bookingList.forEach(booking => {
      if (!map.has(booking.slot.id)) {
        map.set(booking.slot.id, booking.slot);
      }
    });
    return Array.from(map.values());
  })

  // html field/logic field
  selectedSlots = signal<Set<number>>(new Set());
  selectedBookingList = signal<Set<number>>(new Set());
  selectedTimeZone = signal<string>('local');

  // html field for timezone
  timeZoneOption: TimeZoneOption[] = [];

  // logic field (signal) resulted from view / edit
  eventIdFromMode!: number;
  eventTypeFromMode!: EventTypeModel;

  // calendar option
  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin, momentTimeZonePlugin],
    dateClick: (info) => {
      if (info.view.type === 'dayGridMonth') {
        this.calendarApi.changeView('timeGridWeek', info.date);
      } else if (info.view.type === 'timeGridWeek') {
        this.calendarApi.changeView('timeGridDay', info.date);
      }
    },
    selectable: false,
    allDaySlot: false,
    slotDuration: '00:60:00',
    weekends: true,
    timeZone: 'local',
    events: [],
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

  constructor(timeZoneService: TimeZoneService) {
    this.timeZoneOption = timeZoneService.getAllTimeZones();
    this.selectedTimeZone.set(timeZoneService.getUserTimeZone());
    // initialized the selectedslot everytime there is mode change
    effect(() => {
      const slots = this.normalizedSlots();
      this.selectedSlots.set(new Set(slots.map(s => s.id)));

    });
    // initialized the selectedbookinglist everytime there is update
    effect(() => {
      const bookingList = this.bookingList();
      this.selectedBookingList.set(new Set(bookingList.map(booking => booking.slot.id)));

    });
    // init the calendar data
    effect(() => {
      const ready = this.calendarReady(); // trigger when calendarComponent ready
      if (!ready) { // check calendarComponent is ready, if not stop the code, then wait for trigger again
        return;
      }
      const slots = this.filteredSlots(); // trigger when normalize slot update
      const timeZone = this.selectedTimeZone();// trigger when selectedTimeZone update
      const bookingList = this.filteredBookingList();// trigger when bookingList update
      const showBookings = this.showBookings(); // trigger when showBookings is checked, only in VIEW Mode
      const eventId = this.eventIdFromMode;
      const eventType = this.eventTypeFromMode;

      console.log("4", ready);
      console.log(eventId, eventType);

      if (eventId !== undefined && eventType !== undefined) {
        untracked(() => {
          this.calendarApi.setOption('timeZone', timeZone);
          this.initCalendarData(slots, eventId, eventType, bookingList);
          console.log("5");
        });
      }

    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log("1");
    if (this.mode == 'VIEW' && (changes['eventId'] || changes['eventType'])) {
      console.log("2a");
      if (!this.eventId || !this.eventType) return;

      this.eventIdFromMode = this.eventId;
      this.eventTypeFromMode = this.eventType;
      this.bookingService.triggerRefreshForBookingListByEventId(this.eventIdFromMode);
      // slot id provided, get single slot
      if (this.slotId != null) {
        console.log("Single slot mode, triggering slot refresh");
        this.bookingService.triggerRefreshForBookingListBySlotId(this.slotId);
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
        console.log("Multi-slot mode, triggering all slots refresh");
        this.singleSlot.set(null);
        this.slotService.triggerRefresh(this.eventId);
      }
    }

    if (this.mode == 'EDIT' && (changes['slot'] || changes['invitation'])) {
      console.log("2b");
      if (!this.slot || !this.invitation) return;

      this.eventIdFromMode = this.invitation?.event.id;
      this.eventTypeFromMode = this.invitation?.event.eventType;
      this.bookingService.triggerRefreshForBookingListBySlotId(this.slot.id);

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

  ngAfterViewInit(): void {
    this.calendarReady.set(true);
    this.setUpCalendarOption();
    this.setUpCalendarOptionForViewMode();
  }

  private setUpCalendarOption() {
    this.calendarApi.setOption('navLinks', true);
    this.calendarApi.setOption('dayMaxEvents', true);

    this.calendarApi.setOption('navLinkDayClick', (date) => {
      const viewType = this.calendarApi.view.type;
      if (viewType === 'dayGridMonth') {
        this.calendarApi.changeView('timeGridWeek', date);
      } else if (viewType === 'timeGridWeek') {
        this.calendarApi.changeView('timeGridDay', date);
      }
    });
  }
  private setUpCalendarOptionForViewMode() {
    if (this.mode != 'VIEW') return;
    this.calendarApi.setOption('eventClick', (info) => {
      const timeZone = this.selectedTimeZone();
      const start = info.event.start
        ? moment.tz(info.event.start, timeZone).format('YYYY-MM-DD hh:mm a Z')
        : 'N/A';
      const end = info.event.end
        ? moment.tz(info.event.end, timeZone).format('YYYY-MM-DD hh:mm a Z')
        : 'N/A';

      alert(
        `Event Name: TO ADD LATER\n` +
        `Slot Name: ${info.event.title}\n` +
        `Id: ${info.event.id}\n` +
        `Start Time: ${start}\n` +
        `End Time: ${end}`
      )
    });
  }

  private initCalendarData(slots: SlotResponseDto[], eventId: number, eventType: EventTypeModel, bookingList: BookingResponseDto[]) {

    if (this.mode == "VIEW") {
      let eventInputList: EventInput[] = this.mapSlotToCalendarEventForViewMode(slots, eventId, eventType, bookingList);
      this.calendarApi.removeAllEvents();
      this.calendarApi.addEventSource(eventInputList);
    } else if (this.mode == "EDIT") {
      const slot = slots[0]; //EDIT only one slot will be choosen for process, convert it to one slot
      if (!slot) return;
      this.setUpCalendarOptionForEditMode(slot, eventType);
      let eventInputList: EventInput[] = this.mapSlotToCalendarEventForEditMode(slot, eventId, eventType, bookingList);
      this.calendarApi.removeAllEvents();
      this.calendarApi.addEventSource(eventInputList);
    } else {
      console.error("Calendar Mode Error");
    }
  }


  // core conversion
  private mapSlotToCalendarEventForViewMode(slotList: SlotResponseDto[], eventId: number, eventType: EventTypeModel, bookingList: BookingResponseDto[]): EventInput[] {

    const events: EventInput[] = [];

    for (let slot of slotList) {
      const baseEvent: EventInput = {
        title: slot.slotName,
        extendedProps: {
          eventId: eventId,
          slotId: slot.id,
          slotDescription: slot.slotDescription,
          maxBookPerInterval: slot.maxBookPerInterval,
        }
      }

      if (eventType == EventTypeModel.FIXED) {
        events.push({
          ...baseEvent,
          id: `event-${eventId}-slot-${slot.id}`,
          start: slot.slotStartTime,
          end: slot.slotEndTime,
          extendedProps: {
            ...baseEvent.extendedProps
          }
        });
      }

      if (eventType == EventTypeModel.FLEXIBLE) {
        if (!slot.flexibleDaysHours?.length) continue;

        slot.flexibleDaysHours.forEach((range, index) => {
          events.push({
            ...baseEvent,
            id: `event-${eventId}-slot-${slot.id}-rangeIndex-${index}`,
            start: range.open,
            end: range.close,
            extendedProps: {
              ...baseEvent.extendedProps,
              slotIntervalMinutes: slot.slotIntervalMinutes,
              slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
            }
          });
        });
      }

      if (eventType == EventTypeModel.BUSINESS) {
        if (!slot.businessDaysHours || !slot.businessTimeZone || Object.keys(slot.businessDaysHours).length === 0) continue;

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
              id: `event-${eventId}-slot-${slot.id}-day-${dayOfWeek}-rangeIndex-${index}`,
              daysOfWeek: [Number(dayOfWeek)],
              startTime: start,
              endTime: end,
              extendedProps: {
                ...baseEvent.extendedProps,
                slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
                slotIntervalMinutes: slot.slotIntervalMinutes,
                businessTimeZone: slot.businessTimeZone,
                businessAllowOt: slot.businessAllowOt
              }
            });
          });
        });
      }
    }
    if (this.showBookings() && bookingList && bookingList.length) {
      bookingList.forEach(booking => {
        let startTime;
        let endTime;
        if (eventType == EventTypeModel.FIXED) {
          startTime = moment(booking.bookedStartTime).clone();
          endTime = moment(booking.bookedEndTime).clone();
        } else {
          startTime = moment(booking.bookedStartTime).clone();
          // endTime = moment(booking.bookedStartTime).clone().add(booking.slot.slotIntervalMinutes, 'minutes');
          endTime = moment(booking.bookedEndTime).clone();
        }

        const startTitle = startTime.clone().tz(this.selectedTimeZone()).format('hh:mm a');
        const endTitle = endTime.clone().tz(this.selectedTimeZone()).format('hh:mm a');
        events.push({
          title: ` - Booked by: ${booking.lastName} ${booking.lastName}, Email: ${booking.email}`,
          id: `bookingId-${booking.id}`,
          start: startTime.toDate(),
          end: endTime.toDate(),
          color: '#fc6a6d',
          extendedProps: {
            isSlotBooked: true
          }
        });
      });
    }
    return events;
  }

  private setUpCalendarOptionForEditMode(slot: SlotResponseDto, eventType: EventTypeModel) {
    if (this.mode != 'EDIT' || !slot || !eventType) return;

    this.calendarApi.setOption('displayEventTime', false);

    if (eventType == EventTypeModel.FLEXIBLE) {

      if (slot.slotFrequencyIntervalMinutes == null) return;
      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotDuration = this.covertToSlotDuration(slotFrequencyIntervalMinutes);

      this.calendarApi.setOption('slotDuration', slotDuration);
      this.calendarApi.setOption('snapDuration', slotDuration);

      // get view to display
      const timeRanges = slot.flexibleDaysHours;
      if (!timeRanges || timeRanges.length === 0) return;

      const uniqueDays = new Set<string>();
      timeRanges.forEach(range => {

        const open = moment.tz(range.open, this.selectedTimeZone());
        const close = moment.tz(range.close, this.selectedTimeZone());

        const openDate = open.format('YYYY-MM-DD')
        const closeDate = close.format('YYYY-MM-DD')

        if (!uniqueDays.has(openDate)) {
          uniqueDays.add(openDate);
        }
        if (!uniqueDays.has(closeDate)) {
          uniqueDays.add(closeDate);
        }
      })

      let viewType: 'timeGridDay' | 'timeGridWeek' = 'timeGridDay'
      if (uniqueDays.size > 1) {
        viewType = 'timeGridWeek';
      }

      // get date to display
      const sortedDays = Array.from(uniqueDays).sort();
      const firstDay = sortedDays[0];
      const displayDate = moment.tz(firstDay, 'YYYY-MM-DD', this.selectedTimeZone()).toDate();

      // set view and date
      this.calendarApi.changeView(viewType, displayDate);
      this.setUpEventClickForEditMode(slot);
    }

    if (eventType == EventTypeModel.BUSINESS) {

      if (slot.slotFrequencyIntervalMinutes == null) return;
      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotDuration = this.covertToSlotDuration(slotFrequencyIntervalMinutes);

      this.calendarApi.setOption('slotDuration', slotDuration);
      this.calendarApi.setOption('snapDuration', slotDuration);
      this.calendarApi.changeView('timeGridWeek');
      this.setUpEventClickForEditMode(slot);
    }
  }

  private setUpEventClickForEditMode(slot: SlotResponseDto) {
    this.calendarApi.setOption('eventClick', (info) => {
      if (info.event.extendedProps['isSlotBooked']) {
        alert('Booked by others');
        return;
      }
      const start = info.event.start;
      if (!start) return;

      const startZdt = moment.tz(start, this.selectedTimeZone());
      const confirmed = confirm(`Select this time?\nStart Time: ${startZdt.format('YYYY-MM-DD hh:mm a Z')}\nEnd Time: ${startZdt.clone().add(slot.slotIntervalMinutes, 'minutes').format('YYYY-MM-DD hh:mm a Z')}`);
      if (confirmed) {
        this.selectedStartTime.emit(start);
        const datePipeTimeZone = moment().tz(this.selectedTimeZone()).format('ZZ');
        this.timeZone.emit(datePipeTimeZone);
        console.log('Selected time', start);
        this.closeCalendar();
      }

    });
  }

  mapSlotToCalendarEventForEditMode(slot: SlotResponseDto, eventId: number, eventType: EventTypeModel, bookingList: BookingResponseDto[]): EventInput[] {

    const events: EventInput[] = [];

    const baseEvent: EventInput = {
      title: slot.slotName,
      extendedProps: {
        eventId: eventId,
        slotId: slot.id,
        slotDescription: slot.slotDescription,
        maxBookPerInterval: slot.maxBookPerInterval,
      }
    }

    if (eventType == EventTypeModel.FIXED) {
      const start = moment(slot.slotStartTime, this.selectedTimeZone());
      const end = moment(slot.slotEndTime, this.selectedTimeZone())
      events.push({
        ...baseEvent,
        title: `${start.format('hh:mm a')} - ${end.format('hh:mm a')} - ${slot.slotName}`,
        id: `event-${eventId}-slot-${slot.id}`,
        start: slot.slotStartTime,
        end: slot.slotEndTime,
        extendedProps: {
          ...baseEvent.extendedProps
        }
      });
    }

    if (eventType == EventTypeModel.FLEXIBLE) {
      if (!slot.flexibleDaysHours?.length) return events;

      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotIntervalMinutes = slot.slotIntervalMinutes;

      if (!slotFrequencyIntervalMinutes || !slotIntervalMinutes) return events;

      slot.flexibleDaysHours.forEach((range, index) => {

        const startZdt = moment(range.open);
        const endZdt = moment(range.close);

        let current = startZdt.clone();
        let i = 0;

        while (current.clone().add(slotFrequencyIntervalMinutes, 'minutes').isSameOrBefore(endZdt)) {
          const selectEnd = current.clone().add(slotFrequencyIntervalMinutes, 'minutes');
          const startTitle = current.clone().tz(this.selectedTimeZone()).format('hh:mm a');
          const endTitle = current.clone().tz(this.selectedTimeZone()).add(slot.slotIntervalMinutes, 'minutes').format('hh:mm a');
          const slotEnd = current.clone().add(slot.slotIntervalMinutes, 'minutes');

          const isSlotBooked = this.isSlotBooked(current.clone().toDate(), slotEnd.clone().toDate(), bookingList);

          events.push({
            ...baseEvent,
            title: `${startTitle} - ${endTitle} - ${slot.slotName}`,
            id: `event-${eventId}-slot-${slot.id}-rangeIndex-${index}-${i}`,
            start: current.toDate(),
            end: selectEnd.toDate(),
            backgroundColor: isSlotBooked ? '#ff4d4f' : '#3788d8',
            borderColor: isSlotBooked ? '#ff4d4f' : '#3788d8',
            textColor: isSlotBooked ? '#000000' : '#ffffff',
            extendedProps: {
              ...baseEvent.extendedProps,
              slotIntervalMinutes: slotIntervalMinutes,
              slotFrequencyIntervalMinutes: slotFrequencyIntervalMinutes,
              isSlotBooked: isSlotBooked,
            }
          });

          current = selectEnd;
          i++;
        }
      });
    }

    if (eventType == EventTypeModel.BUSINESS) {
      if (!slot.businessDaysHours
        || !slot.businessTimeZone
        || Object.keys(slot.businessDaysHours).length === 0) return events;

      const businessTz = slot.businessTimeZone;
      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotIntervalMinutes = slot.slotIntervalMinutes;

      if (!slotFrequencyIntervalMinutes || !slotIntervalMinutes) return events;

      // push recurring event first
      Object.entries(slot.businessDaysHours).forEach(([dayOfWeek, ranges]) => {
        ranges.forEach((range, index) => {
          const startZdt = moment.tz(range.open, 'HH:mm', businessTz);
          // .tz(this.selectedTimeZone())
          // .format('HH:mm');
          const endZdt = moment.tz(range.close, 'HH:mm', businessTz);
          // .tz(this.selectedTimeZone())
          // .format('HH:mm');

          let current = startZdt.clone();
          let i = 0;
          let endZdtBaseOnAllowOt = endZdt
          if (!slot.businessAllowOt) {
            endZdtBaseOnAllowOt = endZdt.clone().subtract(slotIntervalMinutes, 'minutes');
          }
          while (current.clone().add(slotFrequencyIntervalMinutes, 'minutes').isSameOrBefore(endZdtBaseOnAllowOt)) {
            const next = current.clone().add(slotFrequencyIntervalMinutes, 'minutes');

            const startTime = current.clone().tz(this.selectedTimeZone()).format('HH:mm');
            const nextEnd = next.clone().tz(this.selectedTimeZone()).format('HH:mm');

            const startTitle = current.clone().tz(this.selectedTimeZone()).format('hh:mm a');
            const endTitle = current.clone().tz(this.selectedTimeZone()).add(slot.slotIntervalMinutes, 'minutes').format('hh:mm a');
            events.push({
              ...baseEvent,
              title: `${startTitle} - ${endTitle} - ${slot.slotName}`,
              id: `event-${eventId}-slot-${slot.id}-day-${dayOfWeek}-rangeIndex-${index}-${i}`,
              daysOfWeek: [Number(dayOfWeek)],
              startTime: startTime,
              endTime: nextEnd,
              display: 'background',
              color: '#5d9edf',
              className: 'slot-item',
              extendedProps: {
                ...baseEvent.extendedProps,
                slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
                slotIntervalMinutes: slot.slotIntervalMinutes,
                businessTimeZone: slot.businessTimeZone,
                businessAllowOt: slot.businessAllowOt,
              }
            });
            current = next;
            i++;
          }
        });
      });

      bookingList.forEach(booking => {
        const startTime = moment(booking.bookedStartTime).clone().toDate();
        // const endTime = moment(booking.bookedStartTime).clone().add(slot.slotIntervalMinutes, 'minutes').toDate();
        const endTime = moment(booking.bookedEndTime).clone().toDate();
        const startTitle = moment(booking.bookedStartTime).clone().tz(this.selectedTimeZone()).format('hh:mm a');
        // const endTitle = moment(booking.bookedStartTime).clone().tz(this.selectedTimeZone()).add(slot.slotIntervalMinutes, 'minutes').format('hh:mm a');
        const endTitle = moment(booking.bookedEndTime).clone().tz(this.selectedTimeZone()).format('hh:mm a');
        events.push({
          title: `BOOKED: ${startTitle} - ${endTitle} - ${slot.slotName}`,
          id: `bookingId-${booking.id}`,
          start: startTime,
          end: endTime,
          display: 'background',
          color: '#fc6a6d',
          className: 'booked-slot-item',
          extendedProps: {
            isSlotBooked: true
          }
        });
      });
    }

    return events;
  }

  private isSlotBooked(slotStart: Date, slotEnd: Date, bookingList: BookingResponseDto[]): boolean {
    if (!slotStart || !slotEnd || !bookingList) return false;

    return bookingList.some(b => {
      const bookedStart = new Date(b.bookedStartTime);
      const bookedEnd = new Date(b.bookedEndTime);

      return slotStart < bookedEnd && slotEnd > bookedStart;
    });
  }


  covertToSlotDuration(slotFrequencyIntervalMinutes: number): any {
    const hh = Math.floor(slotFrequencyIntervalMinutes / 60).toString().padStart(2, '0');
    const mm = (slotFrequencyIntervalMinutes % 60).toString().padStart(2, '0');

    return `${hh}:${mm}:00`;
  }

  // Addtional UI Function - Chooseable TimeZone
  onTimeZoneChange(timeZone: string) {
    this.selectedTimeZone.set(timeZone);
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

  toggleBookingFilter(slotId: number, checked: boolean) {
    const updatedFilterBookingList = new Set(this.selectedBookingList());
    if (checked) {
      updatedFilterBookingList.add(slotId);
    } else {
      updatedFilterBookingList.delete(slotId);
    }
    this.selectedBookingList.set(updatedFilterBookingList);
  }

  toggleShowBookings(checked: boolean) {
    this.showBookings.set(checked);
  }


  closeCalendar() {
    this.close.emit();
  }
}
