import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyValueFieldComponent } from './key-value-field.component';

describe('KeyValueFieldComponent', () => {
  let component: KeyValueFieldComponent;
  let fixture: ComponentFixture<KeyValueFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KeyValueFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyValueFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
