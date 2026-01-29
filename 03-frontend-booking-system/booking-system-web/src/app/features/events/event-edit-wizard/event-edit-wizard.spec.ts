import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventEditWizard } from './event-edit-wizard';

describe('EventEditWizard', () => {
  let component: EventEditWizard;
  let fixture: ComponentFixture<EventEditWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventEditWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventEditWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
