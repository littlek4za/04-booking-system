import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingConfirmationDashboard } from './booking-confirmation-dashboard';

describe('BookingConfirmationDashboard', () => {
  let component: BookingConfirmationDashboard;
  let fixture: ComponentFixture<BookingConfirmationDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingConfirmationDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookingConfirmationDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
