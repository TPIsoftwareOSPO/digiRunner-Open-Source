import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdpssoComponent } from './idpsso.component';

describe('IdpssoComponent', () => {
  let component: IdpssoComponent;
  let fixture: ComponentFixture<IdpssoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IdpssoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IdpssoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
