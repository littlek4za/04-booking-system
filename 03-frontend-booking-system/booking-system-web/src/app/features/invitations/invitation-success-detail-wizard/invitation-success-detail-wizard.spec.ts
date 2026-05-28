import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvitationSuccessDetailWizard } from './invitation-success-detail-wizard';

describe('InvitationSuccessDetailWizard', () => {
  let component: InvitationSuccessDetailWizard;
  let fixture: ComponentFixture<InvitationSuccessDetailWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitationSuccessDetailWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvitationSuccessDetailWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
