import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddSlotWizard } from './add-slot-wizard';

describe('AddSlotWizard', () => {
  let component: AddSlotWizard;
  let fixture: ComponentFixture<AddSlotWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddSlotWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddSlotWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
