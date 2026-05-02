import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendeeBookingDetailComponent } from './attendee-booking-detail-component';

describe('AttendeeBookingDetailComponent', () => {
  let component: AttendeeBookingDetailComponent;
  let fixture: ComponentFixture<AttendeeBookingDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttendeeBookingDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendeeBookingDetailComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
