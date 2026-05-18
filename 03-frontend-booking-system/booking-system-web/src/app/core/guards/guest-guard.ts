import { Injectable } from '@angular/core';
import { CanActivate , Router } from '@angular/router';
import { AuthService } from '../../features/auth/auth-service';
import { map, Observable } from 'rxjs';
import { LoggerService } from '@core/services/logger-service';

@Injectable({
  providedIn: 'root',
})
export class GuestGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService, private logger: LoggerService) { }

  canActivate(): Observable<boolean> {
    this.logger.debug('[GuestGuard] Calling canActivate');
    return this.authService.authStatus$.pipe(
      map(isAuth => {
        if (isAuth) {
          alert("Only For Guest User");
          this.logger.debug('[GuestGuard] canActivate result: Rejected, User auth token found');
          return false;
        }
        this.logger.debug('[GuestGuard] canActivate result: Passed');
        return true;
      })
    );
  }
}
