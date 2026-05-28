import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendeeBookingSuccessDetailWizard } from './attendee-booking-success-detail-wizard';

describe('AttendeeBookingSuccessDetailWizard', () => {
  let component: AttendeeBookingSuccessDetailWizard;
  let fixture: ComponentFixture<AttendeeBookingSuccessDetailWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttendeeBookingSuccessDetailWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendeeBookingSuccessDetailWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
