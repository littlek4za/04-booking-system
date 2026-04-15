import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../../features/auth/auth-service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService) { }

  canActivate(route: ActivatedRouteSnapshot): boolean {

    const session = this.authService.getSession();

    if (!session) {
      alert('Please login to continue');
      this.router.navigate(['/login']);
      return false;
    }

    if(!this.authService.hasValidToken()){
      this.authService.logoutByExpiry();
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
        return false;
      }
    }
    return true;
  }
}
