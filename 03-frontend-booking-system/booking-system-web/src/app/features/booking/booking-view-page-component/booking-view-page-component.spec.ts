import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingViewPageComponent } from './booking-view-page-component';

describe('BookingViewPageComponent', () => {
  let component: BookingViewPageComponent;
  let fixture: ComponentFixture<BookingViewPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingViewPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookingViewPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
