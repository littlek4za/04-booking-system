import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeafletMapSelection } from './leaflet-map-selection';

describe('LeafletMapSelection', () => {
  let component: LeafletMapSelection;
  let fixture: ComponentFixture<LeafletMapSelection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LeafletMapSelection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeafletMapSelection);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
