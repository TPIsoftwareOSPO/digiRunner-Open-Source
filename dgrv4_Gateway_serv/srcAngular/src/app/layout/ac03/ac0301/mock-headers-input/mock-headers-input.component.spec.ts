import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MockHeadersInputComponent } from './mock-headers-input.component';

describe('MockHeadersInputComponent', () => {
  let component: MockHeadersInputComponent;
  let fixture: ComponentFixture<MockHeadersInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MockHeadersInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MockHeadersInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
