import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HostnameConfigFormComponent } from './hostname-config-form.component';

describe('HostnameConfigFormComponent', () => {
  let component: HostnameConfigFormComponent;
  let fixture: ComponentFixture<HostnameConfigFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HostnameConfigFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HostnameConfigFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
