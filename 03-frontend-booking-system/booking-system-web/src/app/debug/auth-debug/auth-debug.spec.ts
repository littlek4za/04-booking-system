import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthDebug } from './auth-debug';

describe('AuthDebug', () => {
  let component: AuthDebug;
  let fixture: ComponentFixture<AuthDebug>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthDebug]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthDebug);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
