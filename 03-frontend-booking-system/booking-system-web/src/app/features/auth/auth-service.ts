import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequestDto } from './dtos/login-request-dto';
import { BehaviorSubject, catchError, Observable, tap, throwError } from 'rxjs';
import { SignupRequestDto } from './dtos/signup-request-dto';
import { Router } from '@angular/router';
import { LoginResponseDto } from './dtos/login-response-dto';
import { AuthTokenPayload } from './dtos/auth-token-payload';
import { UserAccessTokenDto } from './dtos/user-access-token-dto';
import { GuestAccessTokenDto } from './dtos/guest-access-token-dto';
import { TokenType } from './model/token-type';
import { environment } from '../../../environments/environment';
import { LoggerService } from '@core/services/logger-service';
import { NotificationService } from '@core/services/notification-service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private loginUrl = `${environment.backendApiUrl}/v1/login`;
  private registerUrl = `${environment.backendApiUrl}/v1/register`;
  private authStatus = new BehaviorSubject<boolean>(false);
  authStatus$ = this.authStatus.asObservable();


  constructor(private httpClient: HttpClient, private router: Router, private logger: LoggerService, private notificationService:NotificationService) {
    this.authStatus.next(this.hasUserValidToken());
  }

  login(loginRequestDto: LoginRequestDto): Observable<LoginResponseDto> {
    this.logger.debug('[AuthService] Login request initiated');
    return this.httpClient.post<LoginResponseDto>(this.loginUrl, loginRequestDto).pipe(
      tap(res => {
        this.logger.info('[AuthService] Login successful');
        this.storeToken(res.userAccessTokenDto);
        this.authStatus.next(true);
      }),
      catchError(error => {
        this.logger.warn('[AuthService] Login failed');
        return throwError(() => error);
      })
    );
  }

  storeToken(token: UserAccessTokenDto | GuestAccessTokenDto) {
    this.logger.debug('[AuthService] Storing user session token');
    localStorage.setItem('session', JSON.stringify(token));
    localStorage.removeItem('guestSession');
  }

  storeGuestToken(token: UserAccessTokenDto | GuestAccessTokenDto) {
    this.logger.debug('[AuthService] Storing guest session token');
    localStorage.setItem('guestSession', JSON.stringify(token));
  }

  register(signupRequestDto: SignupRequestDto): Observable<void> {
    return this.httpClient.post<void>(this.registerUrl, signupRequestDto).pipe(
      tap(() => {
        this.logger.info('[AuthService] Register successful');
      }),
      catchError(error => {
        this.logger.warn('[AuthService] Register failed');
        return throwError(() => error);
      })
    );
  }

  hasUserValidToken(): boolean {
    const session = this.getSession();
    if (!session) return false;

    const payload = this.decodeToken(session.accessToken);
    if (!payload || !payload.exp) { 
      this.logger.debug(`[AuthService] User token validity check: No Session Found`);
      return false 
    };

    if (payload.tokenType !== TokenType.USER) { 
      this.logger.debug(`[AuthService] User token validity check: Not User Token`);
      return false 
    };

    const timeNow = Date.now(); //millisecond

    const expiry = payload.exp * 1000; //millisecond

    const isValid = expiry > timeNow;

    this.logger.debug(`[AuthService] User token validity check: ${isValid}`);

    return isValid;
  }

  getAuthTokenInfo(): AuthTokenPayload | null {
    const session = this.getSession();

    if (!session) {
      this.logger.warn(`[AuthService] No session found when reading auth token`);
      return null;
    }
    const authTokenPayload = this.decodeToken(session.accessToken)

    return authTokenPayload;
  }

  getSession(): UserAccessTokenDto | null {
    const data = localStorage.getItem('session');
    if (!data) return null;

    try {
      return JSON.parse(data);
    } catch {
      this.logger.warn('[AuthService] Failed to parse user session data');
      return null;
    }
  }

  getGuestSession(): GuestAccessTokenDto | null {
    const data = localStorage.getItem('guestSession');
    if (!data) return null;

    try {
      return JSON.parse(data);
    } catch {
      this.logger.warn('[AuthService] Failed to parse guest session data');
      return null;
    }
  }

  getRoles(): string[] {
    const session = this.getSession();
    if (!session) return [];

    const payload = this.decodeToken(session.accessToken);
    return payload?.roles || [];
  }

  logout(): void {
    this.logger.info('[AuthService] User logout triggered');
    this.clearSession();
    this.notificationService.success("Logout Successfully.");
    this.router.navigate(['/welcome']);
  }

  logoutByExpiry(): void {
    this.logger.warn('[AuthService] Session expired logout triggered');
    this.clearSession();
    this.notificationService.warning("Your session has expired. Please log in again.");
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
      this.logger.warn('[AuthService] Failed to decode JWT token');
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
    this.logger.debug('[AuthService] Clearing session storage');
    localStorage.removeItem('session');
    localStorage.removeItem('guestSession');
    this.authStatus.next(false);
  }
}

