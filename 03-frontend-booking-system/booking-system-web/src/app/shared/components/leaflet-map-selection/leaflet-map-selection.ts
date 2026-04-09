import { CommonModule } from '@angular/common';
import { Component, AfterViewInit, EventEmitter, Output, Input, NgZone, signal } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-leaflet-map-selection',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './leaflet-map-selection.html',
  styleUrl: './leaflet-map-selection.css',
})
export class LeafletMapSelection implements AfterViewInit {

  // variable
  map!: L.Map;
  marker?: L.Marker;
  selectedLatLng = signal<L.LatLng | null>(null);

  @Output() selectedLocationEmit: EventEmitter<any> = new EventEmitter<L.LatLng>();
  @Output() clearLocationEmit: EventEmitter<void> = new EventEmitter<void>();
  @Input() inputLatLng?: L.LatLngExpression;

  private readonly defaultLatLng: L.LatLngExpression = [3.09005, 101.66473];
  private mapReady: boolean = false;

  constructor(private ngZone: NgZone) { }

  ngAfterViewInit(): void {
    this.initMap();

  }

  initMap(): void {
    this.map = L.map('map', {
      center: this.defaultLatLng,
      zoom: 11
    });

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);

    setTimeout(() => {
      this.map.invalidateSize();
    }, 100);

    this.mapReady = true;

    //Piority: Input -> get -> default
    if (this.inputLatLng) {
      this.setMapLocation(this.inputLatLng);
    } else {
      this.tryGeoLocation();
    }

    // Map on Click
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.ngZone.run(() => {
        this.updateSelection(e.latlng);
      });
    });
  }

  setMapLocation(latlng: L.LatLngExpression): void {
    if (!this.mapReady) return;

    const ll = L.latLng(latlng);
    this.map.setView(ll, 10);

    // Delay avoids ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      this.ngZone.run(() => {
        this.updateSelection(ll);
      });
    });
  }

  private updateSelection(latlng: L.LatLng): void {
    this.selectedLatLng.set(latlng);

    if (!this.marker) {
      this.marker = this.createMarker(latlng);
    } else {
      this.marker.setLatLng(latlng);
    }
  }

  private createMarker(latlng: L.LatLng): L.Marker {
    const marker = L.marker(latlng, {
      draggable: true,
      icon: L.divIcon({
        className: 'fa-map-marker',
        html: `
        <div class="fa-map-marker-image" style="text-align:center; color:#d9534f; font-size:1.5rem;">
          <i class="fa-solid fa-location-dot"></i>
        </div>
        <label class="fa-map-marker-label" 
               style="display:block; font-size:0.7rem; font-weight:500; color:#333; margin-top:2px; text-align:center; white-space:nowrap;">
          Event<br>Location
        </label>
        `,
        iconSize: [30, 45],
        iconAnchor: [15, 30],
      }),
    }).addTo(this.map);

    marker.on('dragend', e => {
      this.ngZone.run(() => {
        this.updateSelection((e.target as L.Marker).getLatLng());
      });
    });

    return marker;
  }

  private tryGeoLocation(): void {
    if (!navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(
    (position) => {
      this.ngZone.run(() => {
        this.setMapLocation([position.coords.latitude, position.coords.longitude]);
      });
    },
    (error) => {
      console.warn('Geolocation failed', error);
      this.setMapLocation(this.defaultLatLng);
    });
  }

  clearLocation() {
    if (this.marker) {
      this.marker.remove();
      this.marker = undefined;
    }
    this.selectedLatLng.set(null);
    this.clearLocationEmit.emit();
  }

  submitLocation() {
    const value = this.selectedLatLng();
    if (value) {
      this.selectedLocationEmit.emit(value);
    }
  }
}