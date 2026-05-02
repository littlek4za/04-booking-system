import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendeeAccessComponent } from './attendee-access-component';

describe('AttendeeAccessComponent', () => {
  let component: AttendeeAccessComponent;
  let fixture: ComponentFixture<AttendeeAccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttendeeAccessComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendeeAccessComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
