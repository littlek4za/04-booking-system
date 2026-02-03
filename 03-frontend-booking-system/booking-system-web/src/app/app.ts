import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './features/auth/auth-service';
import { AsyncPipe } from '@angular/common';
import { FullCalendarView } from '@shared/components/full-calendar-view/full-calendar-view';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, AsyncPipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('booking-system-web');

  constructor(public authService: AuthService) { }

  
}
