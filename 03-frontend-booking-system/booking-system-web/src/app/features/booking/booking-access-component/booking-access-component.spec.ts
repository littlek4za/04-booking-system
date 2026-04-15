import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingAccessComponent } from './booking-access-component';

describe('BookingAccessComponent', () => {
  let component: BookingAccessComponent;
  let fixture: ComponentFixture<BookingAccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingAccessComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookingAccessComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
