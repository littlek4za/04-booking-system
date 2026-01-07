import { Component, AfterViewInit, EventEmitter, ChangeDetectorRef, Output } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-leaflet-map-selection',
  standalone: true,
  imports: [],
  templateUrl: './leaflet-map-selection.html',
  styleUrl: './leaflet-map-selection.css',
})
export class LeafletMapSelection implements AfterViewInit {

  // variable
  map!: L.Map;
  marker?: L.Marker;
  selectedLatLng?: L.LatLng;

  @Output() selectedLocationEmit: EventEmitter<any> = new EventEmitter<L.LatLng>();
  @Output() clearLocationEmit: EventEmitter<void> = new EventEmitter<void>();

  constructor(private cdr: ChangeDetectorRef) { }


  ngAfterViewInit(): void {
    this.getGeoLocationAndInitMap();
  }

  getGeoLocationAndInitMap() {
    const defaultLatLng: L.LatLngExpression = [3.09005, 101.66473];

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const userLatLng: L.LatLngExpression = [
            position.coords.latitude,
            position.coords.longitude
          ];
          this.initMap(userLatLng);
        },
        (error) => {
          console.warn('Geolocation failed, using default', error);
          this.initMap(defaultLatLng);
        }
      );
    } else {
      console.warn('Geolocation not supported, using default');
      this.initMap(defaultLatLng);
    }
  }

  initMap(geoLatLng: L.LatLngExpression) {
    this.map = L.map('map', {
      center: geoLatLng,
      zoom: 11
    });

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);

    setTimeout(() => {
      this.map.invalidateSize();
    }, 100);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.selectedLatLng = e.latlng;

      if (!this.marker) {
        this.marker = L.marker([e.latlng.lat, e.latlng.lng], {
          icon: L.divIcon({
            className: 'fa-map-marker',
            html: `
            <div class="fa-map-marker-image">
            <i class="fa-solid fa-location-dot"></i>
            </div> 
            <label class="fa-map-marker-label">Event Location</label>
            `,
            iconSize: [30, 45],
            iconAnchor: [15, 30],
          }),
          draggable: true
        }).addTo(this.map);

        this.marker.on('dragend', (ev: L.DragEndEvent) => {
          this.selectedLatLng = (ev.target as L.Marker).getLatLng();
          this.cdr.detectChanges();
        });
      } else {
        this.marker.setLatLng(e.latlng);
      }
      this.cdr.detectChanges();
    });
  }

  clearLocation() {
    if(this.marker){
      this.marker.remove();
      this.marker = undefined;
    }
    this.selectedLatLng = undefined;
    this.clearLocationEmit.emit();
  }

  submitLocation() {
    if (this.selectedLatLng) {
      this.selectedLocationEmit.emit(this.selectedLatLng);
    } else {
      alert('Please click on the map to select a location first!');
    }
  }
}