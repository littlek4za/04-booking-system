import { DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, NgZone, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { InvitationResponseDto } from '@features/invitations/dtos/invitation-response-dto';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';
import { BookingService } from '../booking-service';
import { BookingRequestDto } from '../dtos/booking-request-dto';
import { AuthService } from '@features/auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthTokenPayload } from '@features/auth/dtos/auth-token-payload';
import { OrganizerBookingResponseDto } from '../dtos/booking-response-dto';
import { validateStartAndEndTimeBaseOnEvent } from '@shared/validators/custom-validator';
import { FullCalendarView } from '@shared/components/full-calendar-view/full-calendar-view';
import moment from 'moment';
import { Subject, takeUntil } from 'rxjs';
import { GuestBookingCreateInitRequestDto } from '../dtos/guest-booking-create-init-request-dto';
import { GuestBookingCreateAccessRequestDto } from '../dtos/guest-booking-create-access-request-dto';
import { Router } from '@angular/router';
import { LoggerService } from '@core/services/logger-service';
import { environment } from '../../../../environments/environment';

declare var grecaptcha: any;

@Component({
  selector: 'app-booking-confirmation-wizard',
  imports: [DatePipe, ReactiveFormsModule, FullCalendarView],
  templateUrl: './booking-confirmation-wizard.html',
  styleUrl: './booking-confirmation-wizard.css',
})
export class BookingConfirmationWizard implements OnInit, OnDestroy {

  @ViewChild('captchaContainer') private captchaContainer?: ElementRef<HTMLElement>;

  // component IO
  @Output() close = new EventEmitter<void>();
  @Input() slot!: SlotResponseDto;
  @Input() invitation!: InvitationResponseDto;

  // field
  guestForm!: FormGroup;
  token: string | null = null;
  authTokenPayload: AuthTokenPayload | null = null;
  organizerBookingResponseDto: OrganizerBookingResponseDto | null = null;
  timeZone: string = 'local';

  // field Model
  readonly EventTypeModel = EventTypeModel;

  // show or hide component
  showCalendar: boolean = false;

  // Recaptcha Field
  private captchaSiteKey = environment.captchaSiteKey;
  private captchaWidgetId: number | null = null;
  private accessInProgress: boolean = false;
  captchaToken: string | null = null;
  isSubmitInProgress: boolean = false;
  showCaptcha: boolean = false;

  // for Recaptcha auto submit
  private pendingEmail: string | null = null

  // Destroy Field
  private destroy$ = new Subject<void>();

  constructor(private bookingService: BookingService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private logger: LoggerService,
    private ngZone: NgZone
  ) { }

  ngOnInit(): void {
    this.authTokenPayload = this.authService.getAuthTokenInfo();
    this.initGuestForm();
    if (this.loggedInUser) {
      this.applyLoggedInUserRules();
    }
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
    this.logger.debug(`[BookingConfirmationWizard] Guest form initiated`);
  }

  applyLoggedInUserRules() {
    const emailControl = this.guestForm.get('email');
    emailControl?.clearValidators();
    emailControl?.updateValueAndValidity();
    const firstNameControl = this.guestForm.get('firstName');
    firstNameControl?.clearValidators();
    firstNameControl?.updateValueAndValidity();
    const lastNameControl = this.guestForm.get('lastName');
    lastNameControl?.clearValidators();
    lastNameControl?.updateValueAndValidity();
    this.logger.debug(`[BookingConfirmationWizard] Guest form user rules applied`);
  }

  populateFormBaseOnEventType() {
    if (this.invitation.event.eventType == EventTypeModel.FIXED) {
      this.guestForm.get('choosenStartTime')?.patchValue(this.slot.slotStartTime);
      this.guestForm.get('choosenEndTime')?.patchValue(this.slot.slotEndTime);
      this.logger.debug(`[BookingConfirmationWizard] Guest form with Event Type FIXED is populated`);
    }
  }

  get loggedInUser() {
    return this.authService.hasUserValidToken();
  }

  private renderCaptcha(bookingRequestDto: BookingRequestDto) {
    this.logger.debug('[BookingConfirmationWizard] Rendering CAPTCHA');

    if (this.captchaWidgetId !== null) {
      this.resetCaptcha();
      return;
    }

    if (!this.captchaContainer?.nativeElement) {
      this.logger.warn('[BookingConfirmationWizard] CAPTCHA container is not available');
      this.isSubmitInProgress = false;
      return;
    }

    if (typeof grecaptcha === 'undefined' || !grecaptcha.render) {
      this.logger.warn('[BookingConfirmationWizard] CAPTCHA script is not ready');
      this.isSubmitInProgress = false;
      alert('Captcha is still loading. Please try again in a moment.');
      return;
    }

    this.captchaWidgetId = grecaptcha.render(this.captchaContainer.nativeElement, {
      'sitekey': this.captchaSiteKey,
      'callback': (response: string) => {
        this.logger.debug('[BookingConfirmationWizard] CAPTCHA solved');

        if (!this.showCaptcha) return;
        if (this.accessInProgress) return;

        this.captchaToken = response;

        if (!this.captchaToken) return;
        if (!this.pendingEmail) return;

        if (this.pendingEmail) {
          this.logger.debug('[BookingConfirmationWizard] Proceeding after CAPTCHA, issuing guest token');
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
        this.ngZone.run(() => {
          this.captchaToken = null;
        });
      }
    });
  }

  private resetCaptcha() {
    if (this.captchaWidgetId === null || typeof grecaptcha === 'undefined') {
      return;
    }

    try {
      grecaptcha.reset(this.captchaWidgetId);
    } catch {
      this.logger.warn('[BookingAccessComponent] CAPTCHA reset failed');
    }
  }

  private clearCaptchaState() {
    this.logger.debug('[BookingAccessComponent] Clear and hide captcha');
    this.showCaptcha = false;
    this.pendingEmail = null;
    this.captchaToken = null;
    this.accessInProgress = false;

    this.resetCaptcha();
    this.cdr.detectChanges();
  }

  bookSlot(slot: SlotResponseDto) {

    this.logger.debug('[BookingConfirmationWizard] Guest form submitted');

    if (this.isSubmitInProgress) return;

    this.guestForm.markAllAsTouched();
    if (this.guestForm.invalid) {
      this.logger.warn('[BookingConfirmationWizard] Guest form validation failed');
      return;
    }
    this.isSubmitInProgress = true;

    const bookingRequestDto = this.buildBookingRequestDto();

    if (this.loggedInUser) {
      this.logger.debug('[BookingConfirmationWizard] User detected, proceed to create boooking');
      this.createBooking(bookingRequestDto);
    } else {

      let requestDto = new GuestBookingCreateInitRequestDto();
      requestDto.email = this.guestForm?.value.email;

      this.pendingEmail = this.guestForm?.value.email;

      this.logger.debug('[BookingConfirmationWizard] Guest detected, proceed to init guest access');
      this.logger.debug('[BookingConfirmationWizard] Sending bookingService.initGuestBookingCreateAccess request');
      this.bookingService.initGuestBookingCreateAccess(requestDto)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (res) => {
            this.pendingEmail = this.guestForm?.value.email;

            if (res.captchaRequired) {
              this.logger.debug('[BookingConfirmationWizard] CAPTCHA required');
              this.showCaptcha = true;
              this.cdr.detectChanges();
              setTimeout(() => this.renderCaptcha(bookingRequestDto));
              return;
            }

            if (this.pendingEmail) {
              this.logger.debug('[BookingConfirmationWizard] Guest access valid, issuing guest token');
              this.issueGuestBookingCreateAccessToken(bookingRequestDto, this.pendingEmail, null, this.invitation.id, this.slot.id);
            }
          },
          error: () => {
            this.isSubmitInProgress = false;
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
      this.logger.warn('[BookingConfirmationWizard] Event type error when build booking request dto');
      alert('Booking creation error. Please try again. If the problem persists, please contact the administrator.')
    }
    return bookingRequestDto;
  }

  private createBooking(bookingRequestDto: BookingRequestDto) {

    this.logger.debug('[BookingConfirmationWizard] Sending bookingService.createBooking request');
    this.bookingService.createBooking(bookingRequestDto, this.slot.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.organizerBookingResponseDto = res;
          alert('Booking created successfully');
          this.isSubmitInProgress = false;
          this.closeWizard();
          this.router.navigate(['/invitation']);
        },
        error: () => {
          this.isSubmitInProgress = false;
        },
      })
  }

  private issueGuestBookingCreateAccessToken(bookingRequestDto: BookingRequestDto, email: string, captchaToken: string | null, invitationId: number, slotId: number) {
    if (this.accessInProgress) return;
    this.accessInProgress = true;

    let requestDto = new GuestBookingCreateAccessRequestDto();
    requestDto.email = email;
    requestDto.captchaToken = captchaToken;
    requestDto.invitationId = invitationId;
    requestDto.slotId = slotId;

    this.logger.debug('[BookingConfirmationWizard] Sending bookingService.issueGuestBookingCreateAccessToken request');
    this.bookingService.issueGuestBookingCreateAccessToken(requestDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.authService.storeGuestToken(res);
          this.clearCaptchaState()
          this.createBooking(bookingRequestDto);
        },
        error: () => {
          this.isSubmitInProgress = false;
          this.clearCaptchaState();
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

