import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SrcUrlInputDetailComponent } from './src-url-input-detail.component';

describe('SrcUrlInputDetailComponent', () => {
  let component: SrcUrlInputDetailComponent;
  let fixture: ComponentFixture<SrcUrlInputDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SrcUrlInputDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SrcUrlInputDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
