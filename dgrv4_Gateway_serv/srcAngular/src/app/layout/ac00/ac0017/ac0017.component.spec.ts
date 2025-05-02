import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0017Component } from './ac0017.component';

describe('Ac0017Component', () => {
  let component: Ac0017Component;
  let fixture: ComponentFixture<Ac0017Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0017Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0017Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
