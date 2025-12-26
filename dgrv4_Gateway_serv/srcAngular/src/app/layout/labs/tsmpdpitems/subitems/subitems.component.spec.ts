import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubitemsComponent } from './subitems.component';

describe('SubitemsComponent', () => {
  let component: SubitemsComponent;
  let fixture: ComponentFixture<SubitemsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SubitemsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SubitemsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
