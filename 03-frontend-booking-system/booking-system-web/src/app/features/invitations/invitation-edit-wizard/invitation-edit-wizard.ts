import { Component, EventEmitter, inject, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { Subject, takeUntil } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { SlotService } from '@features/slots/slot-service';
import { logControls, logFormErrors } from '@shared/utils/logging-utils';
import { InvitationRequestDto } from '../dtos/invitation-request-dto';
import { maxUsagePerUserExceedMaxUsage } from '@shared/validators/custom-validator';
import { InvitationService } from '../invitation-service';

@Component({
  selector: 'app-invitation-edit-wizard',
  imports: [ReactiveFormsModule],
  templateUrl: './invitation-edit-wizard.html',
  styleUrl: './invitation-edit-wizard.css',
})
export class InvitationEditWizard implements OnInit, OnDestroy, OnChanges {

  private slotService = inject(SlotService);

  @Input() mode!: 'CREATE' | 'VIEW';
  @Input() eventId!: number;
  @Input() eventType!: EventTypeModel;
  @Input() slotId: number | null = null;
  @Output() close = new EventEmitter<void>;

  //form field
  invitationForm!: FormGroup;
  readonly includeMode = SlotIncludeMode;
  today: string = '';

  //html boolean usage
  showCustomSlotSelection: boolean = false;
  showExpiryDateSelection: boolean = false;
  showMaxUsageSelection: boolean = false;
  showMaxUsagePerUserSelection: boolean = false;

  //signal from service
  slotList = toSignal(this.slotService.slot$, { initialValue: [] });

  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.initInvitationForm();
    this.actionWhenFormValueChanges();
    this.today = new Date().toISOString().split('T')[0];
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['eventId'] && this.eventId) {
      this.slotService.triggerRefresh(this.eventId);
    }
  }

  constructor(private invitationService: InvitationService){}

  private initInvitationForm() {
    this.invitationForm = new FormGroup({
      noExpiry: new FormControl<boolean>(true, [Validators.required]),
      expiresAt: new FormControl<Date | null>(null),
      noMaxUsage: new FormControl<boolean>(true, [Validators.required]),
      maxUsage: new FormControl<number | null>(null),
      noMaxUsagePerUser: new FormControl<boolean>(true, [Validators.required]),
      maxUsagePerUser: new FormControl<number | null>(null),
      slotIncludeMode: new FormControl<SlotIncludeMode>(SlotIncludeMode.ALL_AND_FUTURE),
      selectedSlotIds: new FormControl<number[]>([]),
      requiredLogin: new FormControl<boolean>(true, [Validators.required]),
    }, {
      validators: maxUsagePerUserExceedMaxUsage
    });
  }

  private actionWhenFormValueChanges() {
    this.invitationForm.get('slotIncludeMode')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onSlotIncludeModeChanges(value));

    this.invitationForm.get('noExpiry')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onNoExpiryChanges(value));

    this.invitationForm.get('noMaxUsage')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onNoMaxUsageChanges(value));

    this.invitationForm.get('noMaxUsagePerUser')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onNoMaxUsagePerUserChanges(value));
  }

  private onSlotIncludeModeChanges(value: SlotIncludeMode): void {
    this.invitationForm.get('slotIncludeMode')?.markAsTouched();
    const selectedSlotIdsControls = this.invitationForm.get('selectedSlotIds');
    if (value == SlotIncludeMode.SELECTED) {
      this.showCustomSlotSelection = true;
      selectedSlotIdsControls?.setValidators([Validators.required]);
      selectedSlotIdsControls?.markAsTouched(); 
    } else {
      this.showCustomSlotSelection = false;
      selectedSlotIdsControls?.clearValidators();
      selectedSlotIdsControls?.setValue([]);
    }

    this.invitationForm.get('selectedSlotIds')?.updateValueAndValidity();
  }

  private onNoExpiryChanges(value: boolean): void {
    if (value == true) {
      this.showExpiryDateSelection = false;
      this.invitationForm.get('expiresAt')?.clearValidators();
      this.invitationForm.get('expiresAt')?.updateValueAndValidity();
      this.invitationForm.get('expiresAt')?.patchValue(null);
    } else {
      this.showExpiryDateSelection = true;
      this.invitationForm.get('expiresAt')?.setValidators([Validators.required]);
      this.invitationForm.get('expiresAt')?.updateValueAndValidity();
      this.invitationForm.get('expiresAt')?.markAllAsTouched();
    }
  }

  private onNoMaxUsageChanges(value: boolean): void {
    this.invitationForm.get('noMaxUsage')?.markAsTouched();
    if (value == true) {
      this.showMaxUsageSelection = false;
      this.invitationForm.get('maxUsage')?.clearValidators();
      this.invitationForm.get('maxUsage')?.setValue(null);

    } else {
      this.showMaxUsageSelection = true;
      this.invitationForm.get('maxUsage')?.setValidators([Validators.required, Validators.min(1)]);
    }
    this.invitationForm.get('maxUsage')?.updateValueAndValidity();
  }

  private onNoMaxUsagePerUserChanges(value: boolean): void {
    this.invitationForm.get('noMaxUsagePerUser')?.markAsTouched();
    if (value == true) {
      this.showMaxUsagePerUserSelection = false;
      this.invitationForm.get('requiredLogin')?.enable();
      this.invitationForm.get('maxUsagePerUser')?.clearValidators();
      this.invitationForm.updateValueAndValidity();
      this.invitationForm.get('maxUsagePerUser')?.setValue(null);
    } else {
      this.showMaxUsagePerUserSelection = true;
      this.invitationForm.get('maxUsagePerUser')?.setValidators([Validators.required, Validators.min(1)]);
      this.invitationForm.updateValueAndValidity();
      this.invitationForm.get('requiredLogin')?.patchValue(true);
      this.invitationForm.get('requiredLogin')?.disable();
    }
  }

  onSlotToggle(slotId: number, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    const control = this.invitationForm.get('selectedSlotIds')!;
    const current: number[] = control.value ?? [];

    if (checked) {
      if (!current.includes(slotId)) {
        control.patchValue([...current, slotId]);
      }
    } else {
      control.patchValue(current.filter(id => id !== slotId));
    }

    control.markAsTouched();
    control.updateValueAndValidity();
  }

  closeInvitationWizard() {
    this.close.emit();
  }


  onSubmit() {
    this.invitationForm.markAllAsTouched();
    if (this.invitationForm.invalid) {
      console.warn('Form Submission Field Invalid');
      logFormErrors(this.invitationForm);
      return;
    }

    logControls(this.invitationForm);

    const invitationRequestDto = new InvitationRequestDto;
    invitationRequestDto.maxUsage = this.mapNullable(this.invitationForm.get('maxUsage')?.value);
    invitationRequestDto.maxUsagePerUser = this.mapNullable(this.invitationForm.get('maxUsagePerUser')?.value);
    invitationRequestDto.requiredLogin = this.invitationForm.get('requiredLogin')?.value;
    invitationRequestDto.slotIncludeMode = this.invitationForm.get('slotIncludeMode')?.value;
    invitationRequestDto.slotIdList = this.invitationForm.get('selectedSlotIds')?.value;

    // update date with 23:59
    const dateStr = this.invitationForm.value.expiresAt;
    invitationRequestDto.expiresAt = this.mapNullable(
      dateStr
      ? new Date(`${dateStr}T23:59:59.999`).toISOString()
      : null
    );

    console.log("InvitationRequestDto: ", invitationRequestDto);
    console.log("event Id", this.eventId);

    this.invitationService.createInvitation(invitationRequestDto,this.eventId).subscribe({
      next: (res) =>{
        console.log('Create invitation success', invitationRequestDto);
        alert("Create invitation success");
        this.invitationService.triggerRefreshForInvitationList(this.eventId);
        this.closeInvitationWizard();
      },
      error: (err) =>{
        console.warn('Create invitation failed');
      }
    })

  }

  private mapNullable<T>(value: T | null): T | undefined {
    return value === null? undefined : value;
  }


}
