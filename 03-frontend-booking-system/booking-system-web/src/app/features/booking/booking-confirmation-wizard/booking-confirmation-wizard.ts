import { DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, NgZone, OnDestroy, OnInit, Output } from '@angular/core';
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
import moment from 'moment';
import { Subject, takeUntil } from 'rxjs';
import { GuestBookingCreateInitRequestDto } from '../dtos/guest-booking-create-init-request-dto';
import { GuestBookingCreateAccessRequestDto } from '../dtos/guest-booking-create-access-request-dto';

declare var grecaptcha: any;

@Component({
  selector: 'app-booking-confirmation-wizard',
  imports: [DatePipe, ReactiveFormsModule, FullCalendarView],
  templateUrl: './booking-confirmation-wizard.html',
  styleUrl: './booking-confirmation-wizard.css',
})
export class BookingConfirmationWizard implements OnInit, OnDestroy {

  // component IO
  @Output() close = new EventEmitter<void>();
  @Input() slot!: SlotResponseDto;
  @Input() invitation!: InvitationResponseDto;

  // field
  guestForm!: FormGroup;
  token: string | null = null;
  authTokenPayload: AuthTokenPayload | null = null;
  bookingResponseDto: BookingResponseDto | null = null;
  timeZone: string = 'local';

  // field Model
  readonly EventTypeModel = EventTypeModel;

  // show or hide component
  showCalendar: boolean = false;

  // Recaptcha Field
  private captchaWidgetId: number | null = null;
  captchaToken: string | null = null;
  isBookingInProgress: boolean = false;
  // for Recaptcha auto submit
  private pendingEmail: string | null = null

  // Destroy Field
  private destroy$ = new Subject<void>();

  constructor(private bookingService: BookingService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.authTokenPayload = this.authService.getAuthTokenInfo();
    this.initGuestForm();
    this.populateFormBaseOnEventType();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

  get loggedInUser() {
    return this.authService.hasValidToken() && this.authService.isLoggedInUser();
  }

  private renderCaptcha(bookingRequestDto: BookingRequestDto) {

    if (this.captchaWidgetId !== null) {
      grecaptcha.reset(this.captchaWidgetId);
      return;
    }

    this.captchaWidgetId = grecaptcha.render('captcha-container', {
      'sitekey': '6LdkcLUsAAAAAJMLxLQMGoW3hZ0acjtL7-RdotBu',
      'callback': (response: string) => {

        console.log('Token:', response);
        this.captchaToken = response;

        // AUTO CONTINUE HERE captcha->issuetoken
        if (!this.captchaToken) return;
        if (!this.pendingEmail) return;

        console.log("moving to issueToken");

        if (this.pendingEmail) {
          this.isBookingInProgress = true;
          this.cdr.detectChanges();
          this.issueGuestBookingCreateAccessToken(
            bookingRequestDto,
            this.pendingEmail,
            this.captchaToken,
            this.invitation.id,
            this.slot.id
          );
        }

      },
      'expiry-callback': () => {
        this.captchaToken = null;
      }
    });
  }

  bookSlot(slot: SlotResponseDto) {

    if (this.isBookingInProgress) return;

    this.guestForm.markAllAsTouched();
    if (this.guestForm.invalid) {
      console.log('form invalid');
      return;
    }


    const bookingRequestDto = this.buildBookingRequestDto();

    console.log("BookingRequestDto", bookingRequestDto);

    if (this.loggedInUser) {
      this.createBooking(bookingRequestDto);
    } else {
      let requestDto = new GuestBookingCreateInitRequestDto();
      requestDto.email = this.guestForm?.value.email;

      this.bookingService.initGuestBookingCreateAccess(requestDto)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (res) => {
            this.pendingEmail = this.guestForm?.value.email;

            if (res.captchaRequired) {
              console.log("moving to renderCaptcha");
              this.renderCaptcha(bookingRequestDto);
              return;
            }
            console.log("moving to issueToken");

            if (this.pendingEmail) {
              this.isBookingInProgress = true;
              this.issueGuestBookingCreateAccessToken(bookingRequestDto, this.pendingEmail, null, this.invitation.id, this.slot.id);
            }
          },
          error: (err) => {
            console.log(err);
            this.isBookingInProgress = false;
          }
        })
    }

  }

  private buildBookingRequestDto(): BookingRequestDto {
    let bookingRequestDto = new BookingRequestDto
    if (!this.loggedInUser) {
      bookingRequestDto.email = this.guestForm?.value.email;
      bookingRequestDto.firstName = this.guestForm?.value.firstName;
      bookingRequestDto.lastName = this.guestForm?.value.lastName;
    }

    if (this.invitation.event.eventType == EventTypeModel.FIXED) {
      bookingRequestDto.invitationId = this.invitation.id;
    } else if (this.invitation.event.eventType == EventTypeModel.BUSINESS || this.invitation.event.eventType == EventTypeModel.FLEXIBLE) {
      bookingRequestDto.invitationId = this.invitation.id;
      bookingRequestDto.bookedStartTime = this.guestForm.get('choosenStartTime')?.value;
    } else {
      console.error("Event Type Error", this.invitation);
      alert('Event type error. Please contact administrator')
    }
    return bookingRequestDto;
  }

  private createBooking(bookingRequestDto: BookingRequestDto) {
    console.log('BookingRequestDto', bookingRequestDto);
    this.bookingService.createBooking(bookingRequestDto, this.slot.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.bookingResponseDto = res;
          console.log("Booking created successfully");
          alert('Booking created successfully');
          this.isBookingInProgress = false;
          this.closeWizard();
        },
        error: (err) => {
          console.log("Booking create unsuccesful");
          this.isBookingInProgress = false;
        },
      })
  }

  private issueGuestBookingCreateAccessToken(bookingRequestDto: BookingRequestDto, email: string, captchaToken: string | null, invitationId: number, slotId: number) {
    let requestDto = new GuestBookingCreateAccessRequestDto();
    requestDto.email = email;
    requestDto.captchaToken = captchaToken;
    requestDto.invitationId = invitationId;
    requestDto.slotId = slotId;

    console.log('GuestBookingCreateAccessRequestDto', requestDto);

    this.bookingService.issueGuestBookingCreateAccessToken(requestDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.authService.storeGuestToken(res);
          this.createBooking(bookingRequestDto);
          this.captchaToken = null;
          this.pendingEmail = null;
          if (this.captchaWidgetId !== null) {
            grecaptcha.reset(this.captchaWidgetId);
          }
        },
        error: (err) => {
          alert("Access denied");
          console.log(err);
          this.captchaToken = null;
          this.pendingEmail = null;
          this.isBookingInProgress = false;
          if (this.captchaWidgetId !== null) {
            grecaptcha.reset(this.captchaWidgetId);
          }
        }
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

  processSelectedStartTime($event: Date) {

    const startTimeIso = moment($event).tz(this.timeZone).toISOString();
    const endTimeIso = moment(startTimeIso)
      .add(this.slot.slotIntervalMinutes, 'minutes')
      .toISOString();

    this.guestForm.patchValue({
      choosenStartTime: startTimeIso,
      choosenEndTime: endTimeIso
    }, { emitEvent: true });

    this.guestForm.updateValueAndValidity({ emitEvent: true });
  }

  processSelectedTimeZone(tz: string) {
    this.timeZone = tz;
    const startTime = this.guestForm.get('choosenStartTime')?.value;
    const endTime = this.guestForm.get('choosenEndTime')?.value;

    if (startTime) {
      const startTimeIso = moment(startTime).tz(this.timeZone).toISOString();
      this.guestForm.get('choosenStartTime')?.patchValue(startTimeIso, { emitEvent: false });
    }

    if (endTime) {
      const endTimeIso = moment(endTime).tz(this.timeZone).toISOString();
      this.guestForm.get('choosenEndTime')?.patchValue(endTimeIso, { emitEvent: false });
    }
    this.guestForm.updateValueAndValidity({ emitEvent: false });
  }

}

