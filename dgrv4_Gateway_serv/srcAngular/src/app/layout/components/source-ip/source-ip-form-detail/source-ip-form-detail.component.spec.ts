import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SourceIpFormDetailComponent } from './source-ip-form-detail.component';

describe('SourceIpFormDetailComponent', () => {
  let component: SourceIpFormDetailComponent;
  let fixture: ComponentFixture<SourceIpFormDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SourceIpFormDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SourceIpFormDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
