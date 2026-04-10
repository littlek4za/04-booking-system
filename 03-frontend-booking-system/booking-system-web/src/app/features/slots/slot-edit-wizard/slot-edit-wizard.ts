import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotRequestDto } from '../dtos/slot-request-dto';
import { dateTimeRangeValidator, divisibleBy5Validator, timeOverlapValidator, timeRangeValidatorForBusiness, timeRangeValidatorForFlexible } from '@shared/validators/custom-validator';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { merge, Subject, takeUntil } from 'rxjs';
import { TimeRange } from '@shared/model/time-range';
import { SlotService } from '../slot-service';
import { SlotResponseDto } from '../dtos/slot-response-dto';
import { A11yModule } from "@angular/cdk/a11y";
import { logFormErrors } from '@shared/utils/logging-utils';
import { TimeZoneService } from '@shared/model/time-zone-service';
import { TimeZoneOption } from '@shared/model/time-zone-option';
import { MatNativeDateModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';



@Component({
  selector: 'app-slot-edit-wizard',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MatDatepickerModule, MatNativeDateModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatAutocompleteModule, A11yModule],
  templateUrl: './slot-edit-wizard.html',
  styleUrl: './slot-edit-wizard.css',
})
export class SlotEditWizard implements OnInit, OnChanges, OnDestroy {

  @Output() close = new EventEmitter<void>();
  @Input() eventId!: number;
  @Input() eventType!: string;
  @Input() mode!: 'CREATE' | 'UPDATE';
  @Input() slotId: number | null = null;

  // form
  slotForm!: FormGroup;
  businessDaysHoursForm!: FormGroup;
  flexibleDaysHoursForm!: FormGroup;

  // html show/hide
  showCustomInterval = false;
  showCustomFreq = false;

  // input data
  slotForUpdate?: SlotResponseDto;
  userTimeZone!: string;

  // field
  protected readonly EventType = EventTypeModel;
  readonly SLOT_INTERVAL_PRESETS = [5, 10, 15, 30, 60, 120];
  readonly SLOT_FREQUENCY_PRESETS = [5, 10, 15, 30, 60, 120];

  //field for FIXED event
  starttimeOptionForFixedEvent: string[] = [];
  endTimeOptionsForFixedEvent: { value: string, disabled: boolean }[] = [];

  //field for FLEXIBLE evet
  startTimeOptionForFlexibleEvent: string[] = [];

  //field for BUSINESS event
  dayNames: string[] = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  timezones: TimeZoneOption[] = [];
  startTimeOptionForBusinessEvent: string[] = [];

  // destroy
  private destroy$ = new Subject<void>();
  private warningChecksDestroy$ = new Subject<void>();

  // warning when update
  showUpdateWarning: boolean = false;
  private initialIntervalType: string | null = null;

  constructor(private formBuilder: FormBuilder, private slotService: SlotService, private timeZoneService: TimeZoneService) {
    this.userTimeZone = this.timeZoneService.getUserTimeZone();
    this.timezones = this.timeZoneService.getAllTimeZones();
    this.starttimeOptionForFixedEvent = this.generateTimeOption(5);
    this.startTimeOptionForFlexibleEvent = this.generateTimeOption(5);
    this.startTimeOptionForBusinessEvent = this.generateTimeOption(5);
    this.initSlotForm();

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['slotId'] && this.mode === 'UPDATE' && this.slotId) {
      this.loadSlotForUpdate();
    }
  }

  ngOnInit(): void {
    this.applyEventType(); // require input to load, so place this method in ngoninit is best place, if in constructor, input not loaded
    this.actionWhenFormValueChanges();
  }

  ngOnDestroy(): void {
    this.warningChecksDestroy$.next();
    this.warningChecksDestroy$.complete();
    this.destroy$.next();
    this.destroy$.complete();
  }
  //ngOnInit step 1.1
  private initSlotForm() {
    this.slotForm = new FormGroup({
      slotName: new FormControl<string>(""),
      slotDescription: new FormControl<string | null>(null),
      noMaxBookingsPerIdentity: new FormControl<boolean>(true),
      maxBookingsPerIdentity: new FormControl<number | null>(null),
      startDate: new FormControl<Date | null>(null),
      endDate: new FormControl<Date | null>(null),
      startTime: new FormControl<string | null>(null),
      endTime: new FormControl<string | null>(null),
      maxBookPerInterval: new FormControl<number | null>(null),
      intervalType: new FormControl<string | null>(""),
      slotIntervalMinutes: new FormControl<number | null>(null),
      frequencyType: new FormControl<string | null>(""),
      slotFrequencyIntervalMinutes: new FormControl<number | null>(null),
      businessAllowOt: new FormControl<boolean | null>(null),
      businessTimeZone: new FormControl<string | null>(null),
    });
  }

  //ngOnInit step 2 applyEventType()
  private applyEventType() {
    this.resetSlotFormState();
    this.configureCommonFormControls();
    switch (this.eventType) {
      case EventTypeModel.FIXED:
        this.configureFormControlsForFixed();
        break;
      case EventTypeModel.FLEXIBLE:
        this.configureFormControlsForFlexible();
        break;
      case EventTypeModel.BUSINESS:
        this.configureFormControlsForBusiness();
        break;
      default:
        console.warn('Unknown event type:', this.eventType);
        alert('Unexpected Error Occured.');
        this.disableSlotForm(); // or show message
        return;
    }
    this.slotForm.updateValueAndValidity({ emitEvent: false });
  }

  private resetSlotFormState() {
    this.slotForm.reset({}, { emitEvent: false });
    this.slotForm.patchValue({
      noMaxBookingsPerIdentity: true
    }, { emitEvent: false });
    Object.values(this.slotForm.controls).forEach(control => {
      control.enable({ emitEvent: false });
      control.clearValidators();
    });
  }

  private configureCommonFormControls() {
    this.slotForm.get('slotName')?.addValidators([Validators.required, Validators.minLength(1), Validators.maxLength(350)]);
    this.slotForm.get('slotDescription')?.addValidators([Validators.maxLength(2500)]);
    this.slotForm.get('noMaxBookingsPerIdentity')?.addValidators([Validators.required]);
  }

  private configureFormControlsForFixed() {
    this.enable(['maxBookPerInterval', 'startTime', 'endTime', 'startDate', 'endDate']);
    this.disable(['slotIntervalMinutes', 'slotFrequencyIntervalMinutes']);
    this.slotForm.setValidators(dateTimeRangeValidator);
    this.slotForm.get('maxBookPerInterval')?.addValidators([Validators.required, Validators.min(1)]);
    this.slotForm.get('startDate')?.addValidators([Validators.required]);
    this.slotForm.get('startTime')?.addValidators([Validators.required]);
    this.slotForm.get('endDate')?.addValidators([Validators.required]);
    this.slotForm.get('endTime')?.addValidators([Validators.required]);
  }

  private configureFormControlsForFlexible() {
    this.initFlexibleDaysHoursForm();
    this.enable(['slotIntervalMinutes', 'slotFrequencyIntervalMinutes']);
    this.disable(['maxBookPerInterval', 'startTime', 'endTime', 'startDate', 'endDate']);
    this.slotForm.get('slotIntervalMinutes')?.addValidators([Validators.required, Validators.min(5), divisibleBy5Validator]);
    this.slotForm.get('intervalType')?.addValidators([Validators.required]);
    this.slotForm.get('slotFrequencyIntervalMinutes')?.addValidators([Validators.required, Validators.min(1), Validators.max(1440)]);

  }

  private configureFormControlsForBusiness() {
    this.initBusinessDaysHoursForm();
    this.enable(['slotIntervalMinutes', 'slotFrequencyIntervalMinutes']);
    this.disable(['startTime', 'endTime', 'startDate', 'endDate', 'maxBookPerInterval']);
    this.slotForm.get('slotIntervalMinutes')?.addValidators([Validators.required, Validators.min(5), Validators.max(1440), divisibleBy5Validator]);
    this.slotForm.get('intervalType')?.addValidators([Validators.required]);
    this.slotForm.get('slotFrequencyIntervalMinutes')?.addValidators([Validators.required, Validators.min(1), Validators.max(1440)]);
    this.slotForm.get('frequencyType')?.addValidators([Validators.required]);
    this.slotForm.get('businessAllowOt')?.addValidators([Validators.required]);
    this.slotForm.get('businessTimeZone')?.addValidators([Validators.required]);
    this.slotForm.patchValue({ businessTimeZone: this.userTimeZone });
  }

  private enable(fields: string[]) {
    fields.forEach(f => this.slotForm.get(f)?.enable());
  }

  private disable(fields: string[]) {
    fields.forEach(f => this.slotForm.get(f)?.disable());
  }

  private disableSlotForm() {
    this.slotForm.disable({ emitEvent: false });
  }

  //ngOnInit step 2.1
  private initFlexibleDaysHoursForm() {
    this.flexibleDaysHoursForm = this.formBuilder.group({
      intervals: this.formBuilder.array([])
    }, { validators: timeOverlapValidator }
    );

    if (this.mode === 'CREATE') {
      this.addFlexibleInterval();
    }
  }

  getFlexibleIntervals(): FormArray {
    return this.flexibleDaysHoursForm.get('intervals') as FormArray;
  }

  getFlexibleInterval(index: number): FormGroup {
    return this.getFlexibleIntervals().at(index) as FormGroup;
  }

  addFlexibleInterval() {
    const group =
      this.formBuilder.group({
        startTime: ['', Validators.required],
        startDate: ['', Validators.required],
        endTime: ['', Validators.required],
        endDate: ['', Validators.required],

        endTimeOptions: [[]]
      }, {
        validators: timeRangeValidatorForFlexible(() =>
          this.slotForm.get('slotIntervalMinutes')?.value)
      });

    this.getFlexibleIntervals().push(group);

    // this to update end time option to prevent user selecting earlier time compare to start time/date
    this.watcherForFlexibleInterval(group);

    this.flexibleDaysHoursForm.updateValueAndValidity();
  }

  private watcherForFlexibleInterval(group: FormGroup) {
    merge(
      group.get('startDate')!.valueChanges,
      group.get('startTime')!.valueChanges,
      group.get('endDate')!.valueChanges
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.updateEndTimeOptionsForFlexibleInterval(group));
  }

  updateEndTimeOptionsForFlexibleInterval(group: FormGroup<any>): void {
    const startDate = group.get('startDate')?.value;
    const startTime = group.get('startTime')?.value;
    const endDate = group.get('endDate')?.value;
    const slotIntervalMinutes = this.slotForm.getRawValue().slotIntervalMinutes;

    if (!startDate || !startTime || !endDate) return;

    const endTimeOptions = this.startTimeOptionForFlexibleEvent.map(t => {

      const startMinutes = this.timeToMinutes(startTime);
      const optionMinutes = this.timeToMinutes(t);

      const sameDay =
        (startDate as Date).toDateString() ===
        (endDate as Date).toDateString();

      let isInvalid: boolean;

      if (slotIntervalMinutes) {
        isInvalid =
          sameDay && (optionMinutes <= (startMinutes + slotIntervalMinutes - 1));
      } else {
        isInvalid =
          sameDay && (optionMinutes <= startMinutes);
      }

      if (isInvalid) {
        return { value: t, disabled: true };
      }
      return { value: t, disabled: false };
    });

    group.patchValue(
      { endTimeOptions: endTimeOptions },
      { emitEvent: false }
    )
  }

  removeFlexibleInterval(index: number) {
    this.getFlexibleIntervals().removeAt(index);
    this.flexibleDaysHoursForm.updateValueAndValidity();
  }

  //ngOnInit step 2.2
  private initBusinessDaysHoursForm() {
    this.businessDaysHoursForm = this.formBuilder.group({
      days: this.formBuilder.array([])
    });

    const daysArray = this.businessDaysHoursForm.get('days') as FormArray;

    for (let i = 0; i < 7; i++) {
      daysArray.push(
        this.formBuilder.group({
          day: [i],
          enabled: [true],
          intervals: this.formBuilder.array([])
        }, { validators: timeOverlapValidator })
      );
      const intervalsArray = daysArray.at(i).get('intervals') as FormArray;
      const enabledCtrl = daysArray.at(i).get('enabled');
      if (this.mode === 'CREATE') {
        this.addBusinessInterval(i);
      }
      this.setupBusinessDayEnabledWatcher(enabledCtrl, intervalsArray, i);
    }
  }

  private setupBusinessDayEnabledWatcher(enabledCtrl: any, intervalsArray: FormArray<any>, i: number) {
    enabledCtrl?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((enabled: boolean) => {
        if (enabled) {
          intervalsArray.enable({ emitEvent: false });

          if (intervalsArray.length === 0) {
            this.addBusinessInterval(i);
          }
        } else {
          intervalsArray.disable({ emitEvent: false });
        }
      });
  }

  getBusinessDays(): FormArray {
    return this.businessDaysHoursForm.get('days') as FormArray;
  }

  getBusinessDayIntervals(dayIndex: number): FormArray {
    return this.getBusinessDays().at(dayIndex).get('intervals') as FormArray;
  }

  getBusinessInterval(dayIndex: number, intervalIndex: number) {
    return this.getBusinessDayIntervals(dayIndex).at(intervalIndex);
  }

  getBusinessIntervalControl(dayIndex: number, intervalIndex: number, controlName: string) {
    return this.getBusinessDayIntervals(dayIndex).at(intervalIndex).get(controlName);
  }

  addBusinessInterval(dayIndex: number) {
    const group =
      this.formBuilder.group({
        open: ['', Validators.required],
        close: ['', Validators.required],
        closeTimeOptions: [[]]
      }, {
        validators: timeRangeValidatorForBusiness(() =>
          this.slotForm.get('slotIntervalMinutes')?.value)
      });

    this.getBusinessDayIntervals(dayIndex).push(group);

    this.watcherForBusinessInterval(group);

    this.businessDaysHoursForm.updateValueAndValidity({ emitEvent: false });

  }

  private watcherForBusinessInterval(group: FormGroup) {
    group.get('open')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.updateCloseTimeOptionsForBusinessInterval(group));

  }

  updateCloseTimeOptionsForBusinessInterval(group: FormGroup<any>): void {
    const startTime = group.get('open')?.value;
    const slotIntervalMinutes = this.slotForm.getRawValue().slotIntervalMinutes;

    if (!startTime) return;

    const closeTimeOptions = this.startTimeOptionForBusinessEvent.map(t => {
      const startMinutes = this.timeToMinutes(startTime);
      const optionMinutes = this.timeToMinutes(t);

      let isInvalid: boolean;

      if (slotIntervalMinutes) {
        isInvalid =
          optionMinutes <= (startMinutes + slotIntervalMinutes - 1);
      } else {
        isInvalid =
          optionMinutes <= startMinutes;
      }

      if (isInvalid) {
        return { value: t, disabled: true };
      }
      return { value: t, disabled: false };
    });

    group.patchValue(
      { closeTimeOptions: closeTimeOptions },
      { emitEvent: false }
    )
  }

  removeBusinessinterval(dayIndex: number, intervalIndex: number) {
    this.getBusinessDayIntervals(dayIndex).removeAt(intervalIndex);
  }


  //ngOnInit step 3
  private generateTimeOption(stepMinutes: number): string[] {
    let timeArray: string[] = [];
    for (let h = 0; h < 24; h++) {
      for (let m = 0; m < 60; m += stepMinutes) {
        const hh = h.toString().padStart(2, '0');
        const mm = m.toString().padStart(2, '0');
        timeArray.push(`${hh}:${mm}`);
      }
    }
    return timeArray;
  }

  //ngOnInit step 4 methodForFormValueChanges()
  actionWhenFormValueChanges() {
    this.slotForm.get('intervalType')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onIntervalChange(value));

    this.slotForm.get('frequencyType')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onFequencyChange(value));

    this.slotForm.get('noMaxBookingsPerIdentity')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => this.onNoMaxBookingsPerIdentityChanges(value));

    // Fixed Form usage - patch date
    this.slotForm.get('startDate')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(date => {
        if (!this.slotForm.get('endDate')?.value) {
          this.slotForm.patchValue({ endDate: date });
        }
      });

    // Fixed Form usage - update endTimeOption
    merge(
      this.slotForm.get('startDate')!.valueChanges,
      this.slotForm.get('endDate')!.valueChanges,
      this.slotForm.get('startTime')!.valueChanges
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.updateEndTimeOptionsForFixedEvent();
      });

    // Flexible Form usage - update Validity and endTimeOption
    // Business Form usage - update Validity and endTimeOption
    this.slotForm.get('slotIntervalMinutes')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.eventType === EventTypeModel.FLEXIBLE) {
          this.getFlexibleIntervals().controls.forEach((ctrl) => {
            const group = ctrl as FormGroup;
            group.updateValueAndValidity();
            this.updateEndTimeOptionsForFlexibleInterval(group);
          });
        }

        if (this.eventType === EventTypeModel.BUSINESS) {
          for (let i = 0; i < 7; i++) {
            this.getBusinessDayIntervals(i).controls.forEach((ctrl) => {
              const group = ctrl as FormGroup;
              group.updateValueAndValidity();
              this.updateCloseTimeOptionsForBusinessInterval(group);
            });
          }
        }
      });

  }

  private onIntervalChange(value: string) {
    this.slotForm.get('slotIntervalMinutes')?.markAsTouched();
    if (value === 'customInterval') {
      this.showCustomInterval = true;
      this.slotForm.get('slotIntervalMinutes')?.setValue(null);
    } else {
      this.showCustomInterval = false;
      this.slotForm.get('slotIntervalMinutes')?.setValue(Number(value));
    }
  }

  private onFequencyChange(value: string) {
    this.slotForm.get('slotFrequencyIntervalMinutes')?.markAsTouched();
    if (value === 'customFreq') {
      this.showCustomFreq = true;
      this.slotForm.get('slotFrequencyIntervalMinutes')?.setValue(null);
    } else {
      this.showCustomFreq = false;
      this.slotForm.get('slotFrequencyIntervalMinutes')?.setValue(Number(value));
    }
  }

  private onNoMaxBookingsPerIdentityChanges(value: any): void {
    this.slotForm.get('noMaxBookingsPerIdentity')?.markAsTouched();
    if (value == true) {
      this.slotForm.get('maxBookingsPerIdentity')?.clearValidators();
      this.slotForm.get('maxBookingsPerIdentity')?.setValue(null);
      this.slotForm.updateValueAndValidity({ emitEvent: false });
    } else {
      this.slotForm.get('maxBookingsPerIdentity')?.setValidators([Validators.required, Validators.min(1)]);
      this.slotForm.updateValueAndValidity({ emitEvent: false });
    }
  }

  // update selectable End Time for flexible and fixed type event
  private updateEndTimeOptionsForFixedEvent() {
    if (this.eventType !== EventTypeModel.FIXED) {
      return;
    }

    const f = this.slotForm.getRawValue();

    if (!f.startDate || !f.endDate || !f.startTime) {
      return;
    }

    this.endTimeOptionsForFixedEvent = this.starttimeOptionForFixedEvent.map(t => {

      const startMinutes = this.timeToMinutes(f.startTime);
      const optionMinutes = this.timeToMinutes(t);

      const sameDay =
        (f.startDate as Date).toDateString() ===
        (f.endDate as Date).toDateString();

      const isInvalid =
        sameDay && (optionMinutes <= startMinutes);

      if (isInvalid) {
        return { value: t, disabled: true };
      }

      return { value: t, disabled: false };
    });
  }

  private timeToMinutes(time: string): number {
    const [h, m] = time.split(':').map(Number);
    return h * 60 + m;
  }

  get hasMaxBookingsPerIdentity(): boolean {
    return this.slotForm.getRawValue().noMaxBookingsPerIdentity === false;
  }

  // Update 
  private loadSlotForUpdate() {
    if (this.slotId) {
      this.slotService.getSlotByIdAndEventId(this.eventId, this.slotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (res) => {
            console.log('GET Slot successfully', res);
            this.slotForUpdate = res;
            this.prefillFormForUpdate(this.slotForUpdate);
            this.setupUpdateTimeWarningChecks();
          },
          error: (err) => {
            console.log("GET Slot failed");
          }
        });
    }
  }

  private prefillFormForUpdate(slot: SlotResponseDto) {
    const start = new Date(slot.slotStartTime);
    const end = new Date(slot.slotEndTime);
    const intervalType = this.resolveIntervalType(slot.slotIntervalMinutes);
    this.initialIntervalType = intervalType; // for warning check, as initial value
    const frequencyType = this.resolveFrequencyType(slot.slotFrequencyIntervalMinutes);

    this.slotForm.patchValue({
      slotName: slot.slotName,
      slotDescription: slot.slotDescription,

      startDate: new Date(start.getFullYear(), start.getMonth(), start.getDate()),
      endDate: new Date(end.getFullYear(), end.getMonth(), end.getDate()),
      startTime: start.toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }),
      endTime: end.toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }),
      maxBookPerInterval: slot.maxBookPerInterval,
      intervalType: intervalType,
      slotIntervalMinutes: slot.slotIntervalMinutes,
      frequencyType: frequencyType,
      slotFrequencyIntervalMinutes: slot.slotFrequencyIntervalMinutes,
      businessTimeZone: slot.businessTimeZone,
      businessAllowOt: slot.businessAllowOt,
    }, { emitEvent: false });

    if (slot.maxBookingsPerIdentity != null) {
      this.slotForm.patchValue({
        noMaxBookingsPerIdentity: false,
        maxBookingsPerIdentity: slot.maxBookingsPerIdentity
      }, { emitEvent: false });
    } else {
      this.slotForm.patchValue({
        noMaxBookingsPerIdentity: true,
        maxBookingsPerIdentity: slot.maxBookingsPerIdentity
      }, { emitEvent: false });
    }

    if (this.eventType == EventTypeModel.FIXED) {
      this.setUpdateRulesForFixedEvent(slot);
    }

    if (this.eventType == EventTypeModel.FLEXIBLE) {
      this.prefillFlexibleDaysHoursForm(slot);
    }

    if (this.eventType == EventTypeModel.BUSINESS) {
      this.prefillBusinessDaysHoursForm(slot);
    }

    this.showUpdateWarning = false;
  }

  private setUpdateRulesForFixedEvent(slot: SlotResponseDto) {
    if (slot.bookingsCount >= 1) {
      this.slotForm.get('startDate')?.disable();
      this.slotForm.get('startTime')?.disable();
      this.slotForm.get('endDate')?.disable();
      this.slotForm.get('endTime')?.disable();
    }
  }

  private prefillFlexibleDaysHoursForm(slot: SlotResponseDto) {
    const intervalsArray = this.getFlexibleIntervals() as FormArray;
    intervalsArray.clear();

    const flexibleDaysHoursData = slot.flexibleDaysHours;

    flexibleDaysHoursData?.forEach(item => {

      const start = new Date(item.open);
      const end = new Date(item.close);

      const group =
        this.formBuilder.group({
          startTime: ['', Validators.required],
          startDate: [null as Date | null, Validators.required],
          endTime: ['', Validators.required],
          endDate: [null as Date | null, Validators.required],

          endTimeOptions: [[]]
        }, {
          validators: timeRangeValidatorForFlexible(() =>
            this.slotForm.get('slotIntervalMinutes')?.value)
        });

      intervalsArray.push(group);

      // this to update end time option to prevent user selecting earlier time compare to start time/date
      this.watcherForFlexibleInterval(group);

      group.patchValue(
        {
          startDate: new Date(start.getFullYear(), start.getMonth(), start.getDate()),
          startTime: start.toLocaleTimeString('en-GB', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }),
          endDate: new Date(end.getFullYear(), end.getMonth(), end.getDate()),
          endTime: end.toLocaleTimeString('en-GB', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          })
        }, { emitEvent: true }
      );

      group.markAllAsTouched();
    });

    this.flexibleDaysHoursForm.updateValueAndValidity({ emitEvent: false });
  }

  private prefillBusinessDaysHoursForm(slot: SlotResponseDto) {
    const daysArray = this.getBusinessDays();

    for (let dayIndex = 0; dayIndex < daysArray.length; dayIndex++) {
      const dayGroup = daysArray.at(dayIndex);
      const intervalsArray = dayGroup.get('intervals') as FormArray;
      intervalsArray.clear();

      const slotDayIntervals = slot.businessDaysHours?.[dayIndex] ?? [];

      if (slotDayIntervals.length > 0) {
        dayGroup.get('enabled')?.setValue(true, { emitEvent: false });

        slotDayIntervals.forEach(item => {
          intervalsArray.push(
            this.formBuilder.group(
              {
                open: [item.open, Validators.required],
                close: [item.close, Validators.required]
              },
              {
                validators: timeRangeValidatorForBusiness(
                  () => this.slotForm.get('slotIntervalMinutes')?.value
                )
              }
            )
          );
        });

        intervalsArray.enable({ emitEvent: false });

      } else {
        dayGroup.get('enabled')?.setValue(false, { emitEvent: false });
        intervalsArray.disable({ emitEvent: false });
      }
    }

    this.businessDaysHoursForm.updateValueAndValidity({ emitEvent: false });
  }

  private resolveIntervalType(slotIntervalMinutes: number | null): string {
    if (slotIntervalMinutes == null) {
      return '';
    }
    return this.SLOT_INTERVAL_PRESETS.includes(slotIntervalMinutes) ? String(slotIntervalMinutes) : 'customInterval';
  }

  private resolveFrequencyType(slotFrequencyIntervalMinutes?: number): string {
    if (slotFrequencyIntervalMinutes == null) {
      return '';
    }
    if (slotFrequencyIntervalMinutes == undefined) {
      return '';
    }
    return this.SLOT_FREQUENCY_PRESETS.includes(slotFrequencyIntervalMinutes) ? String(slotFrequencyIntervalMinutes) : 'customFreq';
  }

  private setupUpdateTimeWarningChecks() {
    if (this.mode !== 'UPDATE' || !this.slotForUpdate) return;

    this.warningChecksDestroy$.next();
    this.warningChecksDestroy$ = new Subject<void>();

    if (this.eventType === EventTypeModel.FIXED) {
      ['startDate', 'endDate', 'startTime', 'endTime'].forEach(field => {
        this.slotForm.get(field)?.valueChanges
          .pipe(takeUntil(this.destroy$), takeUntil(this.warningChecksDestroy$))
          .subscribe(value => {
            this.checkFixedWarning();
          });
      });
    }

    if (this.eventType === EventTypeModel.FLEXIBLE) {
      this.slotForm.get('slotIntervalMinutes')?.valueChanges
        .pipe(takeUntil(this.destroy$), takeUntil(this.warningChecksDestroy$))
        .subscribe(() => this.checkFlexibleWarning());
      this.slotForm.get('intervalType')!.valueChanges
        .pipe(takeUntil(this.destroy$), takeUntil(this.warningChecksDestroy$))
        .subscribe(value => this.checkFlexibleWarning());
      this.flexibleDaysHoursForm.valueChanges
        .pipe(takeUntil(this.destroy$), takeUntil(this.warningChecksDestroy$))
        .subscribe(() => this.checkFlexibleWarning());
    }

    if (this.eventType === EventTypeModel.BUSINESS) {
      this.slotForm.get('slotIntervalMinutes')?.valueChanges
        .pipe(takeUntil(this.destroy$), takeUntil(this.warningChecksDestroy$))
        .subscribe(() => this.checkBusinessWarning());
      this.businessDaysHoursForm.valueChanges
        .pipe(takeUntil(this.destroy$), takeUntil(this.warningChecksDestroy$))
        .subscribe(() => this.checkBusinessWarning());
    }
  }

  private checkFixedWarning() {
    if (!this.slotForUpdate || this.slotForUpdate.bookingsCount === 0) {
      this.showUpdateWarning = false;
      return;
    }

    const form = this.slotForm.value;

    const originalStart = this.toLocalDateAndTimeParts(this.slotForUpdate.slotStartTime);
    const originalEnd = this.toLocalDateAndTimeParts(this.slotForUpdate.slotEndTime);

    const startChanged =
      !form.startDate ||
      !form.startTime ||
      form.startDate.getTime() !== originalStart.date.getTime() ||
      form.startTime !== originalStart.time;

    const endChanged =
      !form.endDate ||
      !form.endTime ||
      form.endDate.getTime() !== originalEnd.date.getTime() ||
      form.endTime !== originalEnd.time;

    this.showUpdateWarning = startChanged || endChanged;
  }

  private checkFlexibleWarning(): void {
    if (!this.slotForUpdate || this.slotForUpdate.bookingsCount === 0) {
      this.showUpdateWarning = false;
      return;
    }

    const intervalChanged = this.slotForm.value.slotIntervalMinutes !== this.slotForUpdate.slotIntervalMinutes;
    const intervalTypeChanged = this.slotForm.value.intervalType !== this.initialIntervalType;

    const newIntervals = this.getFlexibleIntervals().getRawValue()
      .filter(i => i.startDate && i.startTime && i.endDate && i.endTime)
      .map(i => {
        const start = this.combineDateAndTime(i.startDate, i.startTime);
        const end = this.combineDateAndTime(i.endDate, i.endTime);

        return {
          open: start.toISOString(),
          close: end.toISOString()
        };
      });

    const originalIntervals = (this.slotForUpdate.flexibleDaysHours || []).map(i => ({
      open: new Date(i.open).toISOString(),
      close: new Date(i.close).toISOString()
    }));

    const rangesChanged = this.isIntervalsDifferent(newIntervals, originalIntervals);

    this.showUpdateWarning = intervalTypeChanged || intervalChanged || rangesChanged;
  }

  private checkBusinessWarning(): void {
    if (!this.slotForUpdate || this.slotForUpdate.bookingsCount === 0) {
      this.showUpdateWarning = false;
      return;
    }

    const intervalChanged = this.slotForm.value.slotIntervalMinutes !== this.slotForUpdate.slotIntervalMinutes;
    const intervalTypeChanged = this.slotForm.value.intervalType !== this.initialIntervalType;

    // Flatten the form intervals
    const newIntervals: TimeRange[] = [];
    (this.businessDaysHoursForm.value.days || []).forEach((day: any) => {
      if (day.enabled) {
        day.intervals.forEach((i: any) => newIntervals.push({ open: i.open, close: i.close }));
      }
    });

    // Flatten original intervals
    const originalIntervals: TimeRange[] = [];
    Object.values(this.slotForUpdate.businessDaysHours || {}).forEach((arr: TimeRange[]) => {
      arr.forEach(i => originalIntervals.push({ open: i.open, close: i.close }));
    });

    const rangesChanged = this.isIntervalsDifferent(newIntervals, originalIntervals);


    this.showUpdateWarning = intervalTypeChanged || intervalChanged || rangesChanged;
  }

  private isIntervalsDifferent(arr1: TimeRange[], arr2: TimeRange[]): boolean {
    if (!arr1 || !arr2 || arr1.length !== arr2.length) return true;
    for (let i = 0; i < arr1.length; i++) {
      if (arr1[i].open !== arr2[i].open || arr1[i].close !== arr2[i].close) return true;
    }
    return false;
  }

  private toLocalDateAndTimeParts(value: string) {
    const date = new Date(value);

    return {
      date: new Date(date.getFullYear(), date.getMonth(), date.getDate()),
      time: date.toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    };
  }

  onSubmit() {
    let isAllValid = true;

    this.slotForm.markAllAsTouched();
    if (this.slotForm.invalid) {
      logFormErrors(this.slotForm);
      isAllValid = false;
    }

    if (this.eventType == EventTypeModel.BUSINESS) {
      this.businessDaysHoursForm.markAllAsTouched();
      this.getFlexibleIntervals().controls.forEach(group => {
        group.updateValueAndValidity();
      });
      if (this.businessDaysHoursForm.invalid) {
        logFormErrors(this.businessDaysHoursForm);
        isAllValid = false;
      }
    }

    if (this.eventType == EventTypeModel.FLEXIBLE) {
      this.flexibleDaysHoursForm.markAllAsTouched();
      if (this.flexibleDaysHoursForm.invalid) {
        logFormErrors(this.flexibleDaysHoursForm);
        isAllValid = false;
      }
    }

    if (!isAllValid) {
      console.warn('Form Submission Field Invalid');
      return;
    }

    const slotRequestDto = this.buildSlotRequestDto();

    console.log('Before submite slotRequestDto', slotRequestDto);

    if (this.mode == 'CREATE') {
      this.slotService.createSlotByEventId(slotRequestDto, this.eventId).subscribe({
        next: (res) => {
          console.log('POST Request success', res);
          alert('Slot Created Succesfully.');
          this.slotService.triggerRefresh(this.eventId);
          this.closeWizard();
        },
        error: (err) => {
          console.error('Slot fail to create');
        }
      })
    } else if (this.mode == 'UPDATE') {

      if (this.slotId == null) {
        console.error('slotId is required in UPDATE mode');
        return;
      }

      this.slotService.putSlotByIdAndEventId(this.eventId, this.slotId, slotRequestDto).subscribe({
        next: (res) => {
          console.log('POST Request success', res);
          alert('Slot Updated Succesfully.');
          this.slotService.triggerRefresh(this.eventId);
          this.closeWizard();
        },
        error: (err) => {
          console.error('Slot fail to update');
        }
      })
    } else {
      console.error('Mode only support create and update. Please check internal code.');
      alert('Internal Error Occur.');
      this.closeWizard();
    }
  }

  private buildSlotRequestDto(): SlotRequestDto {
    const slotRequestDto = new SlotRequestDto();
    const rawSlotFormData = this.slotForm.getRawValue();

    slotRequestDto.eventId = this.eventId;
    slotRequestDto.slotName = rawSlotFormData.slotName;
    slotRequestDto.slotDescription = rawSlotFormData.slotDescription;
    slotRequestDto.maxBookingsPerIdentity = rawSlotFormData.maxBookingsPerIdentity;

    switch (this.eventType) {
      case EventTypeModel.FIXED:
        return this.buildFixedDto(slotRequestDto, rawSlotFormData);
      case EventTypeModel.FLEXIBLE:
        return this.buildFlexibleDto(slotRequestDto, rawSlotFormData);
      case EventTypeModel.BUSINESS:
        return this.buildBusinessDto(slotRequestDto, rawSlotFormData);
      default:
        throw new Error('Unsupported event type');
    }
  }

  buildFixedDto(slotRequestDto: SlotRequestDto, rawSlotFormData: any): SlotRequestDto {
    const startTime = rawSlotFormData.startTime;
    const endTime = rawSlotFormData.endTime;
    const startDate = rawSlotFormData.startDate;
    const endDate = rawSlotFormData.endDate;
    const startTimeCombine = this.combineDateAndTime(startDate, startTime);
    const endTimeCombine = this.combineDateAndTime(endDate, endTime);

    slotRequestDto.slotStartTime = startTimeCombine.toISOString();
    slotRequestDto.slotEndTime = endTimeCombine.toISOString();
    slotRequestDto.maxBookPerInterval = rawSlotFormData.maxBookPerInterval;

    return slotRequestDto;
  }

  buildFlexibleDto(slotRequestDto: SlotRequestDto, rawSlotFormData: any): SlotRequestDto {
    slotRequestDto.slotIntervalMinutes = rawSlotFormData.slotIntervalMinutes;
    slotRequestDto.slotFrequencyIntervalMinutes = rawSlotFormData.slotFrequencyIntervalMinutes;

    const intervals = this.flexibleDaysHoursForm.get('intervals') as FormArray | null;
    const ranges = intervals ? intervals.getRawValue() : [];

    let flexibleDaysHoursData: TimeRange[] = [];
    for (const range of ranges) {
      const startDate = range.startDate;
      const startTime = range.startTime;
      const endDate = range.endDate;
      const endTime = range.endTime;

      const combineStartTime = this.combineDateAndTime(startDate, startTime);
      const combineEndTime = this.combineDateAndTime(endDate, endTime);

      flexibleDaysHoursData.push({
        open: combineStartTime.toISOString(),
        close: combineEndTime.toISOString()
      });
    }
    slotRequestDto.flexibleDaysHours = flexibleDaysHoursData;

    return slotRequestDto;
  }

  buildBusinessDto(slotRequestDto: SlotRequestDto, rawSlotFormData: any): SlotRequestDto {
    slotRequestDto.businessTimeZone = rawSlotFormData.businessTimeZone;
    slotRequestDto.businessAllowOt = rawSlotFormData.businessAllowOt;
    slotRequestDto.slotIntervalMinutes = rawSlotFormData.slotIntervalMinutes;
    slotRequestDto.slotFrequencyIntervalMinutes = rawSlotFormData.slotFrequencyIntervalMinutes;

    let businessDaysHoursData: Record<number, TimeRange[]> = {};
    const days = this.businessDaysHoursForm.value.days as any[];

    for (const day of days) {
      const dayIndex = day.day;
      const intervals: TimeRange[] = day.enabled
        ? day.intervals.map((i: any) => ({
          open: i.open,
          close: i.close
        }))
        : [];

      businessDaysHoursData[dayIndex] = intervals;
    }

    slotRequestDto.businessDaysHours = businessDaysHoursData;

    return slotRequestDto;
  }

  closeWizard() {
    this.close.emit();
  }

  // time conversion utils
  private combineDateAndTime(startDate: Date, startTime: string) {
    const [hours, minutes] = startTime.split(":").map(Number);

    const result = new Date(startDate);
    result.setHours(hours, minutes, 0, 0);

    return result;
  }

  private fromInstantToDateTimeLocal(isoString: string): string {
    const date = new Date(isoString);

    const pad = (n: number) => n.toString().padStart(2, '0');

    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());

    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

}


