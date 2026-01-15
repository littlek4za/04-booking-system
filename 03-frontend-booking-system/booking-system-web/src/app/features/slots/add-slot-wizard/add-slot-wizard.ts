import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-add-slot-wizard',
  standalone: true,
  imports: [],
  templateUrl: './add-slot-wizard.html',
  styleUrl: './add-slot-wizard.css',
})
export class AddSlotWizard implements OnInit {

  @Output() close = new EventEmitter<void>();
  @Input() eventId!: number;
  @Input() eventType!: string;
  addSlotForm!: FormGroup;

  constructor() { }

  ngOnInit(): void {
    this.initAddSlotForm();
    this.applyEventTypeRules();
  }

  initAddSlotForm() {
    this.addSlotForm = new FormGroup({
      slotName: new FormControl<string>(""),
      slotDescription: new FormControl<string>(""),
      slotStartTime: new FormControl<string>(""),
      slotEndTime: new FormControl<string>(""),
      maxBook: new FormControl<string>(""),
      slotIntervalMinutes: new FormControl<string>(""),
      workingDaysHours: new FormControl<string>(""),
    });
  }

  applyEventTypeDefaultValues() {
    switch (this.eventType) {
      case 'FLEXIBLE':
        this.addSlotForm.patchValue({
          maxBook: 0,
          workingDaysHours: null,
        });
        break;
      case 'FIXED':
        this.addSlotForm.patchValue({
          slotIntervalMinutes: 0,
          workingDaysHours: null,
        });
        break;
      case 'BUSINESS':
        this.addSlotForm.patchValue({
          maxBoook:0,
        });
        break;
    }
  }

  applyEventTypeRules() {
    switch (this.eventType) {
      case 'FLEXIBLE':
        this.enable(['slotIntervalMinutes']);
        this.disable(['maxBook', 'workingDaysHours']);
        break;
      case 'FIXED':
        this.enable(['maxBook']);
        this.disable(['slotIntervalMinutes', 'workingDaysHours']);
        break;
      case 'BUSINESS':
        this.enable(['slotIntervalMinutes', 'workingDaysHours']);
        this.disable(['maxBook']);
        break;
    }
  }
  enable(fields: string[]) {
    fields.forEach(f => this.addSlotForm.get(f)?.enable());
  }

  disable(fields: string[]) {
    fields.forEach(f => this.addSlotForm.get(f)?.disable());
  }


  closeWizard() {
    this.close.emit();
  }

}
