import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot): boolean {

    const token = localStorage.getItem('authToken');

    if (!token) {
      alert('Please login to continue');
      this.router.navigate(['/login']);
      return false;
    }

    const payload = JSON.parse(atob(token.split('.')[1])); //decode JWT payload
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
