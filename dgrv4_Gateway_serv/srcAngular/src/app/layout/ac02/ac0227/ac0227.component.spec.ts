import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0227Component } from './ac0227.component';

describe('Ac0227Component', () => {
  let component: Ac0227Component;
  let fixture: ComponentFixture<Ac0227Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0227Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0227Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
