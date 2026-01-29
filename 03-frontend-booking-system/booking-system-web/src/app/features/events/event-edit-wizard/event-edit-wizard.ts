import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { EventRequestDto } from '../dtos/event-request-dto';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventTypeModel } from '../dtos/event-type-model';
import { CommonModule } from '@angular/common';
import { LeafletMapSelection } from '../../../shared/components/leaflet-map-selection/leaflet-map-selection';
import { EventService } from '../event-service';
import { includePositionValidator } from '@shared/validators/custom-validator';
import { EventWithSlotCountResponseDto } from '../dtos/event-with-slot-count-response-dto';

@Component({
  selector: 'app-event-edit-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LeafletMapSelection],
  templateUrl: './event-edit-wizard.html',
  styleUrl: './event-edit-wizard.css',
})
export class EventEditWizard implements OnInit, OnChanges {

  @Input() mode!: 'CREATE' | 'UPDATE';
  @Input() eventId: number | null = null;
  @Output() close = new EventEmitter<void>();

  eventData!: EventWithSlotCountResponseDto | null;
  eventForm!: FormGroup;
  userLatLng?: L.LatLngExpression;
  eventTypeList: EventTypeModel[] = Object.values(EventTypeModel);

  constructor(private eventService: EventService) { }

  ngOnInit(): void {
    this.eventTypeList = Object.values(EventTypeModel);
    this.initEventForm();
    this.listenIncludePositionChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['eventId'] && this.mode === 'UPDATE' && this.eventId) {
      this.loadEvent(this.eventId);
    }
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

  // for update
  private loadEvent(eventId: number) {
    console.log('Loading Event for form',this.eventId);
    this.eventService.getEventById(eventId).subscribe({
      next: (res) => {
        console.log('GET Event Success', res);
        this.eventData = res;
        this.prefillForm(this.eventData);
        this.updateEventTypeControl();
        if (this.eventData?.includePosition) {
          this.userLatLng = [this.eventData.latitude!, this.eventData.longitude!];
        }
      },
      error: (err) => {
        console.log('GET Event Failed');
      }
    });
  }

  private prefillForm(event: EventWithSlotCountResponseDto) {
    this.eventForm.patchValue({
      eventName: event.eventName,
      eventDescription: event.eventDescription,
      eventLocationAddress: event.eventLocationAddress,
      includePosition: event.includePosition,
      latitude: event.latitude,
      longitude: event.longitude,
      eventType: event.eventType,
    })
  }

  private updateEventTypeControl() {
    if (this.eventData?.slotCount === 0) {
      this.eventForm.get('eventType')?.enable();
    } else {
      this.eventForm.get('eventType')?.disable();
    }
  }

  // for map
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

    this.eventService.createEvent(eventRequestDto).subscribe({
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
