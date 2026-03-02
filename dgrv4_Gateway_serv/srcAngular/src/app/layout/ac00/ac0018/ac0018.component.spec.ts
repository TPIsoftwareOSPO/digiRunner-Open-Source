import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0018Component } from './ac0018.component';

describe('Ac0018Component', () => {
  let component: Ac0018Component;
  let fixture: ComponentFixture<Ac0018Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0018Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0018Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
