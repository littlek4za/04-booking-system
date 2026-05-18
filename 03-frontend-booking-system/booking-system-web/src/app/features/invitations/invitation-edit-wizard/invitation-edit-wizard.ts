import { Component, EventEmitter, inject, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { Subject, takeUntil } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { SlotService } from '@features/slots/slot-service';
import { logControls, logFormErrors } from '@shared/utils/logging-utils';
import { InvitationRequestDto } from '../dtos/invitation-request-dto';
import { maxUsagePerIdentityExceedMaxUsage } from '@shared/validators/custom-validator';
import { InvitationService } from '../invitation-service';
import { SlotResponseDto } from '@features/slots/dtos/slot-response-dto';
import { LoggerService } from '@core/services/logger-service';

@Component({
  selector: 'app-invitation-edit-wizard',
  imports: [ReactiveFormsModule],
  templateUrl: './invitation-edit-wizard.html',
  styleUrl: './invitation-edit-wizard.css',
})
export class InvitationEditWizard implements OnInit, OnDestroy, OnChanges {

  private slotService = inject(SlotService);
  private logger = inject(LoggerService);

  @Input() mode!: 'CREATE';
  @Input() eventId!: number;
  @Input() eventType!: EventTypeModel;
  @Input() slotId: number | null = null;
  @Output() close = new EventEmitter<void>;

  //form field
  invitationForm!: FormGroup;
  readonly includeMode = SlotIncludeMode;
  today: string = '';

  //signal from service
  slotList = toSignal(this.slotService.slotListByEventId$, { initialValue: [] });

  //data from service
  slotSingle?: SlotResponseDto;

  private destroy$ = new Subject<void>();

  constructor(private invitationService: InvitationService) {
    this.initInvitationForm();
  }

  ngOnChanges(changes: SimpleChanges): void {

    if (!this.invitationForm) return;

    if (this.slotId != null &&
      this.eventId != null &&
      (changes['slotId'] || changes['eventId'])) {
      this.logger.debug(`[InvitationEditWizard] Changes detected for input "eventId" and "slotId"`);
      this.logger.debug(`[InvitationEditWizard] Sending slotService.getSlotByIdAndEventId request`);
      this.slotService.getSlotByIdAndEventId(this.eventId, this.slotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (res) => {
            this.slotSingle = res;
            this.prefillInvitationFormForSingleSlotUsage();
          },
          error: () => {
            alert('Fail to load slot info. Please try again. If the problem persists, please contact the administrator.')
          }
        })
      return;
    }

    if (changes['eventId'] && this.eventId) {
      this.logger.debug(`[InvitationEditWizard] Changes detected for input "eventId"`);
      this.logger.debug(`[InvitationEditWizard] Sending slotService.triggerRefreshForSlotListByEventId request`);
      this.slotService.triggerRefreshForSlotListByEventId(this.eventId);
    }
  }

  ngOnInit(): void {
    this.actionWhenFormValueChanges();
    this.today = new Date().toISOString().split('T')[0];
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initInvitationForm() {
    this.invitationForm = new FormGroup({
      noExpiry: new FormControl<boolean>(true, [Validators.required]),
      expiresAt: new FormControl<Date | null>(null),
      noMaxUsage: new FormControl<boolean>(true, [Validators.required]),
      maxUsage: new FormControl<number | null>(null),
      noMaxUsagePerIdentity: new FormControl<boolean>(true, [Validators.required]),
      maxUsagePerIdentity: new FormControl<number | null>(null),
      slotIncludeMode: new FormControl<SlotIncludeMode>(SlotIncludeMode.ALL_AND_FUTURE),
      selectedSlotIds: new FormControl<number[]>([]),
      requiredLogin: new FormControl<boolean>(true, [Validators.required]),
    }, {
      validators: maxUsagePerIdentityExceedMaxUsage
    });
    this.logger.debug(`[InvitationEditWizard] Invitation form initiated`);
  }

  private actionWhenFormValueChanges() {
    this.invitationForm.get('slotIncludeMode')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.logger.debug(`[InvitationEditWizard] Formcontrol 'slotIncludeMode' value changes detected`);
        this.onSlotIncludeModeChanges(value)
      });

    this.invitationForm.get('noExpiry')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.logger.debug(`[InvitationEditWizard] Formcontrol 'noExpiry' value changes detected`);
        this.onNoExpiryChanges(value)
      });

    this.invitationForm.get('noMaxUsage')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.logger.debug(`[InvitationEditWizard] Formcontrol 'noMaxUsage' value changes detected`);
        this.onNoMaxUsageChanges(value)
      });

    this.invitationForm.get('noMaxUsagePerIdentity')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.logger.debug(`[InvitationEditWizard] Formcontrol 'noMaxUsagePerIdentity' value changes detected`);
        this.onNoMaxUsagePerIdentityChanges(value)
      });
  }

  get isCustomSlot(): boolean {
    return this.invitationForm.getRawValue().slotIncludeMode === SlotIncludeMode.SELECTED;
  }

  private onSlotIncludeModeChanges(value: SlotIncludeMode): void {
    this.invitationForm.get('slotIncludeMode')?.markAsTouched();
    const selectedSlotIdsControls = this.invitationForm.get('selectedSlotIds');
    if (value == SlotIncludeMode.SELECTED) {
      selectedSlotIdsControls?.setValidators([Validators.required]);
      selectedSlotIdsControls?.markAsTouched();
    } else {
      selectedSlotIdsControls?.clearValidators();
      selectedSlotIdsControls?.setValue([]);
    }

    this.invitationForm.get('selectedSlotIds')?.updateValueAndValidity();
  }

  get hasExpiry(): boolean {
    return this.invitationForm.getRawValue().noExpiry === false;
  }

  private onNoExpiryChanges(value: boolean): void {
    if (value == true) {
      this.invitationForm.get('expiresAt')?.clearValidators();
      this.invitationForm.get('expiresAt')?.updateValueAndValidity();
      this.invitationForm.get('expiresAt')?.patchValue(null);
    } else {
      this.invitationForm.get('expiresAt')?.setValidators([Validators.required]);
      this.invitationForm.get('expiresAt')?.updateValueAndValidity();
      this.invitationForm.get('expiresAt')?.markAllAsTouched();
    }
  }

  get hasMaxUsage(): boolean {
    return this.invitationForm.getRawValue().noMaxUsage === false;
  }

  private onNoMaxUsageChanges(value: boolean): void {
    this.invitationForm.get('noMaxUsage')?.markAsTouched();
    if (value == true) {
      this.invitationForm.get('maxUsage')?.clearValidators();
      this.invitationForm.get('maxUsage')?.setValue(null);

    } else {
      this.invitationForm.get('maxUsage')?.setValidators([Validators.required, Validators.min(1)]);
    }
    this.invitationForm.get('maxUsage')?.updateValueAndValidity();
  }

  get hasMaxUsagePerIdentity(): boolean {
    return this.invitationForm.getRawValue().noMaxUsagePerIdentity === false;
  }

  private onNoMaxUsagePerIdentityChanges(value: boolean): void {
    this.invitationForm.get('noMaxUsagePerIdentity')?.markAsTouched();
    if (value == true) {
      this.invitationForm.get('requiredLogin')?.enable();
      this.invitationForm.get('maxUsagePerIdentity')?.clearValidators();
      this.invitationForm.updateValueAndValidity();
      this.invitationForm.get('maxUsagePerIdentity')?.setValue(null);
    } else {
      this.invitationForm.get('maxUsagePerIdentity')?.setValidators([Validators.required, Validators.min(1)]);
      this.invitationForm.updateValueAndValidity();
      // this.invitationForm.get('requiredLogin')?.patchValue(true);
      // this.invitationForm.get('requiredLogin')?.disable();
    }
  }

  prefillInvitationFormForSingleSlotUsage() {
    this.logger.debug(`[InvitationEditWizard] Prefilling invitation form for single slot usage`);
    this.invitationForm.get('selectedSlotIds')?.patchValue([this.slotId]);
    this.invitationForm.get('slotIncludeMode')?.patchValue(this.includeMode.SELECTED);
    this.invitationForm.get('slotIncludeMode')?.disable();
  }

  onSlotToggle(slotId: number, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    const selectedSlotIdsControl = this.invitationForm.get('selectedSlotIds')!;
    const current: number[] = selectedSlotIdsControl.value ?? [];

    if (checked) {
      if (!current.includes(slotId)) {
        selectedSlotIdsControl.patchValue([...current, slotId]);
      }
    } else {
      selectedSlotIdsControl.patchValue(current.filter(id => id !== slotId));
    }

    selectedSlotIdsControl.markAsTouched();
    selectedSlotIdsControl.updateValueAndValidity();
  }

  closeInvitationWizard() {
    this.close.emit();
  }


  onSubmit() {
    this.logger.debug('[InvitationEditWizard] Invitation form submitted');
    this.invitationForm.markAllAsTouched();
    if (this.invitationForm.invalid) {
      this.logger.warn('[InvitationEditWizard] Invitation form invalid');
      logFormErrors(this.invitationForm, this.logger);
      return;
    }

    logControls(this.invitationForm, this.logger);

    const invitationRequestDto = new InvitationRequestDto;
    invitationRequestDto.maxUsage = this.mapNullToUndefined(this.invitationForm.get('maxUsage')?.value);
    invitationRequestDto.maxUsagePerIdentity = this.mapNullToUndefined(this.invitationForm.get('maxUsagePerIdentity')?.value);
    invitationRequestDto.requiredLogin = this.invitationForm.get('requiredLogin')?.value;
    invitationRequestDto.slotIdList = this.invitationForm.get('selectedSlotIds')?.value;
    if (this.mode === 'CREATE' && this.eventId != null && this.eventType != null && this.slotId != null) {
      invitationRequestDto.slotIncludeMode = this.invitationForm.getRawValue().slotIncludeMode;
    } else {
      invitationRequestDto.slotIncludeMode = this.invitationForm.get('slotIncludeMode')?.value;
    }


    // update date with 23:59
    const dateStr = this.invitationForm.value.expiresAt;
    invitationRequestDto.expiresAt = this.mapNullToUndefined(
      dateStr
        ? new Date(`${dateStr}T23:59:59.999`).toISOString()
        : null
    );

    this.logger.debug('[InvitationEditWizard] Sending invitationService.createInvitation request');
    this.invitationService.createInvitation(invitationRequestDto, this.eventId).subscribe({
      next: () => {
        alert("Create invitation success");
        this.invitationService.triggerRefreshForInvitationListByEventId(this.eventId);
        this.closeInvitationWizard();
      },
      error: () => {
        alert('Create invitation failed. Please try again. If the problem persists, please contact the administrator.');
      }
    })

  }

  private mapNullToUndefined<T>(value: T | null): T | undefined {
    return value === null ? undefined : value;
  }

  getSlotIncludeRemark(): string {
    const value = this.invitationForm.get('slotIncludeMode')?.value;

    switch (value) {
      case this.includeMode.ALL_AND_FUTURE:
        return 'All existing slots will be included, along with any new slots created in the future.';

      case this.includeMode.ALL_CURRENT:
        return 'Only currently available slots will be included. Future slots will not be included.';

      case this.includeMode.SELECTED:
        return '';

      default:
        return '';
    }
  }
}
