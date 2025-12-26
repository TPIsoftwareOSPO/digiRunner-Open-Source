import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldsFormComponent } from './fields-form.component';

describe('FieldsFormComponent', () => {
  let component: FieldsFormComponent;
  let fixture: ComponentFixture<FieldsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldsFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FieldsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
