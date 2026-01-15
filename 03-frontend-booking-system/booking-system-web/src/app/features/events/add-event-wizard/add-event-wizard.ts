import { Component, EventEmitter, Output } from '@angular/core';
import { EventRequestDto } from '../dtos/event-request-dto';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventTypeModel } from '../dtos/event-type-model';
import { CommonModule } from '@angular/common';
import { LeafletMapSelection } from '../../../shared/components/leaflet-map-selection/leaflet-map-selection';
import { EventService } from '../event-service';
import { includePositionValidator } from '@shared/validators/custom-validator';

@Component({
  selector: 'app-add-event-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LeafletMapSelection],
  templateUrl: './add-event-wizard.html',
  styleUrl: './add-event-wizard.css',
})
export class AddEventWizard {

  newEvent!: EventRequestDto;
  eventForm!: FormGroup;
  step: number = 1;
  eventTypeList: EventTypeModel[] = Object.values(EventTypeModel);
  
  @Output() close = new EventEmitter<void>();

  constructor(private eventService:EventService) { }

  ngOnInit(): void {
    this.eventTypeList = Object.values(EventTypeModel);
    this.initEventForm();
    this.listenIncludePositionChanges();
  }

  // init Event Form with validation
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
      eventLocationAddress: new FormControl<string>("", [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(1000)
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
      eventType: new FormControl<string | null>(null, [
        Validators.required
      ])
    }, { validators: includePositionValidator });
  }

  // condition to clear position input for EventForm
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
    const eventRequestDto = new EventRequestDto;
    eventRequestDto.eventName = this.eventForm.value.eventName;
    eventRequestDto.eventDescription = this.eventForm.value.eventDescription;
    eventRequestDto.eventLocationAddress = this.eventForm.value.eventLocationAddress;
    eventRequestDto.includePosition = this.eventForm.value.includePosition;
    eventRequestDto.latitude = this.eventForm.value.latitude;
    eventRequestDto.longitude = this.eventForm.value.longitude;
    eventRequestDto.eventType = this.eventForm.value.eventType;

    this.eventService.saveEventByUser(eventRequestDto).subscribe({
      next: (res) => {
        console.log('Event Created Successfully.', res);
        alert("Event Created Sucessfully.");
        this.eventService.triggerRefresh();
        this.closeWizard();
      },
      error: (err) => {
        console.error('Event fail to create');
      }
    });
  }

  closeWizard() {
    this.close.emit();
  }
}
