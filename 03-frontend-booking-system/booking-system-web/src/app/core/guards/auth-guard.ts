import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from '../../features/auth/auth-service';
import { LoggerService } from '@core/services/logger-service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService, private logger: LoggerService) { }

  canActivate(route: ActivatedRouteSnapshot): boolean {
    this.logger.debug('[AuthGuard] Calling canActivate');
    const session = this.authService.getSession();

    if (!session) {
      alert('Please login to continue');
      this.router.navigate(['/login']);
      this.logger.debug('[AuthGuard] canActivate result: Rejected, No User Session');
      return false;
    }

    if(!this.authService.hasUserValidToken()){
      this.authService.logoutByExpiry();
      this.logger.debug('[AuthGuard] canActivate result: Rejected, No User Valid Token');
      return false;
    }

    // check role
    const allowedRoles: string[] = route.data['roles'] || [];
    const userRoles: string[] = this.authService.getRoles();

    if (allowedRoles.length > 0) {
      const hasRole = userRoles.some(role => allowedRoles.includes(role));
      
      if (!hasRole) {
        alert('You are not authorized for this page');
        this.router.navigate(['/welcome']);
        this.logger.debug('[AuthGuard] canActivate result: Rejected, User Role does not match');
        return false;
      }
    }
    this.logger.debug('[AuthGuard] canActivate result: Passed');
    return true;
  }
}
