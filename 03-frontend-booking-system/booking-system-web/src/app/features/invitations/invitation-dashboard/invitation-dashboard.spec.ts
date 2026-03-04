import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvitationDashboard } from './invitation-dashboard';

describe('InvitationDashboard', () => {
  let component: InvitationDashboard;
  let fixture: ComponentFixture<InvitationDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitationDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvitationDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
