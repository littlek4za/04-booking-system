import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InvitationService } from '../invitation-service';
import { AuthService } from '@features/auth/auth-service';
import { InvitationResponseDto } from '../dtos/invitation-response-dto';
import { InvitationValidationResponseDto } from '../dtos/invitation-validation-response-dto';

@Component({
  selector: 'app-invitation-access-component',
  imports: [ReactiveFormsModule],
  templateUrl: './invitation-access-component.html',
  styleUrl: './invitation-access-component.css',
})
export class InvitationAccessComponent implements OnInit {

  tokenValidationForm!: FormGroup;
  invitationInfo: InvitationValidationResponseDto | null = null;

  constructor(private route: ActivatedRoute,
    private invitationService: InvitationService,
    private authService: AuthService,
    private router: Router) { }

  ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token');

    if (token) {
      this.processToken(token);
    } else {
      this.initTokenValidationForm();
    }
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
    this.invitationService.validateInvitation(token).subscribe({
      next: (res) => {
        this.invitationInfo = res;
        if (res.valid == false) {
          alert(res.reason);
        }
        if (res.requiredLogin == false) {
          alert("redirect to booking page");
        }
        if (res.requiredLogin && !this.authService.hasValidToken()) {
          alert("redirect to login page");
          this.router.navigate(['/login']);

        }
        if (res.requiredLogin && this.authService.hasValidToken()) {
          alert("redirect to booking page");
        }
      },
      error: (err) => {
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

}
