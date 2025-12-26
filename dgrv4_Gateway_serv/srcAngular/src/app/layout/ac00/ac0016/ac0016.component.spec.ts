import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ac0016Component } from './ac0016.component';

describe('Ac0016Component', () => {
  let component: Ac0016Component;
  let fixture: ComponentFixture<Ac0016Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Ac0016Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0016Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
