import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SlotEditWizard } from './slot-edit-wizard';

describe('AddSlotWizard', () => {
  let component: SlotEditWizard;
  let fixture: ComponentFixture<SlotEditWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SlotEditWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SlotEditWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
