import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0019Component } from './ac0019.component';

describe('Ac0019Component', () => {
  let component: Ac0019Component;
  let fixture: ComponentFixture<Ac0019Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0019Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0019Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
