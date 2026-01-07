import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth-service';
import { AsyncPipe } from '@angular/common';
import { LeafletMapSelection } from './components/leaflet-map-selection/leaflet-map-selection';


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
