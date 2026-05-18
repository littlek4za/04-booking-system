import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InvitationService } from '../invitation-service';
import { AuthService } from '@features/auth/auth-service';
import { InvitationValidationResponseDto } from '../dtos/invitation-validation-response-dto';
import { Subject, takeUntil } from 'rxjs';
import { LoggerService } from '@core/services/logger-service';

@Component({
  selector: 'app-invitation-access-component',
  imports: [ReactiveFormsModule],
  templateUrl: './invitation-access-component.html',
  styleUrl: './invitation-access-component.css',
})
export class InvitationAccessComponent implements OnInit, OnDestroy {

  tokenValidationForm!: FormGroup;
  invitationInfo: InvitationValidationResponseDto | null = null;

  @Output() close = new EventEmitter<void>();

  private destroy$ = new Subject<void>();

  constructor(private route: ActivatedRoute,
    private invitationService: InvitationService,
    private authService: AuthService,
    private router: Router,
    private logger: LoggerService) { }

  ngOnInit() {
    this.initTokenValidationForm();
    const token = this.route.snapshot.paramMap.get('invitationToken');

    if (token) {
      this.logger.debug(`[InvitationAccessComponent] Invitation token detected in URL`);
      this.processToken(token);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initTokenValidationForm() {
    this.tokenValidationForm = new FormGroup({
      token: new FormControl<string>("", [
        Validators.required,
        Validators.pattern(`^[a-zA-Z0-9]{6}$`)
      ])
    });
    this.logger.debug(`[InvitationAccessComponent] Token Validation form initiated`);
  }

  private processToken(token: string) {
    this.logger.debug(`[InvitationAccessComponent] Sending invitationService.validateInvitation service`);
    this.invitationService.validateInvitation(token)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.invitationInfo = res;
          if (res.valid == false) {
            alert(res.reason);
          }
          else if (res.requiredLogin == false) {
            alert("redirecting to booking page");
            this.router.navigate([`/bookingConfirmation/${token}`]);
          }
          else if (res.requiredLogin && !this.authService.hasUserValidToken()) {
            alert("redirecting to login page");
            const currentUrl = this.router.url;
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: currentUrl }
            });
          }
          else if (res.requiredLogin && this.authService.hasUserValidToken()) {
            alert("redirecting to booking page");
            this.router.navigate([`/bookingConfirmation/${token}`]);
          } else {
            this.logger.warn(`[InvitationAccessComponent] Unexpected invitation validation response`);
            alert("Unexpected invitation status. Please try again. If the problem persists, please contact the administrator.");
          }
        },
        error: () => {
        }
      })
  };

  onSubmit() {
    this.logger.debug('[InvitationAccessComponent] Invitation validation form submitted');
    this.tokenValidationForm.markAllAsTouched();
    if (this.tokenValidationForm.invalid) {
      this.logger.warn('[InvitationAccessComponent] Invitation validation form validation failed');
      return;
    }

    const token = this.tokenValidationForm.value.token.toUpperCase();

    this.processToken(token);

  }

  closePage() {
    this.close.emit();
  }

}
