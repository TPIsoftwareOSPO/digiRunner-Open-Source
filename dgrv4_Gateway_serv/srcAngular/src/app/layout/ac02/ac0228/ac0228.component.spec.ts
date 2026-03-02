import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0228Component } from './ac0228.component';

describe('Ac0228Component', () => {
  let component: Ac0228Component;
  let fixture: ComponentFixture<Ac0228Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0228Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0228Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
