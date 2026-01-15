import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SlotDashboardComponent } from './slot-dashboard-component';

describe('SlotDashboardComponent', () => {
  let component: SlotDashboardComponent;
  let fixture: ComponentFixture<SlotDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SlotDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SlotDashboardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
