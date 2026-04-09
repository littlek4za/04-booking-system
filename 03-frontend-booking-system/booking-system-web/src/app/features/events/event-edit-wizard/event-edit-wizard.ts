import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { EventRequestDto } from '../dtos/event-request-dto';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventTypeModel } from '../dtos/event-type-model';
import { CommonModule } from '@angular/common';
import { LeafletMapSelection } from '../../../shared/components/leaflet-map-selection/leaflet-map-selection';
import { EventService } from '../event-service';
import { includePositionValidator } from '@shared/validators/custom-validator';
import { EventWithSlotCountResponseDto } from '../dtos/event-with-slot-count-response-dto';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-event-edit-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LeafletMapSelection],
  templateUrl: './event-edit-wizard.html',
  styleUrl: './event-edit-wizard.css',
})
export class EventEditWizard implements OnInit, OnChanges, OnDestroy {

  @Input() mode!: 'CREATE' | 'UPDATE';
  @Input() eventId: number | null = null;
  @Output() close = new EventEmitter<void>();

  eventData!: EventWithSlotCountResponseDto | null;
  eventForm!: FormGroup;
  userLatLng?: L.LatLngExpression;
  eventTypeList: EventTypeModel[] = Object.values(EventTypeModel);

  private destroy$ = new Subject<void>();

  constructor(private eventService: EventService) {
    this.initEventForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const eventIdChange = changes['eventId'];
    if (changes['eventId'] && this.mode === 'UPDATE' && this.eventId) {
      this.loadEvent(this.eventId);
    }
  }

  ngOnInit(): void {
    this.eventTypeList = Object.values(EventTypeModel);
    this.actionWhenFormValueChanges();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
      noMaxBookingsPerIdentity: new FormControl<boolean>(true, [Validators.required]),
      maxBookingsPerIdentity: new FormControl<number | null>(null),
      eventType: new FormControl<string | null>(null, [
        Validators.required
      ])
    }, { validators: includePositionValidator });
  }

  // condition to clear position input for EventForm
  private actionWhenFormValueChanges() {
    this.eventForm.get('includePosition')!.valueChanges.subscribe(value => {
      if (!value) {
        this.eventForm.patchValue({
          latitude: null,
          longitude: null
        });
      }
    });
    this.eventForm.get('noMaxBookingsPerIdentity')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onNoMaxBookingsPerIdentityChanges(value));
  }

  get hasMaxBookingsPerIdentity(): boolean {
    return this.eventForm.getRawValue().noMaxBookingsPerIdentity === false;
  }

  private onNoMaxBookingsPerIdentityChanges(value: any): void {
    this.eventForm.get('noMaxBookingsPerIdentity')?.markAsTouched();
    if (value == true) {
      this.eventForm.get('maxBookingsPerIdentity')?.clearValidators();
      this.eventForm.get('maxBookingsPerIdentity')?.setValue(null);
      this.eventForm.updateValueAndValidity({ emitEvent: false });
    } else {
      this.eventForm.get('maxBookingsPerIdentity')?.setValidators([Validators.required, Validators.min(1)]);
      this.eventForm.updateValueAndValidity({ emitEvent: false });
    }
  }

  // for update
  private loadEvent(eventId: number) {
    console.log('Loading Event for form', this.eventId);
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

    if (event.maxBookingsPerIdentity != null) {
      this.eventForm.patchValue({
        noMaxBookingsPerIdentity: false,
        maxBookingsPerIdentity: event.maxBookingsPerIdentity
      });
    } else {
      this.eventForm.patchValue({
        noMaxBookingsPerIdentity: true,
        maxBookingsPerIdentity: event.maxBookingsPerIdentity
      });
    }
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

    const eventFormRawData = this.eventForm.getRawValue();

    eventRequestDto.eventName = eventFormRawData.eventName;
    eventRequestDto.eventDescription = eventFormRawData.eventDescription;
    eventRequestDto.eventLocationAddress = eventFormRawData.eventLocationAddress;
    eventRequestDto.includePosition = eventFormRawData.includePosition;
    eventRequestDto.latitude = eventFormRawData.latitude;
    eventRequestDto.longitude = eventFormRawData.longitude;
    eventRequestDto.maxBookingsPerIdentity = eventFormRawData.maxBookingsPerIdentity;
    eventRequestDto.eventType = eventFormRawData.eventType;

    if (this.mode == 'CREATE') {
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
    if (this.mode == 'UPDATE' && this.eventId != null) {
      this.eventService.putEventById(this.eventId, eventRequestDto).subscribe({
        next: (res) => {
          console.log('Event Updated Successfully.', res);
          alert("Event Update Sucessfully.");
          this.eventService.triggerRefresh();
          this.closeWizard();
        },
        error: (err) => {
          console.error('Event fail to update');
        }
      });
    }

  }

  closeWizard() {
    this.close.emit();
  }
}
