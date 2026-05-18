import { ChangeDetectorRef, Component, ElementRef, EventEmitter, inject, Input, NgZone, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { AuthService } from '@features/auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GuestBookingViewInitRequestDto } from '../dtos/guest-booking-view-init-request-dto';
import { GuestBookingViewAccessRequestDto } from '../dtos/guest-booking-view-access-request-dto';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { environment } from '../../../../environments/environment';

declare var grecaptcha: any;

@Component({
  selector: 'app-booking-access-component',
  imports: [ReactiveFormsModule],
  templateUrl: './booking-access-component.html',
  styleUrl: './booking-access-component.css',
})

export class BookingAccessComponent implements OnInit, OnDestroy {

  private captchaSiteKey = environment.captchaSiteKey;
  @ViewChild('captchaContainer') private captchaContainer?: ElementRef<HTMLElement>;

  private authService = inject(AuthService);

  @Output() close = new EventEmitter<void>();
  @Input() showCloseButton = false;

  // Form
  bookingTokenValidationForm!: FormGroup;

  // Submit Field
  bookingInfo: AttendeeBookingResponseDto | null = null;

  // Recaptcha Field
  showCaptcha = false;
  private accessInProgress = false;
  private captchaWidgetId: number | null = null;
  captchaToken: string | null = null;

  // for Recaptcha auto submit
  private pendingEmail: string | null = null;
  private pendingBookingToken: string | null = null;

  // Destroy Field
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private bookingService: BookingService,
    private router: Router,
    private logger: LoggerService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.initbookingTokenValidationForm();
    const bookingToken = this.route.snapshot.paramMap.get('bookingToken');
    if (bookingToken) {
      this.logger.debug(`[BookingAccessComponent] Booking token detected in URL`);
      this.bookingTokenValidationForm.patchValue({ bookingToken });

      if (this.authService.hasUserValidToken()) {
        this.logger.debug(`[BookingAccessComponent] User already authenticated, redirecting`);
        this.router.navigate([`/bookingView/${bookingToken}`]);
      }
    }
  }

  ngOnDestroy(): void {
    this.resetCaptcha();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initbookingTokenValidationForm() {
    this.bookingTokenValidationForm = new FormGroup({
      bookingToken: new FormControl<string>("", [
        Validators.required,
        Validators.pattern(`^[a-zA-Z0-9]{6}$`)
      ]),
      email: new FormControl<string>("", [
        Validators.required,
        Validators.pattern('^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$')
      ])
    });
    this.logger.debug('[BookingAccessComponent] Booking token validation form initialized');
  }

  private initGuestAccess(email: string, bookingToken: string) {
    let requestDto = new GuestBookingViewInitRequestDto();
    requestDto.bookingToken = bookingToken;
    requestDto.email = email;
    this.logger.debug('[BookingAccessComponent] Sending bookingService.initGuestBookingViewAccess request');
    this.bookingService.initGuestBookingViewAccess(requestDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.pendingEmail = email;
          this.pendingBookingToken = bookingToken;

          if (res.captchaRequired) {
            this.logger.debug('[BookingAccessComponent] CAPTCHA required')
            this.showCaptcha = true;
            this.cdr.detectChanges();
            setTimeout(() => this.renderCaptcha());
            return;
          }
          if (res.valid !== true) {
            this.logger.warn('[BookingAccessComponent] Invalid booking access attempt');
            alert("Booking not found(init)");
            this.clearCaptchaState();
            return;
          }
          this.issueToken(email, bookingToken, null);
        },
        error: () => {
          this.logger.warn('[BookingAccessComponent] initGuestAccess failed');
          alert("An error occurred. Please try again. If the problem persists, please contact the administrator.");
        }
      });
  }

  clearCaptchaState() {
    this.logger.debug('[BookingAccessComponent] Clear and hide captcha');
    this.showCaptcha = false;
    this.pendingBookingToken = null;
    this.pendingEmail = null;
    this.captchaToken = null;
    this.accessInProgress = false;

    this.resetCaptcha();
    this.cdr.detectChanges();
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

  private renderCaptcha() {
    this.logger.debug('[BookingAccessComponent] Rendering CAPTCHA');

    if (this.captchaWidgetId !== null) {
      this.resetCaptcha();
      return;
    }

    if (!this.captchaContainer?.nativeElement) {
      this.logger.warn('[BookingAccessComponent] CAPTCHA container is not available');
      return;
    }

    if (typeof grecaptcha === 'undefined' || !grecaptcha.render) {
      this.logger.warn('[BookingAccessComponent] CAPTCHA script is not ready');
      alert('Captcha is still loading. Please try again in a moment.');
      return;
    }

    this.captchaWidgetId = grecaptcha.render(this.captchaContainer.nativeElement, {
      'sitekey': this.captchaSiteKey,
      'callback': (response: string) => {
        this.logger.debug('[BookingAccessComponent] CAPTCHA solved');

        if (!this.showCaptcha) return;
        if (this.accessInProgress) return;

        this.captchaToken = response;

        if (!this.captchaToken) return;
        if (!this.pendingEmail || !this.pendingBookingToken) return;

        this.logger.debug('[BookingAccessComponent] Proceeding after CAPTCHA, issuing guest token');
        this.issueToken(this.pendingEmail, this.pendingBookingToken, this.captchaToken);
      },
      'expiry-callback': () => {
        this.ngZone.run(() => {
          this.captchaToken = null;
        });
      }
    });

  }

  private issueToken(email: string, bookingToken: string, captchaToken: string | null) {
    if (this.accessInProgress) return;
    this.accessInProgress = true;

    let requestDto = new GuestBookingViewAccessRequestDto();
    requestDto.bookingToken = bookingToken;
    requestDto.email = email;
    requestDto.captchaToken = captchaToken;

    this.logger.debug('[BookingAccessComponent] Sending bookingService.issueGuestBookingViewAccessToken request');

    this.bookingService.issueGuestBookingViewAccessToken(requestDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.authService.storeGuestToken(res);
          this.clearCaptchaState();
          this.router.navigate([`/bookingView/${bookingToken}`]);
        },
        error: () => {
          this.clearCaptchaState();
        }
      })
  }


  onSubmit() {
    this.logger.debug('[BookingAccessComponent] Booking token validation form submitted');
    this.bookingTokenValidationForm.markAllAsTouched();
    if (this.bookingTokenValidationForm.invalid) {
      this.logger.warn('[BookingAccessComponent] Booking token validation form validation failed');
      return;
    }

    const bookingToken = this.bookingTokenValidationForm.value.bookingToken;
    const email = this.bookingTokenValidationForm.value.email;


    this.pendingEmail = email;
    this.pendingBookingToken = bookingToken;
    this.initGuestAccess(email, bookingToken);

  }

  closePage() {
    this.close.emit();
  }
}
