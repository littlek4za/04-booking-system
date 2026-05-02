import { Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { BookingResponseDto } from '../dtos/booking-response-dto';
import { toSignal } from '@angular/core/rxjs-interop';
import { BookingService } from '../booking-service';
import { DatePipe, NgClass } from '@angular/common';

@Component({
  selector: 'app-booking-manager-dashboard',
  imports: [DatePipe, NgClass],
  templateUrl: './booking-manager-dashboard.html',
  styleUrl: './booking-manager-dashboard.css',
})
export class BookingManagerDashboard implements OnChanges {

  private bookingService = inject(BookingService);

  bookingListBySlotId = toSignal(this.bookingService.bookingListBySlotId$, { initialValue: [] as BookingResponseDto[] });

  @Input() slotId!: number;
  @Input() slotName!: string | null;
  @Input() eventName!: string | null;
  @Output() close = new EventEmitter<void>();

  statusClassMap: Record<string, string> = {
    UPCOMING: 'bg-primary',
    ONGOING: 'bg-success',
    DELETED: 'bg-danger',
    EXPIRED: 'bg-warning text-dark'
  };

  constructor() { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['slotId']) {
      this.bookingService.triggerRefreshForBookingListBySlotId(this.slotId);
    }
  }

  closeDashboard() {
    this.close.emit();
  }

  comfirmDeleteBooking(slotId:number, bookingId: number) {
    this.bookingService.softDeleteBookingAsOrganizer(slotId,bookingId).subscribe({
      next:(res)=> {
        console.log("Delete booking succesful");
        this.bookingService.triggerRefreshForBookingListBySlotId(this.slotId);
      },
      error:(err)=> {
        console.log("Delete unsucessful");
        alert("Delete unsucessful, please contact admin");
      },
    })
  }

}
