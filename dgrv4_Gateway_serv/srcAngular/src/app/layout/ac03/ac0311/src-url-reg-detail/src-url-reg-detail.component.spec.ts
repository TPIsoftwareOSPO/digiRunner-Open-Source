import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SrcUrlRegDetailComponent } from './src-url-reg-detail.component';

describe('SrcUrlRegDetailComponent', () => {
  let component: SrcUrlRegDetailComponent;
  let fixture: ComponentFixture<SrcUrlRegDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SrcUrlRegDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SrcUrlRegDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
