import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0226Component } from './ac0226.component';

describe('Ac0226Component', () => {
  let component: Ac0226Component;
  let fixture: ComponentFixture<Ac0226Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0226Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0226Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
