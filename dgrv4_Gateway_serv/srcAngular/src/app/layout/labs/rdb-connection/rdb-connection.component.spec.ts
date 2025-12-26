import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RdbConnectionComponent } from './rdb-connection.component';

describe('RdbConnectionComponent', () => {
  let component: RdbConnectionComponent;
  let fixture: ComponentFixture<RdbConnectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RdbConnectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RdbConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
