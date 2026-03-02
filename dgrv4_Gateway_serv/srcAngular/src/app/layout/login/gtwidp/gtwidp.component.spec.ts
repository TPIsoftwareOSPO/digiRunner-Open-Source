import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GtwidpComponent } from './gtwidp.component';

describe('GtwidpComponent', () => {
  let component: GtwidpComponent;
  let fixture: ComponentFixture<GtwidpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GtwidpComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GtwidpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
