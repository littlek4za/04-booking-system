import { DatePipe } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { InvitationResponseDto } from '@features/invitations/dtos/invitation-response-dto';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';
import { BookingService } from '../booking-service';
import { BookingRequestDto } from '../dtos/booking-request-dto';
import { AuthService } from '@features/auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthTokenPayload } from '@features/auth/dtos/auth-token-payload';
import { BookingResponseDto } from '../dtos/booking-response-dto';
import { validateStartAndEndTimeBaseOnEvent } from '@shared/validators/custom-validator';
import { FullCalendarView } from '@shared/components/full-calendar-view/full-calendar-view';

@Component({
  selector: 'app-booking-confirmation-wizard',
  imports: [DatePipe, ReactiveFormsModule, FullCalendarView],
  templateUrl: './booking-confirmation-wizard.html',
  styleUrl: './booking-confirmation-wizard.css',
})
export class BookingConfirmationWizard implements OnInit {

  // component IO
  @Output() close = new EventEmitter<void>();
  @Input() slot!: SlotResponseDto;
  @Input() invitation!: InvitationResponseDto;

  // field
  guestForm!: FormGroup;
  token: string | null = null;
  authTokenPayload: AuthTokenPayload | null = null;
  bookingResponseDto: BookingResponseDto | null = null;

  // field Model
  readonly EventTypeModel = EventTypeModel;

  // show or hide component
  showCalendar: boolean = false;

  constructor(private bookingService: BookingService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authTokenPayload = this.authService.getAuthTokenInfo();
    this.initGuestForm();
    this.populateFormBaseOnEventType();
  }


  initGuestForm() {
    this.guestForm = new FormGroup({
      email: new FormControl<string>("",
        [Validators.required,
        Validators.maxLength(255),
        Validators.pattern('^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$')]),
      firstName: new FormControl<string>("",
        [Validators.required,
        Validators.minLength(1),
        Validators.maxLength(100)]),
      lastName: new FormControl<string>("",
        [Validators.required,
        Validators.minLength(1),
        Validators.maxLength(100)]),
      choosenStartTime: new FormControl<string>("",
        [Validators.required]),
      choosenEndTime: new FormControl<string>("",
        [Validators.required]),
    }, { validators: validateStartAndEndTimeBaseOnEvent(this.slot, this.invitation) }
    );
  }

  populateFormBaseOnEventType() {
    if (this.invitation.event.eventType == EventTypeModel.FIXED) {
      this.guestForm.get('choosenStartTime')?.patchValue(this.slot.slotStartTime);
      this.guestForm.get('choosenEndTime')?.patchValue(this.slot.slotEndTime);
    }
  }



  bookSlot(slot: SlotResponseDto) {
    const bookingRequestDto = new BookingRequestDto;

    if (!this.authTokenPayload) {
      bookingRequestDto.email = this.guestForm?.value.email;
      bookingRequestDto.firstName = this.guestForm?.value.firstName;
      bookingRequestDto.lastName = this.guestForm?.value.lastName;
    }

    if (this.invitation.event.eventType == EventTypeModel.FIXED) {
      bookingRequestDto.slotId = slot.id;
      bookingRequestDto.invitationId = this.invitation.id;
    } else if (this.invitation.event.eventType == EventTypeModel.BUSINESS || this.invitation.event.eventType == EventTypeModel.FLEXIBLE) {
      bookingRequestDto.slotId = slot.id;
      bookingRequestDto.invitationId = this.invitation.id;
      bookingRequestDto.bookedStartTime = this.guestForm.get('choosenStartTime')?.value;
    } else {
      console.error("Event Type Error", this.invitation);
      alert('Event type error. Please contact administrator')
    }

    console.log("BookingRequestDto", bookingRequestDto);
    this.bookingService.createBooking(bookingRequestDto).subscribe({
      next: (res) => {
        this.bookingResponseDto = res;
        console.log("Booking created successfully");
        alert('Booking created successfully');
        this.closeWizard();
      },
      error: (err) => {
        console.log("Booking create unsuccesful");
      },
    })
  }

  closeWizard() {
    this.close.emit();
  }

  openCalendar() {
    this.showCalendar = true;
  }
  closeCalendar() {
    this.showCalendar = false;
  }

}

