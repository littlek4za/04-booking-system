import { ChangeDetectorRef, Component, ElementRef, EventEmitter, inject, Input, NgZone, OnDestroy, OnInit, Output, signal, ViewChild } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { AuthService } from '@features/auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GuestBookingViewInitRequestDto } from '../dtos/guest-booking-view-init-request-dto';
import { GuestBookingViewAccessRequestDto } from '../dtos/guest-booking-view-access-request-dto';
import { LoggerService } from '@core/services/logger-service';
import { environment } from '../../../../environments/environment';
import { NotificationService } from '@core/services/notification-service';

declare var grecaptcha: any;

@Component({
  selector: 'app-booking-access-component',
  imports: [ReactiveFormsModule],
  templateUrl: './booking-access-component.html',
  styleUrl: './booking-access-component.css',
})

export class BookingAccessComponent implements OnInit, OnDestroy {

  private captchaSiteKey = environment.captchaSiteKey;

  @ViewChild('captchaContainer') set captchaContainer(element: ElementRef<HTMLElement> | undefined) {
    if (element && this.showCaptcha() && this.captchaWidgetId === null) {
      this.renderCaptcha(element.nativeElement);
    }
  };

  private authService = inject(AuthService);

  @Output() close = new EventEmitter<void>();
  @Input() showCloseButton = false;

  // Form
  bookingTokenValidationForm!: FormGroup;

  // Recaptcha Field
  showCaptcha = signal<boolean>(false);
  private accessInProgress: boolean = false;
  private captchaWidgetId: number | null = null;
  captchaToken: string | null = null;
  isSubmitInProgress = signal<boolean>(false);

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
    // private cdr: ChangeDetectorRef,
    private notificationService: NotificationService
  ) { }

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
            this.logger.debug('[BookingAccessComponent] CAPTCHA required');
            setTimeout(() => {
              this.showCaptcha.set(true);
            });
            // this.cdr.detectChanges();
            // setTimeout(() => this.renderCaptcha());
            return;
          }
          if (res.valid !== true) {
            this.logger.warn('[BookingAccessComponent] Invalid booking access attempt');
            this.notificationService.error("Booking not found(init)");
            this.clearCaptchaState();
            this.isSubmitInProgress.set(false);
            // this.cdr.detectChanges();
            return;
          }
          this.issueToken(email, bookingToken, null);
        },
        error: () => {
          this.isSubmitInProgress.set(false);
          // this.cdr.detectChanges();
        }
      });
  }

  clearCaptchaState() {
    this.logger.debug('[BookingAccessComponent] Clear and hide captcha');
    this.showCaptcha.set(false);
    this.pendingBookingToken = null;
    this.pendingEmail = null;
    this.captchaToken = null;
    this.accessInProgress = false;

    this.resetCaptcha();
    // this.cdr.detectChanges();
    this.captchaWidgetId = null;
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

  private renderCaptcha(containerElement: HTMLElement) {
    this.logger.debug('[BookingAccessComponent] Rendering CAPTCHA');

    // if (this.captchaWidgetId !== null) {
    //   this.resetCaptcha();
    //   return;
    // }

    // if (!this.captchaContainer?.nativeElement) {
    //   this.logger.warn('[BookingAccessComponent] CAPTCHA container is not available');
    //   this.isSubmitInProgress = false;
    //   // this.cdr.detectChanges();
    //   return;
    // }

    if (typeof grecaptcha === 'undefined' || !grecaptcha.render) {
      this.logger.warn('[BookingAccessComponent] CAPTCHA script is not ready');
      this.notificationService.warning('Captcha is still loading. Please try again in a moment.');
      this.isSubmitInProgress.set(false);
      // this.cdr.detectChanges();
      return;
    }

    this.captchaWidgetId = grecaptcha.render(containerElement, {
      'sitekey': this.captchaSiteKey,
      'callback': (response: string) => {
        this.ngZone.run(() => {
          this.logger.debug('[BookingAccessComponent] CAPTCHA solved');

          if (!this.showCaptcha()) return;
          if (this.accessInProgress) return;

          this.captchaToken = response;

          if (!this.captchaToken) return;
          if (!this.pendingEmail || !this.pendingBookingToken) return;

          this.logger.debug('[BookingAccessComponent] Proceeding after CAPTCHA, issuing guest token');
          this.issueToken(this.pendingEmail, this.pendingBookingToken, this.captchaToken);
        });
      },
      'expiry-callback': () => {
        this.ngZone.run(() => {
          this.captchaToken = null;
          this.isSubmitInProgress.set(false);
          // this.cdr.detectChanges();
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
          this.isSubmitInProgress.set(false);
          // this.cdr.detectChanges();
          this.router.navigate([`/bookingView/${bookingToken}`]);
        },
        error: () => {
          this.clearCaptchaState();
          this.isSubmitInProgress.set(false);
          // this.cdr.detectChanges();
        }
      })
  }


  onSubmit() {
    if (this.isSubmitInProgress() == true) return;

    this.logger.debug('[BookingAccessComponent] Booking token validation form submitted');

    this.bookingTokenValidationForm.markAllAsTouched();

    if (this.bookingTokenValidationForm.invalid) {
      this.logger.warn('[BookingAccessComponent] Booking token validation form validation failed');
      return;
    }

    this.isSubmitInProgress.set(true);
    // this.cdr.detectChanges();

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
