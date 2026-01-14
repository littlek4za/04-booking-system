import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../../features/auth/auth-service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService) { }

  canActivate(route: ActivatedRouteSnapshot): boolean {

    const token = localStorage.getItem('authToken');
    // check token availability
    if (!token) {
      alert('Please login to continue');
      this.router.navigate(['/login']);
      return false;
    }

    //decode JWT payload
    const payload = JSON.parse(atob(token.split('.')[1])); 

    // check expiry
    const timeNow = Math.floor(Date.now()/1000);
    if(payload.exp && payload.exp < timeNow){
      this.authService.logoutByExpiry();
      return false;
    }

    // check role
    const allowedRoles: string[] = route.data['roles'] || [];
    const userRoles: string[] = payload.roles || [];

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
