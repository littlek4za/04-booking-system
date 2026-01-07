import { Component, EventEmitter, Output } from '@angular/core';
import { EventSaveRequestDto } from '../../common/event-save-request-dto';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SlotTypeModel } from '../../common/slot-type-model';
import { CommonModule } from '@angular/common';
import { LeafletMapSelection } from '../leaflet-map-selection/leaflet-map-selection';
import { EventService } from '../../services/event-service';
import { includePositionValidator } from '../../validators/custom-validator';

@Component({
  selector: 'app-add-event-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LeafletMapSelection],
  templateUrl: './add-event-wizard.html',
  styleUrl: './add-event-wizard.css',
})
export class AddEventWizard {

  newEvent!: EventSaveRequestDto;
  eventForm!: FormGroup
  step: number = 1;
  slotType = Object.values(SlotTypeModel);

  constructor(private eventService:EventService) { }

  ngOnInit(): void {
    this.slotType = Object.values(SlotTypeModel);
    this.initEventForm();
    this.listenIncludePositionChanges();
  }

  private initEventForm() {
    this.eventForm = new FormGroup({
      eventName: new FormControl<string>("", [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(350)
      ]),
      eventDescription: new FormControl<string>("", [
        Validators.maxLength(2500)
      ]),
      eventLocationName: new FormControl<string>("", [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(350)
      ]),
      includePosition: new FormControl<boolean>(false),
      latitude: new FormControl<number | null>(null, [
        Validators.min(-90),
        Validators.max(90)
      ]),
      longitude: new FormControl<number | null>(null, [
        Validators.min(-180),
        Validators.max(180)
      ]),
      slotType: new FormControl<string | null>(null, [
        Validators.required
      ])
    }, { validators: includePositionValidator });
  }

  // position method
  private listenIncludePositionChanges() {
    this.eventForm.get('includePosition')!.valueChanges.subscribe(value => {
      if (!value) {
        this.eventForm.patchValue({
          latitude: null,
          longitude: null
        });
      }
    });
  }

  clearLocation() {
    this.eventForm.patchValue({
      latitude: null,
      longitude: null
    });
  }

  selectLocation(latlng: L.LatLng) {
    console.log('Selected Location', latlng);

    this.eventForm.patchValue({
      latitude: latlng.lat,
      longitude: latlng.lng
    });
  }

  onSubmit() {
    this.eventForm.markAllAsTouched();
    if (this.eventForm.invalid) {
      return;
    }
    const eventSaveRequestDto = new EventSaveRequestDto;
    eventSaveRequestDto.eventName = this.eventForm.value.eventName;
    eventSaveRequestDto.eventDescription = this.eventForm.value.eventDescription;
    eventSaveRequestDto.eventLocationName = this.eventForm.value.eventLocationName;
    eventSaveRequestDto.includePosition = this.eventForm.value.includePosition;
    eventSaveRequestDto.latitude = this.eventForm.value.latitude;
    eventSaveRequestDto.longitude = this.eventForm.value.longitude;
    eventSaveRequestDto.slotType = this.eventForm.value.slotType;

    this.eventService.saveEvent(eventSaveRequestDto).subscribe({
      next: (res) => {
        console.log('Event Created Successfully.', res);
        alert("Event Created Sucessfully.");
        this.closeWizard();
      },
      error: (err) => {
        console.error('Event fail to create', err);
        let alertMessage = err.error?.message || "Something went wrong. Please try again.";
        if (err.error?.fieldErrorList && err.error.fieldErrorList.length > 0) {
          const fieldMessages = err.error.fieldErrorList
                                          .map((f:any)=> `${f.field}:${f.message}`)
                                          .join('\n');
          alertMessage += '\n' + fieldMessages;
                    
        }
        alert(alertMessage);
      },
      complete:() => {
        console.log("Request complete");
      }
    });
  }

  @Output()
  close = new EventEmitter<void>();

  closeWizard() {
    this.close.emit();
  }



}
