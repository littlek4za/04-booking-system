import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequestDto } from './dtos/login-request-dto';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { SignupRequestDto } from './dtos/signup-request-dto';
import { Router } from '@angular/router';
import { LoginResponseDto } from './dtos/login-response-dto';
import { AuthTokenPayload } from './dtos/auth-token-payload';
@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private loginUrl = "http://localhost:8080/api/v1/login";
  private registerUrl = "http://localhost:8080/api/v1/register";
  private authStatus = new BehaviorSubject<boolean>(this.hasValidToken());
  authStatus$ = this.authStatus.asObservable();

  constructor(private httpClient: HttpClient, private router: Router) {
  }

  login(loginRequestDto: LoginRequestDto): Observable<LoginResponseDto> {
    return this.httpClient.post<LoginResponseDto>(this.loginUrl, loginRequestDto).pipe(
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

  register(signupRequestDto: SignupRequestDto): Observable<any> {
    return this.httpClient.post<SignupRequestDto>(this.registerUrl, signupRequestDto);
  }

  hasValidToken(): boolean {
    const token = localStorage.getItem('authToken');
    if (!token) {
      return false;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const timeNow = Math.floor(Date.now() / 1000);
      if (!payload.exp || payload.exp < timeNow) {
        return false;
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  getAuthTokenInfo(): AuthTokenPayload | null{
    const token = localStorage.getItem('authToken');

    if (token) {
      try {
        const parts = token.split('.');
        if (parts.length != 3) {
          console.log("invalid token");
        }
        return JSON.parse(atob(this.base64UrlDecode(parts[1]))) as AuthTokenPayload;
      } catch (err: any) {
        console.error(err);
        alert(err.message);
        return null;
      }
    } else {
      return null;
    }
  }

  base64UrlDecode(str: string): string {
    str = str.replace(/-/g, '+').replace(/_/g, '/');
    // Add padding if missing
    while (str.length % 4) {
      str += '=';
    }
    return str;
  }

  logout(): void {
    localStorage.removeItem("authToken");
    this.authStatus.next(false);
    alert("Logout Successfully.");
    this.router.navigate(['/welcome']);
  }

  logoutByExpiry(): void {
    localStorage.removeItem("authToken");
    this.authStatus.next(false);
    alert("Your session has expired. Please log in again.");
    setTimeout(() => {
      this.router.navigate(['/login']);
    }, 0);
  }
}

