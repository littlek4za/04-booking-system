import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './features/auth/auth-service';
import { AsyncPipe } from '@angular/common';
import { LoadingService } from '@core/services/loading-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, AsyncPipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('booking-system-web');
  isNavbarOpen = signal(false);

  constructor(
    public authService: AuthService,
    public loadingService: LoadingService
  ) { }

  toggleNavbar() {
    this.isNavbarOpen.update(value => !value);
  }

  closeNavbar() {
    this.isNavbarOpen.set(false);
  }

}
