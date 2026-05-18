import { Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { OrganizerBookingResponseDto } from '../dtos/booking-response-dto';
import { toSignal } from '@angular/core/rxjs-interop';
import { BookingService } from '../booking-service';
import { DatePipe, NgClass } from '@angular/common';
import { LoggerService } from '@core/services/logger-service';
import { SlotService } from '@features/slots/slot-service';

@Component({
  selector: 'app-booking-manager-dashboard',
  imports: [DatePipe, NgClass],
  templateUrl: './booking-manager-dashboard.html',
  styleUrl: './booking-manager-dashboard.css',
})
export class BookingManagerDashboard implements OnChanges {

  private bookingService = inject(BookingService);
  private slotService = inject(SlotService);

  organizerBookingListBySlotId = toSignal(this.bookingService.organizerBookingListBySlotId$, { initialValue: [] as OrganizerBookingResponseDto[] });

  @Input() slotId!: number;
  @Input() slotName!: string | null;
  @Input() eventName!: string | null;
  @Input() eventId!: number | null;
  @Output() close = new EventEmitter<void>();

  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  constructor(private logger: LoggerService) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['slotId']) {
      this.logger.debug(`[BookingManagerDashboard] Slot id changes detected, sending bookingService.triggerRefreshForOrganizerBookingListBySlotId request`);
      this.bookingService.triggerRefreshForOrganizerBookingListBySlotId(this.slotId);
    }
  }

  closeDashboard() {
    this.close.emit();
  }

  comfirmDeleteBooking(slotId: number, bookingId: number) {
    if (confirm("Confirm Delete Booking?")) {
      this.logger.debug(`[BookingManagerDashboard] Sending bookingService.softDeleteBookingAsOrganizer request`);
      this.bookingService.softDeleteBookingAsOrganizer(slotId, bookingId).subscribe({
        next: () => {
          this.bookingService.triggerRefreshForOrganizerBookingListBySlotId(this.slotId);
          if(this.eventId){
            this.slotService.triggerRefreshForSlotListByEventId(this.eventId);
          }
        },
        error: () => {
          alert("Delete unsucessful, please contact admin");
        },
      });
    }
  }
}
