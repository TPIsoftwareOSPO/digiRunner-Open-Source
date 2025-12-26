import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MockHeadersInputDetailComponent } from './mock-headers-input-detail.component';

describe('MockHeadersInputDetailComponent', () => {
  let component: MockHeadersInputDetailComponent;
  let fixture: ComponentFixture<MockHeadersInputDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MockHeadersInputDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MockHeadersInputDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
