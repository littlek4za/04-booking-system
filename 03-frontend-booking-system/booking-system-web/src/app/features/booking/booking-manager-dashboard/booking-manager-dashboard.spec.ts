import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingManagerDashboard } from './booking-manager-dashboard';

describe('BookingManagerDashboard', () => {
  let component: BookingManagerDashboard;
  let fixture: ComponentFixture<BookingManagerDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingManagerDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookingManagerDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
