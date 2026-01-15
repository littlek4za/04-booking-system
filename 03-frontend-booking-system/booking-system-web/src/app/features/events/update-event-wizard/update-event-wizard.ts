import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { EventWithSlotCountResponseDto } from '../dtos/event-with-slot-count-response-dto';
import { EventService } from '../event-service';
import { extractFieldErrorMessage } from '@shared/utils/error-utils';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { includePositionValidator } from '@shared/validators/custom-validator';
import { CommonModule } from '@angular/common';
import { LeafletMapSelection } from '@shared/components/leaflet-map-selection/leaflet-map-selection';
import { EventTypeModel } from '../dtos/event-type-model';
import { EventRequestDto } from '../dtos/event-request-dto';

@Component({
  selector: 'app-update-event-wizard',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, LeafletMapSelection],
  templateUrl: './update-event-wizard.html',
  styleUrl: './update-event-wizard.css',
})
export class UpdateEventWizard implements OnInit, OnChanges {

  @Input() eventId!: number;
  @Output() close = new EventEmitter<void>();

  eventData!: EventWithSlotCountResponseDto | null;
  updateEventForm!: FormGroup;
  eventTypeList: EventTypeModel[] = Object.values(EventTypeModel);
  @ViewChild(LeafletMapSelection) mapComponent!: LeafletMapSelection;
  userLatLng?: L.LatLngExpression;

  constructor(private eventService: EventService,) { }

  ngOnInit(): void {
    this.initUpdateEventForm();
    this.listenIncludePositionChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['eventId'] && this.eventId) {
      this.loadEvent(this.eventId);
    }
  }

  private initUpdateEventForm() {
    this.updateEventForm = new FormGroup({
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

  private loadEvent(eventId: number) {
    console.log(this.eventId);
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

  private listenIncludePositionChanges() {
    this.updateEventForm.get('includePosition')!.valueChanges.subscribe(value => {
      if (!value) {
        this.updateEventForm.patchValue({
          latitude: null,
          longitude: null
        });
      }
    });
  }

  private prefillForm(event: EventWithSlotCountResponseDto) {
    this.updateEventForm.patchValue({
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
      this.updateEventForm.get('eventType')?.enable();
    } else {
      this.updateEventForm.get('eventType')?.disable();
    }
  }

  selectLocation(latlng: L.LatLng) {
    console.log('Selected Location', latlng);

    this.updateEventForm.patchValue({
      latitude: latlng.lat,
      longitude: latlng.lng
    });
  }

  clearLocation() {
    this.updateEventForm.patchValue({
      latitude: null,
      longitude: null
    });
  }

  closeWizard() {
    this.close.emit();
  }

  onSubmit() {
    this.updateEventForm.markAllAsTouched;
    if (this.updateEventForm.invalid) {
      return;
    }
    const eventRequestDto = new EventRequestDto;
    eventRequestDto.eventName = this.updateEventForm.value.eventName;
    eventRequestDto.eventDescription = this.updateEventForm.value.eventDescription;
    eventRequestDto.eventLocationAddress = this.updateEventForm.value.eventLocationAddress;
    eventRequestDto.includePosition = this.updateEventForm.value.includePosition;
    eventRequestDto.latitude = this.updateEventForm.value.latitude;
    eventRequestDto.longitude = this.updateEventForm.value.longitude;
    this.updateEventForm.get('eventType')?.enable();
    eventRequestDto.eventType = this.updateEventForm.value.eventType;

    this.eventService.putEventById(this.eventId, eventRequestDto).subscribe({
      next: (res) => {
        console.log('Event Updated Succesfully.', res);
        alert("Event Updated Successfully.");
        this.eventService.triggerRefresh();
        this.closeWizard();
      },
      error: (err) =>{
        console.error('Event fail to update');
      }
    });

  }

}
