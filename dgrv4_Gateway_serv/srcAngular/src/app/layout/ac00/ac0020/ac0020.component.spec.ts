import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0020Component } from './ac0020.component';

describe('Ac0020Component', () => {
  let component: Ac0020Component;
  let fixture: ComponentFixture<Ac0020Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0020Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0020Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
