import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SourceIpFormComponent } from './source-ip-form.component';

describe('SourceIpFormComponent', () => {
  let component: SourceIpFormComponent;
  let fixture: ComponentFixture<SourceIpFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SourceIpFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SourceIpFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
