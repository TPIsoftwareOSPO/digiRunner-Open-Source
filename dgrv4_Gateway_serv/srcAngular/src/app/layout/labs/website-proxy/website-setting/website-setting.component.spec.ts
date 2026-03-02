import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WebsiteSettingComponent } from './website-setting.component';

describe('WebsiteSettingComponent', () => {
  let component: WebsiteSettingComponent;
  let fixture: ComponentFixture<WebsiteSettingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WebsiteSettingComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WebsiteSettingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
