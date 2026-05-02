import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendeeBookingListViewComponent } from './attendee-booking-list-view-component';

describe('AttendeeBookingListViewComponent', () => {
  let component: AttendeeBookingListViewComponent;
  let fixture: ComponentFixture<AttendeeBookingListViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttendeeBookingListViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendeeBookingListViewComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
