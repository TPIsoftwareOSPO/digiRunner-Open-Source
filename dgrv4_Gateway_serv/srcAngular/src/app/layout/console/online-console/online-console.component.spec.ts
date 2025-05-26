import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OnlineConsoleComponent } from './online-console.component';

describe('OnlineConsoleComponent', () => {
  let component: OnlineConsoleComponent;
  let fixture: ComponentFixture<OnlineConsoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OnlineConsoleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OnlineConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
