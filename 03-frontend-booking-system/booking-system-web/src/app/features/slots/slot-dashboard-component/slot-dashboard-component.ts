import { Component, inject, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SlotService } from '../slot-service';
import { AddSlotWizard } from '../add-slot-wizard/add-slot-wizard';
import { CommonModule } from '@angular/common';
import { EventService } from '@features/events/event-service';

@Component({
  standalone: true,
  selector: 'app-slot-dashboard-component',
  imports: [CommonModule, RouterLink, AddSlotWizard],
  templateUrl: './slot-dashboard-component.html',
  styleUrl: './slot-dashboard-component.css',
})
export class SlotDashboardComponent implements OnInit {

  private slotService = inject(SlotService);
  slotWizard: boolean = false;
  slotList = toSignal(this.slotService.slot$, { initialValue: [] });
  eventId!: number;
  eventType!: string;


  constructor(private route: ActivatedRoute, private eventService: EventService) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(
      paramMap => {
        this.eventId = +paramMap.get('id')!;
        if (this.eventId) {
          this.slotService.triggerRefresh(this.eventId);
        }
      }
    );

    this.eventService.getEventById(this.eventId).subscribe({
      next:(res) => {
        console.log('GET Event Succesful', res);
        this.eventType = res.eventType;
      },
      error:(err) => {
        console.log('GET Event Failed');
      }
    });
  }


  //Slot Wizard
  openAddSlotWizard() {
    this.slotWizard = true;
  }
  closeAddSlotWizard() {
    this.slotWizard = false;
  }
}
