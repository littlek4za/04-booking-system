import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvitationEditWizard } from './invitation-edit-wizard';

describe('InvitationEditWizard', () => {
  let component: InvitationEditWizard;
  let fixture: ComponentFixture<InvitationEditWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitationEditWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvitationEditWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
