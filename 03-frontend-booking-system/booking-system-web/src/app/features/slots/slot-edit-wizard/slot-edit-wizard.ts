import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotRequestDto } from '../dtos/slot-request-dto';
import { dateTimeRangeValidator, divisibleBy5Validator, timeOverlapValidator, timeRangeValidator } from '@shared/validators/custom-validator';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { Subject, takeUntil, timeInterval } from 'rxjs';
import { TimeRange } from '@shared/model/time-range';
import { SlotService } from '../slot-service';
import { SlotResponseDto } from '../dtos/slot-response-dto';
import { A11yModule } from "@angular/cdk/a11y";
import { logFormErrors } from '@shared/utils/logging-utils';
import moment from 'moment-timezone';
import { TimeZoneService } from '@shared/model/time-zone-service';
import { TimeZoneOption } from '@shared/model/time-zone-option';


@Component({
  selector: 'app-slot-edit-wizard',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MatDatepickerModule, MatNativeDateModule, MatFormFieldModule, MatInputModule, MatSelectModule, A11yModule],
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

  // html show
  showCustomInterval = false;
  showCustomFreq = false;

  // field
  timeOption: string[] = [];
  endTimeOptionsWithState: { value: string, disabled: boolean }[] = [];
  protected readonly EventType = EventTypeModel;
  dayNames: string[] = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  slotForUpdate?: SlotResponseDto;
  readonly SLOT_INTERVAL_PRESETS = [5, 10, 15, 30, 60, 120];
  readonly SLOT_FREQUENCY_PRESETS = [5, 10, 15, 30, 60, 120];
  submitted: boolean = false;
  timezones: TimeZoneOption[] = [];
  userTimeZone!: string;

  // destroy
  private destroy$ = new Subject<void>();

  constructor(private formBuilder: FormBuilder, private slotService: SlotService, private timeZoneService: TimeZoneService) { }

  ngOnInit(): void {
    this.userTimeZone = this.timeZoneService.getUserTimeZone();
    this.timezones = this.timeZoneService.getAllTimeZones();
    this.initSlotForm();
    this.applyEventType();
    this.timeOption = this.generateTimeOption(5);
    this.actionWhenFormValueChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['slotId'] && this.mode === 'UPDATE' && this.slotId) {
      this.loadSlot();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  //ngOnInit step 1.1
  private initSlotForm() {
    this.slotForm = new FormGroup({
      slotName: new FormControl<string>(""),
      slotDescription: new FormControl<string | null>(null),
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
    switch (this.eventType) {
      case EventTypeModel.FIXED:
        this.configureFixed();
        break;
      case EventTypeModel.FLEXIBLE:
        this.configureFlexible();
        break;
      case EventTypeModel.BUSINESS:
        this.configureBusiness();
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
    this.slotForm.reset();
    Object.values(this.slotForm.controls).forEach(control => {
      control.enable({ emitEvent: false });
      control.clearValidators();
    });
  }

  private configureFixed() {
    this.enable(['maxBookPerInterval', 'startTime', 'endTime', 'startDate', 'endDate']);
    this.disable(['slotIntervalMinutes', 'slotFrequencyIntervalMinutes']);
    this.slotForm.setValidators(dateTimeRangeValidator);
    this.slotForm.get('slotName')?.addValidators([Validators.required, Validators.minLength(1), Validators.maxLength(350)]);
    this.slotForm.get('slotDescription')?.addValidators([Validators.maxLength(2500)]);
    this.slotForm.get('maxBookPerInterval')?.addValidators([Validators.required, Validators.min(1)]);
    this.slotForm.get('startDate')?.addValidators([Validators.required]);
    this.slotForm.get('startTime')?.addValidators([Validators.required]);
    this.slotForm.get('endDate')?.addValidators([Validators.required]);
    this.slotForm.get('endTime')?.addValidators([Validators.required]);
  }

  private configureFlexible() {
    this.initFlexibleDaysHoursForm();
    this.enable(['slotIntervalMinutes', 'slotFrequencyIntervalMinutes']);
    this.disable(['maxBookPerInterval', 'startTime', 'endTime', 'startDate', 'endDate']);
    this.slotForm.setValidators(dateTimeRangeValidator);
    this.slotForm.get('slotName')?.addValidators([Validators.required, Validators.minLength(1), Validators.maxLength(350)]);
    this.slotForm.get('slotDescription')?.addValidators([Validators.maxLength(2500)]);
    this.slotForm.get('slotIntervalMinutes')?.addValidators([Validators.required, Validators.min(5), divisibleBy5Validator]);
    this.slotForm.get('intervalType')?.addValidators([Validators.required]);
    this.slotForm.get('slotFrequencyIntervalMinutes')?.addValidators([Validators.required, Validators.min(1), Validators.max(1440)]);

  }

  private configureBusiness() {
    this.initBusinessDaysHoursForm();
    this.enable(['slotIntervalMinutes', 'slotFrequencyIntervalMinutes']);
    this.disable(['startTime', 'endTime', 'startDate', 'endDate', 'maxBookPerInterval']);
    this.slotForm.get('slotName')?.addValidators([Validators.required, Validators.minLength(1), Validators.maxLength(350)]);
    this.slotForm.get('slotDescription')?.addValidators([Validators.maxLength(2500)]);
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
      this.addTimeInterval();
    }
  }

  getTimeIntervals(): FormArray {
    return this.flexibleDaysHoursForm.get('intervals') as FormArray;
  }

  addTimeInterval() {
    this.getTimeIntervals().push(
      this.formBuilder.group({
        open: ['', Validators.required], //format YYYY-MM-DDTHH:mm
        close: ['', Validators.required] //format YYYY-MM-DDTHH:mm
      }, {
        validators: timeRangeValidator(() =>
          this.slotForm.get('slotIntervalMinutes')?.value
        )
      })
    );
    this.flexibleDaysHoursForm.updateValueAndValidity({ emitEvent: false });
  }

  removeTimeInterval(index: number) {
    this.getTimeIntervals().removeAt(index);
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
        this.addIntervalGroup(i);
      }
      this.setupEnabledWatcher(enabledCtrl, intervalsArray, i);
    }
  }

  private setupEnabledWatcher(enabledCtrl: any, intervalsArray: FormArray<any>, i: number) {
    enabledCtrl?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((enabled: boolean) => {
        if (enabled) {
          intervalsArray.enable({ emitEvent: false });

          if (intervalsArray.length === 0) {
            intervalsArray.push(this.addIntervalGroup(i));
          }
        } else {
          intervalsArray.disable({ emitEvent: false });
        }
      });
  }

  getDaysArray(): FormArray {
    return this.businessDaysHoursForm.get('days') as FormArray;
  }

  getIntervalsArray(dayIndex: number): FormArray {
    return this.getDaysArray().at(dayIndex).get('intervals') as FormArray;
  }

  getIntervalGroup(dayIndex: number, intervalIndex: number) {
    return this.getIntervalsArray(dayIndex).at(intervalIndex);
  }

  getIntervalGroupControl(dayIndex: number, intervalIndex: number, controlName: string) {
    return this.getIntervalsArray(dayIndex).at(intervalIndex).get(controlName);
  }

  addIntervalGroup(dayIndex: number) {
    this.getIntervalsArray(dayIndex).push(
      this.formBuilder.group({
        open: ['', Validators.required],
        close: ['', Validators.required]
      }, { validators: timeRangeValidator() })
    );
    this.businessDaysHoursForm.updateValueAndValidity({ emitEvent: false });
  }

  removeIntervalGroup(dayIndex: number, intervalIndex: number) {
    this.getIntervalsArray(dayIndex).removeAt(intervalIndex);
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
    this.slotForm.get('startDate')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(date => {
        if (!this.slotForm.get('endDate')?.value) {
          this.slotForm.patchValue({ endDate: date });
        }
      });
    this.slotForm.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.updateEndTimeOptions();
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

  // update selectable End Time for flexible and fixed type event
  private updateEndTimeOptions() {
    const f = this.slotForm.value;
    const isFlexible = this.eventType === EventTypeModel.FLEXIBLE;
    const isFixed = this.eventType === EventTypeModel.FIXED;

    this.endTimeOptionsWithState = this.timeOption.map(t => {
      if (!f.startDate || !f.endDate || !f.startTime) {
        return { value: t, disabled: true };
      }

      if (isFlexible && !f.slotIntervalMinutes) {
        return { value: t, disabled: true };
      }

      const startMinutes = this.timeToMinutes(f.startTime);
      const optionMinutes = this.timeToMinutes(t);

      const sameDay =
        (f.startDate as Date).toDateString() ===
        (f.endDate as Date).toDateString();

      const isInvalid =
        sameDay &&
        (
          (isFlexible && optionMinutes < startMinutes + f.slotIntervalMinutes) ||
          (isFixed && optionMinutes <= startMinutes)
        );

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

  // Update 
  private loadSlot() {
    if (this.slotId) {
      this.slotService.getSlotByIdAndEventId(this.eventId, this.slotId).subscribe({
        next: (res) => {
          console.log('GET Slot successfully', res);
          this.slotForUpdate = res;
          this.prefillForm(this.slotForUpdate);
        },
        error: (err) => {
          console.log("GET Slot failed");
        }
      });
    }
  }

  private prefillForm(slot: SlotResponseDto) {
    const start = new Date(slot.slotStartTime);
    const end = new Date(slot.slotEndTime);
    const intervalType = this.resolveIntervalType(slot.slotIntervalMinutes);
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
    });

    if (this.eventType == EventTypeModel.BUSINESS) {
      this.prefillBusinessDaysHoursForm(slot);
    }

    if (this.eventType == EventTypeModel.FLEXIBLE) {
      this.prefillFlexibleDaysHoursForm(slot);
    }
  }

  private prefillFlexibleDaysHoursForm(slot: SlotResponseDto) {
    const intervalsArray = this.getTimeIntervals() as FormArray;
    intervalsArray.clear();

    const flexibleDaysHoursData = slot.flexibleDaysHours;

    flexibleDaysHoursData?.forEach(item => {
      intervalsArray.push(
        this.formBuilder.group(
          {
            open: [this.fromInstantToDateTimeLocal(item.open), Validators.required],
            close: [this.fromInstantToDateTimeLocal(item.close), Validators.required]
          }, {
          validators: timeRangeValidator(() => this.slotForm.get('slotIntervalMinutes')?.value
          )
        })
      );
    });

    this.flexibleDaysHoursForm.updateValueAndValidity({ emitEvent: false });
  }

  private prefillBusinessDaysHoursForm(slot: SlotResponseDto) {
    const daysArray = this.getDaysArray();

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
              { validators: timeRangeValidator() }
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

  onSubmit() {
    let isAllValid = true;

    this.slotForm.markAllAsTouched();
    if (this.slotForm.invalid) {
      logFormErrors(this.slotForm);
      isAllValid = false;
    }

    if (this.eventType == EventTypeModel.BUSINESS) {
      this.businessDaysHoursForm.markAllAsTouched();
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

    const slotRequestDto = new SlotRequestDto;
    if (this.eventType == EventTypeModel.FIXED) {
      const startTime = this.slotForm.value.startTime;
      const endTime = this.slotForm.value.endTime;
      const startDate = this.slotForm.value.startDate;
      const endDate = this.slotForm.value.endDate;
      const startTimeCombine = this.combineDateAndTime(startDate, startTime);
      const endTimeCombine = this.combineDateAndTime(endDate, endTime);

      slotRequestDto.slotStartTime = startTimeCombine.toISOString();
      slotRequestDto.slotEndTime = endTimeCombine.toISOString();
    }

    slotRequestDto.eventId = this.eventId;
    slotRequestDto.slotName = this.slotForm.value.slotName;
    slotRequestDto.slotDescription = this.slotForm.value.slotDescription;
    slotRequestDto.slotIntervalMinutes = this.slotForm.value.slotIntervalMinutes;
    slotRequestDto.slotFrequencyIntervalMinutes = this.slotForm.value.slotFrequencyIntervalMinutes;
    slotRequestDto.maxBookPerInterval = this.slotForm.value.maxBookPerInterval;

    // Flexible Type section

    if (this.eventType == EventTypeModel.FLEXIBLE) {

      const ranges = this.flexibleDaysHoursForm.get('intervals')?.value || [];
      let flexibleDaysHoursData: TimeRange[] = [];
      for (const range of ranges) {
        flexibleDaysHoursData.push({
          open: this.toInstantString(range.open),
          close: this.toInstantString(range.close)
        });
      }
      slotRequestDto.flexibleDaysHours = flexibleDaysHoursData;
    }
    // Business Type section

    if (this.eventType == EventTypeModel.BUSINESS) {
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
      console.log("done 2nd part");

      slotRequestDto.businessDaysHours = businessDaysHoursData;
      slotRequestDto.businessTimeZone = this.slotForm.value.businessTimeZone;
      slotRequestDto.businessAllowOt = this.slotForm.value.businessAllowOt;
    }
    console.log(slotRequestDto);

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

  private combineDateAndTime(startDate: Date, startTime: string) {
    const [hours, minutes] = startTime.split(":").map(Number);

    const result = new Date(startDate);
    result.setHours(hours, minutes, 0, 0);

    return result;
  }

  closeWizard() {
    this.close.emit();
  }

  private toInstantString(value: string): string {
    return new Date(value).toISOString();
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

  private mapUndefinedToNull<T>(value: T | undefined | null): T | null {
    return value == undefined ? null : value;
  }
}


