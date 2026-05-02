import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InvitationService } from '../invitation-service';
import { AuthService } from '@features/auth/auth-service';
import { InvitationValidationResponseDto } from '../dtos/invitation-validation-response-dto';
import { Subject, takeUntil } from 'rxjs';

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
    private router: Router) { }

  ngOnInit() {
    this.initTokenValidationForm();
    const token = this.route.snapshot.paramMap.get('invitationToken');

    if (token) {
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
  }

  private processToken(token: string) {
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
          else if (res.requiredLogin && !this.authService.hasValidToken()) {
            alert("redirecting to login page");
            const currentUrl = this.router.url;
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: currentUrl }
            });
          }
          else if (res.requiredLogin && this.authService.hasValidToken()) {
            alert("redirectingn to booking page");
            this.router.navigate([`/bookingConfirmation/${token}`]);
          } else {
            console.warn("Unexpected invitation validation response:", res);
            alert("Unexpected invitation status. Please contact administrator.");
          }
        },
        error: (err) => {
          alert("Token validation error. Please contact administrator");
        }
      })
  };

  onSubmit() {
    this.tokenValidationForm.markAllAsTouched();
    if (this.tokenValidationForm.invalid) {
      return;
    }

    const token = this.tokenValidationForm.value.token.toUpperCase();

    this.processToken(token);

  }

  closePage() {
    this.close.emit();
  }

}
