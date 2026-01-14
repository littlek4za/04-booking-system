import { Component } from '@angular/core';
import { AuthService } from '../auth-service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { passwordMatchedValidator } from '../../../shared/validators/custom-validator';
import { SignupRequestDto } from '../dtos/signup-request-dto';
import { Router } from '@angular/router';
import { extractFieldErrorMessage } from '../../../shared/utils/error-utils';

@Component({
  selector: 'app-register-component',
  imports: [ReactiveFormsModule],
  templateUrl: './register-component.html',
  styleUrl: './register-component.css',
})
export class RegisterComponent {

  registerForm!: FormGroup;
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.initRegisterForm();
  }

  private initRegisterForm() {
    this.registerForm = new FormGroup({
      username: new FormControl<string>("",
        [Validators.required,
        Validators.minLength(3),
        Validators.maxLength(30),
        Validators.pattern('^[a-zA-Z0-9][a-zA-Z0-9!@#$%^&*]{0,28}[a-zA-Z0-9]$')]),
      password: new FormControl<string>("",
        [Validators.required,
        Validators.minLength(8),
        Validators.maxLength(40),
        Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$')]),
      confirmPassword: new FormControl<string>("",
        [Validators.required]),
      email: new FormControl<string>("",
        [Validators.required,
        Validators.maxLength(255),
        Validators.pattern('^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$')]),
      firstName: new FormControl<string>("",
        [Validators.required,
        Validators.minLength(1),
        Validators.maxLength(100)]),
      lastName: new FormControl<string>("",
        [Validators.required,
        Validators.minLength(1),
        Validators.maxLength(100)]),
      isAgree: new FormControl<boolean>(false,
        [Validators.requiredTrue])
    },
      { validators: passwordMatchedValidator }
    );
  }

  onSubmit() {
    this.registerForm.markAllAsTouched;
    if (this.registerForm.invalid) {
      return;
    }
    const signupRequestDto = new SignupRequestDto;
    signupRequestDto.username = this.registerForm.value.username;
    signupRequestDto.password = this.registerForm.value.password;
    signupRequestDto.email = this.registerForm.value.email;
    signupRequestDto.firstName = this.registerForm.value.firstName;
    signupRequestDto.lastName = this.registerForm.value.lastName;

    this.authService.register(signupRequestDto).subscribe({
      next: (response) => {
        console.log('Register success', response);
        alert("Registration Success! Please proceed to log in");
        this.router.navigate(['/login'])
      },
      error: (err) => {
        console.log('Registration failed');
      },
    });
  }
}




