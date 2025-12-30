import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CredentialsDto } from '../common/credentials-dto';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { SignupDto } from '../common/signup-dto';
import { Router } from '@angular/router';
import { LoginResponseDto } from '../common/login-response-dto';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private loginUrl = "http://localhost:8080/login";
  private registerUrl = "http://localhost:8080/register";
  private authStatus = new BehaviorSubject<boolean>(this.hasValidToken());
  authStatus$ = this.authStatus.asObservable();

  constructor(private httpClient: HttpClient, private router: Router) {
  }

  login(credentialsDto: CredentialsDto): Observable<LoginResponseDto> {
    return this.httpClient.post<LoginResponseDto>(this.loginUrl, credentialsDto).pipe(
      tap(
        {
          next: (res) => {
            localStorage.setItem('authToken', res.token);
            this.authStatus.next(true);
          }
        }
      )
    );
  }

  register(signupDto: SignupDto): Observable<any> {
    return this.httpClient.post<SignupDto>(this.registerUrl, signupDto);
  }

  hasValidToken(): boolean {
    const token = localStorage.getItem('authToken');
    if (!token) {
      return false;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const timeNow = Math.floor(Date.now() / 1000);
      return payload.exp && payload.exp > timeNow;
    } catch (e) {
      localStorage.removeItem('authToken');
      return false;
    }
  }

  logout(): void {
    localStorage.removeItem("authToken");
    this.authStatus.next(false);
    alert("Logout Successfully.");
    // this.router.navigate(['/welcome']);
  }
}

