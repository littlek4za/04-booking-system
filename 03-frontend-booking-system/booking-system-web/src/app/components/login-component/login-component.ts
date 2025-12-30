import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CredentialsDto } from '../../common/credentials-dto';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-component',
  imports: [ReactiveFormsModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent {

  loginForm: FormGroup;

  constructor(private authService: AuthService, private router:Router) {
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

    const credentialsDto = new CredentialsDto();
    credentialsDto.username = this.loginForm.value.username;
    credentialsDto.password = this.loginForm.value.password;

    this.authService.login(credentialsDto).subscribe({
      next: (response) => {
        console.log('Login success', response);
        this.router.navigate(['/roleSelect']);
      },
      error: (err) => {
        console.log('Login failed', err);
        alert(err.error.message);
        this.router.navigate(['/login']);
      }
    })
  }

}
