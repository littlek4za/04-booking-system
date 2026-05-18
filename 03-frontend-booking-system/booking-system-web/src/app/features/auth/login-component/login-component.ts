import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoginRequestDto } from '../dtos/login-request-dto';
import { AuthService } from '../auth-service';
import { ActivatedRoute, Router } from '@angular/router';
import { LoggerService } from '@core/services/logger-service';

@Component({
  selector: 'app-login-component',
  imports: [ReactiveFormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;
  showPassword: boolean = false;

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute, private logger: LoggerService) { }

  successLoginReturnUrl: string = '/roleSelect';

  ngOnInit(): void {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    this.successLoginReturnUrl = returnUrl || '/roleSelect';
    this.initLoginForm();
  }

  initLoginForm() {

    this.loginForm = new FormGroup({
      username: new FormControl<string>("", [Validators.required]),
      password: new FormControl<string>("", [Validators.required])
      // isAgree: new FormControl<boolean>(false, [Validators.requiredTrue])
    });

    this.logger.debug('[LoginComponent] Login form initialized');
  }

  goRegister() {
    this.logger.info('[LoginComponent] Navigating to /register');

    if (this.successLoginReturnUrl && this.successLoginReturnUrl !== '/roleSelect') {
      this.router.navigate(['/register'], {
        queryParams: { returnUrl: this.successLoginReturnUrl }
      });
    } else {
      this.router.navigate(['/register']);
    }

  }

  onSubmit() {
    this.logger.debug('[LoginComponent] Login form submitted');

    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) {
      this.logger.warn('[LoginComponent] Login form validation failed');
      return;
    }

    const loginRequestDto = new LoginRequestDto();
    loginRequestDto.username = this.loginForm.value.username;
    loginRequestDto.password = this.loginForm.value.password;
    this.logger.debug('[LoginComponent] Sending AuthService.login request');
    this.authService.login(loginRequestDto).subscribe({
      next: () => {
        this.router.navigateByUrl(this.successLoginReturnUrl);
      },
      error: () => {
      }
    });
  }

}
