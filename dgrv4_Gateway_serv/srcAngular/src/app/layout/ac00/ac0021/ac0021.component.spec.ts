import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0021Component } from './ac0021.component';

describe('Ac0021Component', () => {
  let component: Ac0021Component;
  let fixture: ComponentFixture<Ac0021Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0021Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0021Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
