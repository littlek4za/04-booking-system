import { DatePipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { InvitationResponseDto } from '@features/invitations/dtos/invitation-response-dto';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';

@Component({
  selector: 'app-booking-confirmation-wizard',
  imports: [DatePipe],
  templateUrl: './booking-confirmation-wizard.html',
  styleUrl: './booking-confirmation-wizard.css',
})
export class BookingConfirmationWizard {

  @Output() close = new EventEmitter<void>();

  @Input() slot!: SlotResponseDto;
  @Input() invitation!: InvitationResponseDto;

  closeWizard() {
    this.close.emit();
  }

  bookSlot(slot: SlotResponseDto) {
    throw new Error('Method not implemented.');
  }
}

