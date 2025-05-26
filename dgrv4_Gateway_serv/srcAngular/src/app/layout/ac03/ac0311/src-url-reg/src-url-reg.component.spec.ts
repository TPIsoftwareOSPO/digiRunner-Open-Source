import { SrcUrlRegComponent } from './src-url-reg.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';



describe('SrcUrlTestComponent', () => {
  let component: SrcUrlRegComponent;
  let fixture: ComponentFixture<SrcUrlRegComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SrcUrlRegComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SrcUrlRegComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
