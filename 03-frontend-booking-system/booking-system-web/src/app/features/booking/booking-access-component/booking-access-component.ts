import { AfterViewInit, Component, EventEmitter, inject, OnDestroy, OnInit, Output } from '@angular/core';
import { BookingResponseDto } from '../dtos/booking-response-dto';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { AuthService } from '@features/auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GuestBookingViewInitRequestDto } from '../dtos/guest-booking-view-init-request-dto';
import { GuestBookingViewAccessRequestDto } from '../dtos/guest-booking-view-access-request-dto';
import { AttendeeBookingResponseDto } from '../dtos/attendee-booking-response-dto';

declare var grecaptcha: any;

@Component({
  selector: 'app-booking-access-component',
  imports: [ReactiveFormsModule],
  templateUrl: './booking-access-component.html',
  styleUrl: './booking-access-component.css',
})

export class BookingAccessComponent implements OnInit, OnDestroy {

  private authService = inject(AuthService);

  @Output() close = new EventEmitter<void>();




  // Form
  bookingTokenValidationForm!: FormGroup;

  // Submit Field
  bookingInfo: AttendeeBookingResponseDto | null = null;

  // Recaptcha Field
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
    private router: Router) { }

  ngOnInit(): void {
    this.initbookingTokenValidationForm();
  }

  ngOnDestroy(): void {
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
  }

  private initGuestAccess(email: string, bookingToken: string) {
    let requestDto = new GuestBookingViewInitRequestDto();
    requestDto.bookingToken = bookingToken;
    requestDto.email = email;
    this.bookingService.initGuestBookingViewAccess(requestDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe(res => {
        if (res.captchaRequired) {
          console.log("moving to renderCaptcha");
          this.renderCaptcha();
          return;
        }
        if (!res.valid) {
          alert("Invalid booking");
          return;
        }
        console.log("moving to issueToken");
        this.issueToken(email, bookingToken, null);
      })
  }

  private renderCaptcha() {

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
        if (!this.pendingEmail || !this.pendingBookingToken) return;
        if (this.pendingEmail && this.pendingBookingToken) {
          console.log("moving to issueToken");
          this.issueToken(
            this.pendingEmail,
            this.pendingBookingToken,
            this.captchaToken
          );
        }
      },
      'expiry-callback': () => {
        this.captchaToken = null;
      }
    });
  }

  private issueToken(email: string, bookingToken: string, captchaToken: string | null) {
    let requestDto = new GuestBookingViewAccessRequestDto();
    requestDto.bookingToken = bookingToken;
    requestDto.email = email;
    requestDto.captchaToken = captchaToken;
    console.log('Guest', this.authService.getGuestSession());
    console.log('User', this.authService.getSession());
    this.bookingService.issueGuestBookingViewAccessToken(requestDto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.authService.storeGuestToken(res);
          this.pendingBookingToken = null;
          this.pendingEmail = null;
          this.captchaToken = null;
          if (this.captchaWidgetId !== null) {
            grecaptcha.reset(this.captchaWidgetId);
          }
          this.router.navigate([`/bookingView/${bookingToken}`]);
        },
        error: (err) => {
          alert("Access denied");
          console.log(err);
          this.pendingBookingToken = null;
          this.pendingEmail = null;
          this.captchaToken = null;
          if (this.captchaWidgetId !== null) {
            grecaptcha.reset(this.captchaWidgetId);
          }
        }
      })
  }

  onSubmit() {
    this.bookingTokenValidationForm.markAllAsTouched();
    if (this.bookingTokenValidationForm.invalid) {
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
