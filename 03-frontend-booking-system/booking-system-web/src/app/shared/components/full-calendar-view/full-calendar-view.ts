import { AfterViewInit, Component, computed, effect, EventEmitter, inject, Input, OnChanges, OnDestroy, OnInit, Output, signal, SimpleChanges, untracked, ViewChild } from '@angular/core';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, EventInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import { CommonModule } from '@angular/common';
import interactionPlugin from '@fullcalendar/interaction';
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
import { OrganizerBookingResponseDto } from '@features/booking/dtos/organizer-booking-response-dto';
import { SlotBookedTimeResponseDto } from '@features/booking/dtos/slot-booked-time-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { NotificationService } from '@core/services/notification-service';

@Component({
  selector: 'app-full-calendar-view',
  imports: [CommonModule, FullCalendarModule, CommonModule, FormsModule],
  templateUrl: './full-calendar-view.html',
  styleUrl: './full-calendar-view.css',
})
export class FullCalendarView implements OnChanges, OnDestroy, AfterViewInit {

  private slotService = inject(SlotService);
  private bookingService = inject(BookingService);
  private logger = inject(LoggerService);

  // destroy field
  private destroy$ = new Subject<void>();

  @Input() mode!: 'EDIT' | 'VIEW';

  // Input from parent, For View Usage
  @Input() eventType: EventTypeModel | null = null;
  @Input() eventId: number | null = null;
  @Input() slotId: number | null = null;

  // Input from parent, For Edit Usage
  @Input() slot?: SlotResponseDto;
  @Input() invitation?: InvitationResponseDto;

  // Output to Parent, For Edit Usage
  @Output() selectedStartTime = new EventEmitter<Date>();
  @Output() timeZone = new EventEmitter<string>();

  @Output() close = new EventEmitter<void>();

  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;

  // signal field for slots and slot (VIEW)
  slotList = toSignal(this.slotService.slotListByEventId$, { initialValue: [] });
  singleSlot = signal<SlotResponseDto | null>(null);
  selectedSlots = signal<Set<number>>(new Set());

  normalizedSlots = computed<SlotResponseDto[]>(() => {
    const slot = this.singleSlot();
    return slot ? [slot] : this.slotList();
  })

  filteredSlots = computed<SlotResponseDto[]>(() => {
    const slots = this.normalizedSlots();
    const selected = this.selectedSlots();

    return slots.filter(slot => selected.has(slot.id));
  });

  // singal field for booking list (VIEW)
  organizerBookingListBySlotId = toSignal(this.bookingService.organizerBookingListBySlotId$, { initialValue: [] as OrganizerBookingResponseDto[] });
  organizerBookingListByEventId = toSignal(this.bookingService.organizerBookingListByEventId$, { initialValue: [] as OrganizerBookingResponseDto[] });
  selectedOrganizerBookingListForView = signal<Set<number>>(new Set());

  organizerBookingListForView = computed<OrganizerBookingResponseDto[]>(() => {
    if (this.mode !== 'VIEW') {
      return [];
    }

    const organizerBookingListByEventId = this.organizerBookingListByEventId();
    const organizerBookingListBySlotId = this.organizerBookingListBySlotId();

    if (this.slotId) {
      return organizerBookingListBySlotId;
    }
    return organizerBookingListByEventId;
  });

  filteredOrganizerBookingList = computed<OrganizerBookingResponseDto[]>(() => {
    const bookingList = this.organizerBookingListForView();
    const selectedBookingList = this.selectedOrganizerBookingListForView();

    return bookingList.filter(booking => selectedBookingList.has(booking.slot.id));
  })

  distinctBookingSlots = computed<SlotResponseDto[]>(() => {
    const bookingList = this.organizerBookingListForView();
    const map = new Map<number, SlotResponseDto>();

    bookingList.forEach(booking => {
      if (!map.has(booking.slot.id)) {
        map.set(booking.slot.id, booking.slot);
      }
    });
    return Array.from(map.values());
  })

  // singal field for booking list (EDIT)
  slotBookedTimesBySlotId = toSignal(this.bookingService.slotBookedTimesBySlotId$, { initialValue: [] as SlotBookedTimeResponseDto[] });
  slotBookedTimesForEdit = computed<SlotBookedTimeResponseDto[]>(() => {
    if (this.mode !== 'EDIT') {
      return [];
    }
    return this.slotBookedTimesBySlotId();
  });

  // html field
  timeZoneOption: TimeZoneOption[] = [];
  selectedTimeZone = signal<string>('local');
  showBookings = signal<boolean>(false)

  // logic field (signal) resulted from view / edit
  eventIdFromMode!: number;
  eventTypeFromMode!: EventTypeModel;

  // logic field
  calendarReady = signal(false);

  // calendar date visible field
  private visibleRangeStart: Date | null = null;
  private visibleRangeEnd: Date | null = null;
  private editInitialViewApplied: boolean = false;

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

  constructor(
    private timeZoneService: TimeZoneService,
    private notificationService: NotificationService
  ) {
    this.timeZoneOption = this.timeZoneService.getAllTimeZones();
    this.selectedTimeZone.set(this.timeZoneService.getUserTimeZone());
    // initialized the selectedslot everytime there is changes
    effect(() => {
      const slots = this.normalizedSlots();
      this.selectedSlots.set(new Set(slots.map(s => s.id)));
      this.logger.debug('[FullCalendarView] Selected Slot List initialized');
    });
    // initialized the selectedbookinglist everytime there is changes
    effect(() => {
      const organizerBookingListForView = this.organizerBookingListForView();
      this.selectedOrganizerBookingListForView.set(new Set(organizerBookingListForView.map(booking => booking.slot.id)));
      this.logger.debug('[FullCalendarView] Selected Booking List initialized');
    });

    // init the calendar data
    effect(() => {
      const ready = this.calendarReady(); // trigger when calendarComponent ready
      const slots = this.filteredSlots(); // trigger when normalize slot update
      const timeZone = this.selectedTimeZone();// trigger when selectedTimeZone update
      const filteredOrganizerBookingList = this.filteredOrganizerBookingList();// trigger when bookingList update (for VIEW)
      const slotBookedTimesForEdit = this.slotBookedTimesForEdit();// trigger when bookingList update (for EDIT)
      const showBookings = this.showBookings(); // trigger when showBookings is checked, only in VIEW Mode
      const eventId = this.eventIdFromMode;
      const eventType = this.eventTypeFromMode;

      if (!ready) return; // check calendarComponent is ready, if not stop the code, then wait for trigger again
      if (eventId === undefined || eventType === undefined) return;

      this.logger.debug('[FullCalendarView] Initing calendar data');

      untracked(() => {
        this.syncCalendarTimeZone();
        this.refreshCalendarData();
      });

    });
  }

  private syncCalendarTimeZone() {
    const selectedTimeZone = this.selectedTimeZone();
    const currentTimeZone = this.calendarApi.getOption('timeZone');

    if (currentTimeZone !== selectedTimeZone) {
      this.calendarApi.setOption('timeZone', selectedTimeZone);
    }

    this.logger.debug('[FullCalendarView] Calendar timezone updated');
  }

  private refreshCalendarData() {
    if (!this.calendarReady()) return;
    if (this.eventIdFromMode === undefined || this.eventTypeFromMode === undefined) return;

    this.initCalendarData(this.filteredSlots(), this.eventIdFromMode, this.eventTypeFromMode, this.filteredOrganizerBookingList(), this.slotBookedTimesForEdit());
    this.logger.debug('[FullCalendarView] Calendar data loaded');
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.mode == 'VIEW' && (changes['eventId'] || changes['eventType'])) {
      this.logger.debug('[FullCalendarView] Changes detected for VIEW mode');
      if (!this.eventId || !this.eventType) return;

      this.eventIdFromMode = this.eventId;
      this.eventTypeFromMode = this.eventType;
      this.bookingService.triggerRefreshForOrganizerBookingListByEventId(this.eventIdFromMode);
      // slot id provided, get single slot
      if (this.slotId != null) {
        this.logger.debug('[FullCalendarView] Loading single slot data');
        this.logger.debug('[FullCalendarView] Sending bookingService.triggerRefreshForOrganizerBookingListBySlotId request');
        this.bookingService.triggerRefreshForOrganizerBookingListBySlotId(this.slotId);
        this.logger.debug('[FullCalendarView] Sending slotService.getSlotByIdAndEventId request');
        this.slotService.getSlotByIdAndEventId(this.eventId, this.slotId)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (res) => {
              this.singleSlot.set(res);
            },
            error: () => {
            }
          });
      } else { // slot id not provided, get slotList
        this.logger.debug('[FullCalendarView] Loading multiple slot data');
        this.singleSlot.set(null);
        this.logger.debug('[FullCalendarView] Sending slotService.triggerRefreshForSlotListByEventId request');
        this.slotService.triggerRefreshForSlotListByEventId(this.eventId);
      }
    }

    if (this.mode == 'EDIT' && (changes['slot'] || changes['invitation'])) {
      this.logger.debug('[FullCalendarView] Changes detected for EDIT mode');
      if (!this.slot || !this.invitation) return;

      this.editInitialViewApplied = false;

      this.eventIdFromMode = this.invitation?.event.id;
      this.eventTypeFromMode = this.invitation?.event.eventType;
      this.logger.debug('[FullCalendarView] Sending bookingService.triggerRefreshForSlotBookedTimesBySlotId request');
      this.bookingService.triggerRefreshForSlotBookedTimesBySlotId(this.slot.id);

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
    this.setUpCalendarOption();
    this.setUpCalendarOptionForViewMode();
    this.calendarReady.set(true);
  }

  private setUpCalendarOption() {
    this.logger.debug('[FullCalendarView] Setup calendar option');
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

    this.calendarApi.setOption('datesSet', (info) => {
      const sameRange =
        this.visibleRangeStart?.getTime() === info.start.getTime() &&
        this.visibleRangeEnd?.getTime() === info.end.getTime();

      this.visibleRangeStart = info.start;
      this.visibleRangeEnd = info.end;

      if (!sameRange) {
        setTimeout(() => this.refreshCalendarData(), 0);
      }
    });
  }
  private setUpCalendarOptionForViewMode() {
    if (this.mode != 'VIEW') return;
    this.logger.debug('[FullCalendarView] Setup calendar option for VIEW mode');

    this.calendarApi.setOption('eventClick', (info) => {
      const timeZone = this.selectedTimeZone();
      const start = info.event.start
        ? moment.tz(info.event.start, timeZone).format('YYYY-MM-DD hh:mm a Z')
        : 'N/A';
      const end = info.event.end
        ? moment.tz(info.event.end, timeZone).format('YYYY-MM-DD hh:mm a Z')
        : 'N/A';

      this.notificationService.info(
        `Event Name: TO ADD LATER\n` +
        `Slot Name: ${info.event.title}\n` +
        `Id: ${info.event.id}\n` +
        `Start Time: ${start}\n` +
        `End Time: ${end}`
      )
    });
    this.calendarApi.setOption('headerToolbar', this.isMobileCalendarView()
      ? {
        left: 'prev,next,today',
        center: 'title',
        right: 'timeGridWeek,timeGridDay'
      }
      : {
        left: 'prev,next,today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay'
      }
    );
  }

  private initCalendarData(
    slots: SlotResponseDto[],
    eventId: number,
    eventType: EventTypeModel,
    organizerBookingList: OrganizerBookingResponseDto[],
    slotBookedTimes: SlotBookedTimeResponseDto[]
  ) {

    if (this.mode == "VIEW") {
      let eventInputList: EventInput[] = this.mapSlotToCalendarEventForViewMode(slots, eventId, eventType, organizerBookingList);

      this.calendarApi.batchRendering(() => {
        this.calendarApi.removeAllEvents();
        this.calendarApi.addEventSource(eventInputList);
      });
    } else if (this.mode == "EDIT") {
      const slot = slots[0]; //EDIT only one slot will be choosen for process, convert it to one slot
      if (!slot) return;
      this.setUpCalendarOptionForEditMode(slot, eventType);
      let eventInputList: EventInput[] = this.mapSlotToCalendarEventForEditMode(slot, eventId, eventType, slotBookedTimes);
      this.calendarApi.batchRendering(() => {
        this.calendarApi.removeAllEvents();
        this.calendarApi.addEventSource(eventInputList);
      });
    } else {
      this.logger.error('[FullCalendarView] Unknown calendar mode');
    }
  }

  // core conversion
  private mapSlotToCalendarEventForViewMode(slotList: SlotResponseDto[], eventId: number, eventType: EventTypeModel, organizerBookingList: OrganizerBookingResponseDto[]): EventInput[] {

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
        this.logger.debug('[FullCalendarView] Mapping calendar event for VIEW mode and FIXED event');
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
        this.logger.debug('[FullCalendarView] Mapping calendar event for VIEW mode and FLEXIBLE event');
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
        if (!this.visibleRangeStart || !this.visibleRangeEnd) continue;
        this.logger.debug('[FullCalendarView] Mapping calendar event for VIEW mode and BUSINESS event');
        const businessTz = slot.businessTimeZone;

        const businessRangeStart = moment(this.visibleRangeStart)
          .tz(businessTz)
          .subtract(1, 'day')
          .startOf('day');

        const businessRangeEnd = moment(this.visibleRangeEnd)
          .tz(businessTz)
          .add(1, 'day')
          .endOf('day');

        let businessDate = businessRangeStart.clone();

        while (businessDate.isSameOrBefore(businessRangeEnd, 'day')) {
          const businessDayOfWeek = businessDate.day();
          const ranges = slot.businessDaysHours[businessDayOfWeek] ?? [];
          // const sortedRanges = [...ranges].sort((a, b) =>
          //   a.open.localeCompare(b.open)
          // );

          // const lastRange = sortedRanges[sortedRanges.length - 1];

          ranges.forEach((range, rangeIndex) => {
            const startParts = range.open.split(':').map(Number);
            const endParts = range.close.split(':').map(Number);

            // const isLastRange = range.open === lastRange.open && range.close === lastRange.close;

            const rangeStart = businessDate
              .clone()
              .hour(startParts[0])
              .minute(startParts[1])
              .second(0)
              .millisecond(0);

            let rangeEnd = businessDate
              .clone()
              .hour(endParts[0])
              .minute(endParts[1])
              .second(0)
              .millisecond(0);

            // Handles 22:00 -> 00:00 / 23:00 -> 01:00 overnight range.
            if (!rangeEnd.isAfter(rangeStart)) {
              rangeEnd = rangeEnd.add(1, 'day');
            }

            const startTitle = rangeStart.clone().tz(this.selectedTimeZone()).format('hh:mm a');
            const endTitle = rangeEnd.clone().tz(this.selectedTimeZone()).format('hh:mm a');

            events.push({
              ...baseEvent,
              id: `event-${eventId}-slot-${slot.id}-${businessDate.format('YYYYMMDD')}-rangeIndex-${rangeIndex}`,
              start: rangeStart.toDate(),
              end: rangeEnd.toDate(),
              extendedProps: {
                ...baseEvent.extendedProps,
                slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
                slotIntervalMinutes: slot.slotIntervalMinutes,
                businessTimeZone: slot.businessTimeZone,
                businessAllowOt: slot.businessAllowOt
              }
            });
          });

          businessDate.add(1, 'day');
        }
      }
    }
    if (this.showBookings() && organizerBookingList && organizerBookingList.length) {
      organizerBookingList.forEach(booking => {
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
          title: ` - Booked by: ${booking.attendeeLastName} ${booking.attendeeLastName}, Email: ${booking.attendeeEmail}`,
          id: `bookingId-${booking.bookingId}`,
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
    this.logger.debug('[FullCalendarView] Setup calendar option for EDIT mode');
    this.calendarApi.setOption('displayEventTime', false);

    if (eventType == EventTypeModel.FLEXIBLE) {

      if (slot.slotFrequencyIntervalMinutes == null) return;

      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotDuration = this.covertToSlotDuration(slotFrequencyIntervalMinutes);

      this.calendarApi.setOption('slotDuration', slotDuration);
      this.calendarApi.setOption('snapDuration', slotDuration);

      this.calendarApi.setOption('headerToolbar', this.isMobileCalendarView()
        ? {
          left: 'prev,next',
          center: 'title',
          right: 'today'
        }
        : {
          left: 'prev,next,today',
          center: 'title',
          right: 'timeGridWeek,timeGridDay'
        }
      );

      this.applyInitialEditView(slot, eventType);
      this.setUpCalendarClickForEditMode(slot);
    }

    if (eventType == EventTypeModel.BUSINESS) {

      if (slot.slotFrequencyIntervalMinutes == null) return;

      if (this.editInitialViewApplied) return;

      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotDuration = this.covertToSlotDuration(slotFrequencyIntervalMinutes);

      this.calendarApi.setOption('slotDuration', slotDuration);
      this.calendarApi.setOption('snapDuration', slotDuration);

      this.calendarApi.setOption('headerToolbar', this.isMobileCalendarView()
        ? {
          left: 'prev,next',
          center: 'title',
          right: 'today'
        }
        : {
          left: 'prev,next,today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay'
        }
      );

      this.applyInitialEditView(slot, eventType);
      this.setUpCalendarClickForEditMode(slot);
    }
  }

  private applyInitialEditView(slot: SlotResponseDto, eventType: EventTypeModel) {
    if (this.editInitialViewApplied) return;
    this.logger.debug('[FullCalendarView] Setup calendar initial view');
    if (eventType == EventTypeModel.FLEXIBLE) {
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
      this.calendarApi.changeView(this.isMobileCalendarView() ? 'timeGridDay' : viewType, displayDate);

      this.editInitialViewApplied = true;
    }

    if (eventType == EventTypeModel.BUSINESS) {
      this.calendarApi.changeView(this.isMobileCalendarView() ? 'timeGridDay' : 'timeGridWeek');
      this.editInitialViewApplied = true;
    }
  }

  private isMobileCalendarView(): boolean {
    return window.innerWidth < 768;
  }

  private setUpCalendarClickForEditMode(slot: SlotResponseDto) {
    this.logger.debug('[FullCalendarView] Setup calendar event click option for EDIT mode');

    this.calendarApi.setOption('dateClick', undefined);

    this.calendarApi.setOption('eventClick', (info) => {
      if (info.event.extendedProps['isSlotBooked']) {
        this.notificationService.warning('Booked by others');
        return;
      }

      if (info.event.extendedProps['isPast']) {
        this.notificationService.warning('This time is no longer available');
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
        this.closeCalendar();
      }

    });
  }

  mapSlotToCalendarEventForEditMode(
    slot: SlotResponseDto,
    eventId: number,
    eventType: EventTypeModel,
    slotBookedTimes: SlotBookedTimeResponseDto[]
  ): EventInput[] {
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

    // const now = moment();

    // function isPastSlot(slotEnd: moment.Moment){
    //   slotEnd.isSameOrBefore(now);
    // } 

    if (eventType == EventTypeModel.FIXED) {
      this.logger.debug('[FullCalendarView] Mapping calendar event for EDIT mode and FIXED event');
      const start = moment(slot.slotStartTime, this.selectedTimeZone());
      const end = moment(slot.slotEndTime, this.selectedTimeZone())
      const isSlotBooked = this.isSlotBooked(start.clone().toDate(), end.clone().toDate(), slotBookedTimes);
      const isPast = start.isSameOrBefore(moment());
      events.push({
        ...baseEvent,
        title: `${start.format('hh:mm a')} - ${end.format('hh:mm a')} - ${slot.slotName}`,
        id: `event-${eventId}-slot-${slot.id}`,
        start: slot.slotStartTime,
        end: slot.slotEndTime,
        backgroundColor: isPast ? '#686868b7' : isSlotBooked ? '#ff4d4f' : '#3788d8',
        borderColor: isPast ? '#686868b7' : isSlotBooked ? '#ff4d4f' : '#3788d8',
        textColor: isPast ? '#000000' : isSlotBooked ? '#000000' : '#ffffff',
        extendedProps: {
          ...baseEvent.extendedProps,
          isSlotBooked: isSlotBooked,
        }
      });
    }

    if (eventType == EventTypeModel.FLEXIBLE) {
      if (!slot.flexibleDaysHours?.length) return events;
      this.logger.debug('[FullCalendarView] Mapping calendar event for EDIT mode and FLEXIBLE event');
      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotIntervalMinutes = slot.slotIntervalMinutes;

      if (!slotFrequencyIntervalMinutes || !slotIntervalMinutes) return events;

      slot.flexibleDaysHours.forEach((range, index) => {

        const startZdt = moment(range.open);
        const endZdt = moment(range.close);

        let current = startZdt.clone();
        let i = 0;

        while (current.clone().add(slotIntervalMinutes, 'minutes').isSameOrBefore(endZdt)) {
          const selectEnd = current.clone().add(slotFrequencyIntervalMinutes, 'minutes');
          const startTitle = current.clone().tz(this.selectedTimeZone()).format('hh:mm a');
          const endTitle = current.clone().tz(this.selectedTimeZone()).add(slot.slotIntervalMinutes, 'minutes').format('hh:mm a');
          const slotEnd = current.clone().add(slot.slotIntervalMinutes, 'minutes');

          const isPast = current.isSameOrBefore(moment());
          const isSlotBooked = this.isSlotBooked(current.clone().toDate(), slotEnd.clone().toDate(), slotBookedTimes);


          events.push({
            ...baseEvent,
            title: `${startTitle} - ${endTitle} - ${slot.slotName}`,
            id: `event-${eventId}-slot-${slot.id}-rangeIndex-${index}-${i}`,
            start: current.toDate(),
            end: selectEnd.toDate(),
            backgroundColor: isPast ? '#686868b7' : isSlotBooked ? '#ff4d4f' : '#3788d8',
            borderColor: isPast ? '#686868b7' : isSlotBooked ? '#ff4d4f' : '#3788d8',
            textColor: isPast ? '#000000' : isSlotBooked ? '#000000' : '#ffffff',
            extendedProps: {
              ...baseEvent.extendedProps,
              slotIntervalMinutes: slotIntervalMinutes,
              slotFrequencyIntervalMinutes: slotFrequencyIntervalMinutes,
              isSlotBooked: isSlotBooked,
              isPast: isPast,
            }
          });

          current = selectEnd;
          i++;
        }
      });
    }

    if (eventType == EventTypeModel.BUSINESS) {

      this.logger.debug('[FullCalendarView] Mapping calendar event for EDIT mode and BUSINESS event');

      if (!slot.businessDaysHours
        || !slot.businessTimeZone
        || Object.keys(slot.businessDaysHours).length === 0) return events;

      const businessTz = slot.businessTimeZone;
      const slotFrequencyIntervalMinutes = slot.slotFrequencyIntervalMinutes;
      const slotIntervalMinutes = slot.slotIntervalMinutes;

      if (!slotFrequencyIntervalMinutes || !slotIntervalMinutes) return events;

      if (!this.visibleRangeStart || !this.visibleRangeEnd) return events;

      const businessRangeStart = moment(this.visibleRangeStart)
        .tz(businessTz)
        .subtract(1, 'day')
        .startOf('day');

      const businessRangeEnd = moment(this.visibleRangeEnd)
        .tz(businessTz)
        .add(1, 'day')
        .endOf('day');

      let businessDate = businessRangeStart.clone();

      while (businessDate.isSameOrBefore(businessRangeEnd, 'day')) {
        const businessDayOfWeek = businessDate.day();
        const ranges = slot.businessDaysHours[businessDayOfWeek] ?? [];
        const sortedRanges = [...ranges].sort((a, b) =>
          a.open.localeCompare(b.open)
        );
        const lastRange = sortedRanges[sortedRanges.length - 1];

        ranges.forEach((range, rangeIndex) => {

          const isLastRange = range.open === lastRange.open && range.close === lastRange.close;

          let current = businessDate
            .clone()
            .hour(Number(range.open.split(':')[0]))
            .minute(Number(range.open.split(':')[1]))
            .second(0)
            .millisecond(0);

          const rangeEnd = businessDate
            .clone()
            .hour(Number(range.close.split(':')[0]))
            .minute(Number(range.close.split(':')[1]))
            .second(0)
            .millisecond(0);

          if (rangeEnd.isBefore(current)) {
            rangeEnd.add(1, 'day');
          }

          let i = 0;

          while (
            (slot.businessAllowOt && isLastRange)
              ? current.isSameOrBefore(rangeEnd)
              : current.clone().add(slotIntervalMinutes, 'minutes').isSameOrBefore(rangeEnd)
          ) {

            const eventEnd = current.clone().add(slotFrequencyIntervalMinutes, 'minutes');
            const bookingEnd = current.clone().add(slotIntervalMinutes, 'minutes');

            const startTitle = current.clone().tz(this.selectedTimeZone()).format('hh:mm a');
            const endTitle = bookingEnd.clone().tz(this.selectedTimeZone()).format('hh:mm a');
            const isPast = current.isSameOrBefore(moment());

            events.push({
              ...baseEvent,
              title: `${startTitle} - ${endTitle} - ${slot.slotName}`,
              id: `event-${eventId}-slot-${slot.id}-${businessDate.format('YYYYMMDD')}-rangeIndex-${rangeIndex}-${i}`,
              start: current.toDate(),
              end: eventEnd.toDate(),
              display: 'background',
              color: isPast ? '#686868b7' : '#5d9edf',
              className: isPast ? 'slot-item past-slot-item' : 'slot-item',
              extendedProps: {
                ...baseEvent.extendedProps,
                slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
                slotIntervalMinutes: slot.slotIntervalMinutes,
                businessTimeZone: slot.businessTimeZone,
                businessAllowOt: slot.businessAllowOt,
                isPast: isPast,
              }
            });

            current = eventEnd;
            i++;
          }
        });

        businessDate.add(1, 'day');
      }

      slotBookedTimes.forEach((bookedTime, index) => {
        const startTime = moment(bookedTime.bookedStartTime).clone().toDate();
        // const endTime = moment(booking.bookedStartTime).clone().add(slot.slotIntervalMinutes, 'minutes').toDate();
        const endTime = moment(bookedTime.bookedEndTime).clone().toDate();
        const startTitle = moment(bookedTime.bookedStartTime).clone().tz(this.selectedTimeZone()).format('hh:mm a');
        // const endTitle = moment(booking.bookedStartTime).clone().tz(this.selectedTimeZone()).add(slot.slotIntervalMinutes, 'minutes').format('hh:mm a');
        const endTitle = moment(bookedTime.bookedEndTime).clone().tz(this.selectedTimeZone()).format('hh:mm a');
        events.push({
          title: `BOOKED: ${startTitle} - ${endTitle} - ${slot.slotName}`,
          id: `bookedTime-${slot.id}-${index}`,
          start: startTime,
          end: endTime,
          display: 'background',
          color: '#fc6a6d',
          className: 'booked-slot-item',
          extendedProps: {
            isSlotBooked: true
          }
        });

        const bookingStart = moment(bookedTime.bookedStartTime);
        const bookingDayStart = bookingStart.clone().startOf('day');

        const overlapMinutes = slotIntervalMinutes - slotFrequencyIntervalMinutes;

        if (overlapMinutes > 0) {
          const businessDay = bookingStart.clone().tz(businessTz).day();
          const businessRanges = slot.businessDaysHours?.[businessDay] ?? [];

          const matchingRange = businessRanges.find(range => {
            const rangeStart = bookingStart
              .clone()
              .tz(businessTz)
              .set({
                hour: Number(range.open.split(':')[0]),
                minute: Number(range.open.split(':')[1]),
                second: 0,
                millisecond: 0
              });

            const rangeEnd = bookingStart
              .clone()
              .tz(businessTz)
              .set({
                hour: Number(range.close.split(':')[0]),
                minute: Number(range.close.split(':')[1]),
                second: 0,
                millisecond: 0
              });

            return bookingStart.clone().tz(businessTz).isSameOrAfter(rangeStart)
              && bookingStart.clone().tz(businessTz).isBefore(rangeEnd);
          });

          const businessRangeStart = matchingRange
            ? bookingStart
              .clone()
              .tz(businessTz)
              .set({
                hour: Number(matchingRange.open.split(':')[0]),
                minute: Number(matchingRange.open.split(':')[1]),
                second: 0,
                millisecond: 0
              })
            : bookingDayStart;

          const earliestBlockedStart = moment.max(
            bookingStart.clone().subtract(overlapMinutes, 'minutes'),
            bookingDayStart,
            businessRangeStart
          );

          let currentBlocked = earliestBlockedStart.clone();

          while (currentBlocked.isBefore(bookingStart)) {
            const blockedEnd = moment.min(
              currentBlocked.clone().add(slotFrequencyIntervalMinutes, 'minutes'),
              bookingStart
            );

            if (!blockedEnd.isAfter(currentBlocked)) {
              break;
            }

            const blockedStartTitle = currentBlocked
              .clone()
              .tz(this.selectedTimeZone())
              .format('hh:mm a');

            const blockedEndTitle = blockedEnd
              .clone()
              .tz(this.selectedTimeZone())
              .format('hh:mm a');

            events.push({
              title: `BLOCKED: ${blockedStartTitle} - ${blockedEndTitle} - ${slot.slotName}`,
              id: `blocked-overlap-${slot.id}-${index}-${currentBlocked.valueOf()}`,
              start: currentBlocked.toDate(),
              end: blockedEnd.toDate(),
              display: 'background',
              color: '#fc6a6d',
              className: 'blocked-overlap-slot',
              extendedProps: {
                isSlotBooked: true,
                isBlockedOverlap: true,
                blockedReason: 'overlap-before-booking'
              }
            });

            currentBlocked.add(slotFrequencyIntervalMinutes, 'minutes');
          }
        }

      });
    }

    return events;
  }

  private isSlotBooked(slotStart: Date, slotEnd: Date, bookedTimes: SlotBookedTimeResponseDto[]): boolean {
    if (!slotStart || !slotEnd || !bookedTimes) return false;

    return bookedTimes.some(b => {
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
    const updatedFilterOrganizerBookingList = new Set(this.selectedOrganizerBookingListForView());
    if (checked) {
      updatedFilterOrganizerBookingList.add(slotId);
    } else {
      updatedFilterOrganizerBookingList.delete(slotId);
    }
    this.selectedOrganizerBookingListForView.set(updatedFilterOrganizerBookingList);
  }

  toggleShowBookings(checked: boolean) {
    this.showBookings.set(checked);
  }


  closeCalendar() {
    this.close.emit();
  }
}
