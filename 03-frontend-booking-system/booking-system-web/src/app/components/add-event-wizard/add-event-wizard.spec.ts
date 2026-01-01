import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddEventWizard } from './add-event-wizard';

describe('AddEventWizard', () => {
  let component: AddEventWizard;
  let fixture: ComponentFixture<AddEventWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEventWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddEventWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
