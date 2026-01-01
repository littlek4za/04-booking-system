import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvitationManagerComponent } from './invitation-manager-component';

describe('InvitationManagerComponent', () => {
  let component: InvitationManagerComponent;
  let fixture: ComponentFixture<InvitationManagerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitationManagerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvitationManagerComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
