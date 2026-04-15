import { AfterViewInit, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { BookingResponseDto } from '../dtos/booking-response-dto';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../booking-service';
import { AuthService } from '@features/auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GuestBookingRequestDto } from '../dtos/guest-booking-request-dto';

declare var grecaptcha: any;

@Component({
  selector: 'app-booking-access-component',
  imports: [ReactiveFormsModule],
  templateUrl: './booking-access-component.html',
  styleUrl: './booking-access-component.css',
})

export class BookingAccessComponent implements OnInit, OnDestroy, AfterViewInit {

  private authService = inject(AuthService);

  // Form
  bookingTokenValidationForm!: FormGroup;

  // Submit Field
  bookingInfo: BookingResponseDto | null = null;

  // Recaptcha Field
  captchaToken: string | null = null;

  // IO
  userValid: boolean = this.authService.hasValidToken();

  // Destroy Field
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private bookingService: BookingService,
    private router: Router) { }

  ngOnInit(): void {
    this.initbookingTokenValidationForm();
    const bookingToken = this.route.snapshot.paramMap.get('bookingToken');

    if (bookingToken) {
      this.processBookingToken(bookingToken);
    }
  }

  ngAfterViewInit(): void {
    if (!this.userValid) {
      this.renderCaptcha();
    }

  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  renderCaptcha() {
    grecaptcha.render('captcha-container', {
      'sitekey': '6LdkcLUsAAAAAJMLxLQMGoW3hZ0acjtL7-RdotBu',
      'callback': (response: string) => {
        console.log('Token:', response);
        this.captchaToken = response;
      },
      'expiry-callback': () => {
        this.captchaToken = null;
      }
    });
  }

  private initbookingTokenValidationForm() {
    this.bookingTokenValidationForm = new FormGroup({
      bookingToken: new FormControl<string>("", [
        Validators.required,
        Validators.pattern(`^[a-zA-Z0-9]{6}$`)
      ])
    });
  }

  private processBookingToken(bookingToken: string) {

    this.bookingService.getBookingByToken(bookingToken)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.bookingInfo = res;
          alert("redirecting to booking details page");
          this.router.navigate([`/booking/${bookingToken}`])
        },
        error: (err) => {
          alert("GET Booking failed.");
        }
      });


  }

  private processGuestBookingToken(bookingToken: string) {
    let dto = new GuestBookingRequestDto();
    if (!this.captchaToken) return;
    dto.captcha = this.captchaToken;
    dto.bookingToken = bookingToken;
    this.bookingService.retrieveGuestBooking(dto)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.bookingInfo = res;
          alert("redirecting to booking details page");
          this.router.navigate([`/booking/${bookingToken}`])
        },
        error: (err) => {
          alert("GET Guest Booking failed.");
        }
      });
  }

  onSubmit() {
    this.bookingTokenValidationForm.markAllAsTouched();
    if (this.bookingTokenValidationForm.invalid) {
      return;
    }

    const bookingToken = this.bookingTokenValidationForm.value.bookingToken;

    if (this.userValid) {
      this.processBookingToken(bookingToken);
    } else {
      if (this.captchaToken == null) {
        alert('Please complete the captcha');
        return;
      }
      this.processGuestBookingToken(bookingToken);
    }
  }
}
