import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { EventTypeModel } from '@features/events/dtos/event-type-model';
import { SlotIncludeMode } from '../dtos/slot-include-mode';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-invitation-edit-wizard',
  imports: [],
  templateUrl: './invitation-edit-wizard.html',
  styleUrl: './invitation-edit-wizard.css',
})
export class InvitationEditWizard implements OnInit, OnDestroy {


  @Input() mode!: 'CREATE' | 'VIEW';
  @Input() eventId!: number;
  @Input() eventType!: EventTypeModel;
  @Input() slotId: number | null = null;

  invitationForm!: FormGroup;

  private destroy$ = new Subject<void>();
  
  ngOnInit(): void {
    this.initInvitationForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initInvitationForm() {
    this.invitationForm = new FormGroup({
      noExpiry: new FormControl<boolean>(true),
      expiresAt: new FormControl<Date | null>(null),
      noMaxUsage: new FormControl<boolean>(true),
      maxUsage: new FormControl<number | null>(null),
      includeAllSlot: new FormControl<SlotIncludeMode> (SlotIncludeMode.ALL_AND_FUTURE),
      selectedSlotIds: new FormControl<number[]|null>(null),
    });
  }

  onSubmit() {
    this.invitationForm.markAllAsTouched();
    if(this.invitationForm.invalid) {
      console.warn('Form Submission Field Invalid');
      return;
    }
  }
}
