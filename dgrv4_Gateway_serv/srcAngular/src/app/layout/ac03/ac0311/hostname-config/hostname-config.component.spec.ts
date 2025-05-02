import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HostnameConfigComponent } from './hostname-config.component';

describe('HostnameConfigComponent', () => {
  let component: HostnameConfigComponent;
  let fixture: ComponentFixture<HostnameConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HostnameConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HostnameConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
