import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldsFormDetailComponent } from './fields-form-detail.component';

describe('FieldsFormDetailComponent', () => {
  let component: FieldsFormDetailComponent;
  let fixture: ComponentFixture<FieldsFormDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldsFormDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FieldsFormDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
