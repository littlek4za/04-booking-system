import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingConfirmationWizard } from './booking-confirmation-wizard';

describe('BookingConfirmationWizard', () => {
  let component: BookingConfirmationWizard;
  let fixture: ComponentFixture<BookingConfirmationWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingConfirmationWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookingConfirmationWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
