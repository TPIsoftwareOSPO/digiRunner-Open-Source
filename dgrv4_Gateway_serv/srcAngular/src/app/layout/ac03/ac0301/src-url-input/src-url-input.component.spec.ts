import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SrcUrlInputComponent } from './src-url-input.component';

describe('SrcUrlInputComponent', () => {
  let component: SrcUrlInputComponent;
  let fixture: ComponentFixture<SrcUrlInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SrcUrlInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SrcUrlInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
