import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateEventWizard } from './update-event-wizard';

describe('UpdateEventWizard', () => {
  let component: UpdateEventWizard;
  let fixture: ComponentFixture<UpdateEventWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateEventWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateEventWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
