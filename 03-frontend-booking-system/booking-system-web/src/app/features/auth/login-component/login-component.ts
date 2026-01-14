import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoginRequestDto } from '../dtos/login-request-dto';
import { AuthService } from '../auth-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-component',
  imports: [ReactiveFormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.initLoginForm();
  }

  initLoginForm() {
    this.loginForm = new FormGroup({
      username: new FormControl<string>("", [Validators.required]),
      password: new FormControl<string>("", [Validators.required]),
      isAgree: new FormControl<boolean>(false, [Validators.requiredTrue])
    });
  }

  onSubmit() {
    this.loginForm.markAllAsTouched();
    console.log("Login Form Submit isValid: " + this.loginForm.valid);
    if (this.loginForm.invalid) {
      return;
    }

    const loginRequestDto = new LoginRequestDto();
    loginRequestDto.username = this.loginForm.value.username;
    loginRequestDto.password = this.loginForm.value.password;

    this.authService.login(loginRequestDto).subscribe({
      next: (response) => {
        console.log('Login success', response);
        this.router.navigate(['/roleSelect']);
      },
      error: (err) => {
        console.log('Login failed');
        this.router.navigate(['/login']);
      }
    });
  }

}
