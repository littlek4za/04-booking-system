import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../features/auth/auth-service';
import { JsonPipe } from '@angular/common';

@Component({
  selector: 'app-auth-debug',
  imports: [JsonPipe],
  templateUrl: './auth-debug.html',
  styleUrl: './auth-debug.css',
})
export class AuthDebug implements OnInit, OnDestroy {

  hasToken: boolean = false;
  token: string | null = null;
  tokenHeader: any = null;
  tokenPayload: any = null;
  decodeError: string | null = null;
  hasTokenExpired: boolean | null = null;

  constructor(public authService: AuthService) { }

  ngOnInit(): void {
    const session = this.authService.getSession();

    this.hasToken = !!session?.token;

    if (session?.token) {
      try {
        const parts = session.token.split('.');
        if (parts.length != 3) {
          console.log("invalid token");
        }
        this.tokenHeader = JSON.parse(atob(this.base64UrlDecode(parts[0])));
        this.tokenPayload = JSON.parse(atob(this.base64UrlDecode(parts[1])));
        this.hasTokenExpired = checkExpiry(this.tokenPayload);
      } catch (err: any) {
        this.decodeError = err.message;
      }
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

  ngOnDestroy(): void {
    console.log('Destoryed debug auth');
  }
}




function checkExpiry(tokenPayload: any): boolean | null {
  const now = Math.floor(Date.now()/1000);
  return tokenPayload.exp < now;
}

