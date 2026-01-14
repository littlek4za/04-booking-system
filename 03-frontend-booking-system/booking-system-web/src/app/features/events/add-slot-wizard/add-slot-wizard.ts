import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-add-slot-wizard',
  standalone:true,
  imports: [],
  templateUrl: './add-slot-wizard.html',
  styleUrl: './add-slot-wizard.css',
})
export class AddSlotWizard implements OnInit {

  @Output() close = new EventEmitter<void>();
  @Input() eventId!: String;
  addSlotForm!: FormGroup; 

  constructor(){}

  ngOnInit(): void {
    this.initAddSlotForm();
  }


  initAddSlotForm() {
    throw new Error('Method not implemented.');
  }

  closeWizard(){
    this.close.emit();
  }

}
