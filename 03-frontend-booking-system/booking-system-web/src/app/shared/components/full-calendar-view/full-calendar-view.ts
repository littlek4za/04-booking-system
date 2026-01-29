import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CalendarSlot } from '@shared/model/calendar-slot';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { Calendar, CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import { CommonModule } from '@angular/common';
import interactionPlugin, { DateClickArg } from '@fullcalendar/interaction';

@Component({
  selector: 'app-full-calendar-view',
  imports: [CommonModule, FullCalendarModule],
  templateUrl: './full-calendar-view.html',
  styleUrl: './full-calendar-view.css',
})
export class FullCalendarView {
  @Input() slots: CalendarSlot[] = [];
  @Input() initialView: string = 'dayGridMonth';
  @Input() viewDate: Date = new Date();

  @Output() eventClick = new EventEmitter<CalendarSlot>();
  @Output() dateChange = new EventEmitter<Date>();

  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;

  calendarOptions: CalendarOptions = {
    initialView: this.initialView,
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    dateClick: (info) => alert('date click! ' + info.dateStr),
    weekends: false,
    events: [
      {title: 'event 1', date: '2026-01-29'},
      { title: 'event 2', date: '2026-01-30' }
    ],
    eventClick: (info) => alert('event click! ' + info.event),
    // datesSet: (info) => alert('dateSet click! ' + info.start),
    headerToolbar: {
      left: 'prev,next',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay' 
    }
  };

// event
//   {
//   id?: string;
//   title?: string;
//   start?: Date | string;
//   end?: Date | string;
//   allDay?: boolean;
//   color?: string;
//   backgroundColor?: string;
//   borderColor?: string;
//   textColor?: string;
//   editable?: boolean;
//   startEditable?: boolean;
//   durationEditable?: boolean;
//   display?: 'auto' | 'block' | 'list-item' | 'background';
//   extendedProps?: any;
// slotId
// maxBooking
// eventType
// capacity
// price
// }

  private get calendarApi() {
    return this.calendarComponent.getApi();
  }

  toggleWeekends() {
    this.calendarOptions = {
      ...this.calendarOptions,
      weekends: !this.calendarOptions.weekends
    };
  }

}
