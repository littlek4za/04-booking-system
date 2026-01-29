import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FullCalendarView } from './full-calendar-view';

describe('FullCalendarView', () => {
  let component: FullCalendarView;
  let fixture: ComponentFixture<FullCalendarView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FullCalendarView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FullCalendarView);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
