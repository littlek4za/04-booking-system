import { Component, ViewChild } from '@angular/core';
import { AddEventWizard } from '../add-event-wizard/add-event-wizard';
import { AddSlotWizard } from '../add-slot-wizard/add-slot-wizard';
import { LeafletMapSelection } from '../leaflet-map-selection/leaflet-map-selection';

@Component({
  selector: 'app-event-dashboard-component',
  standalone: true,
  imports: [AddEventWizard, AddSlotWizard],
  templateUrl: './event-dashboard-component.html',
  styleUrl: './event-dashboard-component.css',
})
export class EventDashboardComponent {

  activeWizard: 'event' | 'slot' | null = null;

  openEventWizard() {
    this.activeWizard = 'event';
  }

  openSlotWizard() {
    this.activeWizard = 'slot';
  }

  closeWizard() {
    this.activeWizard = null;
  }

}
