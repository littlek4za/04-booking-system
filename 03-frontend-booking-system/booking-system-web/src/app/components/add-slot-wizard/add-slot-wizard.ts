import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-add-slot-wizard',
  standalone:true,
  imports: [],
  templateUrl: './add-slot-wizard.html',
  styleUrl: './add-slot-wizard.css',
})
export class AddSlotWizard {

  @Output()
  close = new EventEmitter<void>();

  closeWizard(){
    this.close.emit();
  }

}
