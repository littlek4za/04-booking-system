import { Component, inject, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SlotService } from '../slot-service';
import { AddSlotWizard } from '../add-slot-wizard/add-slot-wizard';
import { CommonModule, DatePipe } from '@angular/common';
import { EventService } from '@features/events/event-service';
import { EventTypeModel } from '@features/events/dtos/event-type-model';

@Component({
  standalone: true,
  selector: 'app-slot-dashboard-component',
  imports: [CommonModule, RouterLink, AddSlotWizard, DatePipe],
  templateUrl: './slot-dashboard-component.html',
  styleUrl: './slot-dashboard-component.css',
})
export class SlotDashboardComponent implements OnInit {

  private slotService = inject(SlotService);
  openSlotWizard: boolean = false;
  updateSlotWizard: boolean = false;
  slotList = toSignal(this.slotService.slot$, { initialValue: [] });
  eventId!: number;
  eventType!: string;
  slotId: number | null = null;
  modeSlotWizard!: 'CREATE' | 'UPDATE';
  protected readonly EventType = EventTypeModel;


  constructor(private route: ActivatedRoute, private eventService: EventService) { }

  ngOnInit(): void {
    this.refreshSlotListWithEventId();
    this.subscribeEventType();
  }

  private refreshSlotListWithEventId() {
    this.route.paramMap.subscribe(
      paramMap => {
        this.eventId = +paramMap.get('id')!;
        if (this.eventId) {
          this.slotService.triggerRefresh(this.eventId);
        }
      }
    );
  }

  private subscribeEventType() {
    this.eventService.getEventById(this.eventId).subscribe({
      next: (res) => {
        console.log('GET Event Succesful', res);
        this.eventType = res.eventType;
      },
      error: (err) => {
        console.log('GET Event Failed');
      }
    });
  }

  confirmDeleteSlot(slotId: number) {
    if(confirm("Are you sure you want to delete this Slot?")){
      console.log(slotId);
      this.deleteSlotById(slotId);
    }
  }

  private deleteSlotById(slotId : number) {
    this.slotService.deleteSlotByIdAndEvent(this.eventId, slotId).subscribe({
      next: (res) => {
        console.log('Delete Slot Succesfully');
        this.slotService.triggerRefresh(this.eventId);
      },
      error: (err) => {
        console.error('Delete Slot Failed');
      }
    })
  }

  //Slot Wizard
  openCreateSlotWizard() {
    this.modeSlotWizard = 'CREATE';
    this.slotId = null;
    this.openSlotWizard = true;
  }

  openUpdateSlotWizard(slotId:number){
    this.modeSlotWizard = 'UPDATE';
    this.slotId = slotId;
    this.openSlotWizard = true;
  }

  closeSlotWizard() {
    this.openSlotWizard = false;
  }

}
