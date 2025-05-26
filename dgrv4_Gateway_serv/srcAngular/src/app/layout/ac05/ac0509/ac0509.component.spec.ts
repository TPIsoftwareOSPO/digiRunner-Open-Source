import { Ac0509Component } from './ac0509.component';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

describe('Sc0003Component', () => {
  let component: Ac0509Component;
  let fixture: ComponentFixture<Ac0509Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Ac0509Component ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Ac0509Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
