import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequestDto } from './dtos/login-request-dto';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { SignupRequestDto } from './dtos/signup-request-dto';
import { Router } from '@angular/router';
import { LoginResponseDto } from './dtos/login-response-dto';
import { AuthTokenPayload } from './dtos/auth-token-payload';
import { AuthSession } from './model/auth-session';

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
      tap(res => {
        const authTokenPayload: AuthTokenPayload | null = this.decodeToken(res.token);
        const session: AuthSession = {
          token: res.token,
          type: 'USER',
          expiry: authTokenPayload?.exp ?? 0
        };

        localStorage.setItem('session', JSON.stringify(session));
        this.authStatus.next(true);
      })
    );
  }

  register(signupRequestDto: SignupRequestDto): Observable<any> {
    return this.httpClient.post<SignupRequestDto>(this.registerUrl, signupRequestDto);
  }

  hasValidToken(): boolean {
    const session = this.getSession();

    if (!session) return false;

    const timeNow = Math.floor(Date.now() / 1000);

    return session.expiry > timeNow;
  }

  getAuthTokenInfo(): AuthTokenPayload | null {
    const session = this.getSession();

    if (!session) return null;

    return this.decodeToken(session.token);
  }

  getSession(): AuthSession | null {
    const data = localStorage.getItem('session');
    if (!data) return null;

    try {
      return JSON.parse(data);
    } catch {
      return null;
    }
  }

  getRoles(): string[] {
    const session =this.getSession();
    if(!session) return [];

    const payload = this.decodeToken(session.token);
    return payload?.roles || [];
  }

  logout(): void {
    this.clearSession();
    alert("Logout Successfully.");
    this.router.navigate(['/welcome']);
  }

  logoutByExpiry(): void {
    this.clearSession();
    alert("Your session has expired. Please log in again.");
    setTimeout(() => {
      this.router.navigate(['/login']);
    }, 0);
  }

  private decodeToken(token: string | null): AuthTokenPayload | null {
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(this.base64UrlDecode(payload)));
    } catch {
      return null;
    }
  }

  private base64UrlDecode(str: string): string {
    str = str.replace(/-/g, '+').replace(/_/g, '/');
    // Add padding if missing
    while (str.length % 4) {
      str += '=';
    }
    return str;
  }

  private clearSession(): void {
    localStorage.removeItem('session');
    this.authStatus.next(false);
  }
}

