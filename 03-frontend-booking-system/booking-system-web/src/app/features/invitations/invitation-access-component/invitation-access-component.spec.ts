import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvitationAccessComponent } from './invitation-access-component';

describe('InvitationAccessComponent', () => {
  let component: InvitationAccessComponent;
  let fixture: ComponentFixture<InvitationAccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitationAccessComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvitationAccessComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
