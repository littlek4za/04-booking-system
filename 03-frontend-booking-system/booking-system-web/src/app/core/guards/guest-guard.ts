import { Injectable } from '@angular/core';
import { CanActivate , Router } from '@angular/router';
import { AuthService } from '../../features/auth/auth-service';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GuestGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService) { }

  canActivate(): Observable<boolean> {
    return this.authService.authStatus$.pipe(
      map(isAuth => {
        if (isAuth) {
          alert("Only For Guest User");
          return false;
        }
        return true;
      })
    );
  }
}
