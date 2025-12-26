import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WebsiteSettingRowComponent } from './website-setting-row.component';

describe('WebsiteSettingRowComponent', () => {
  let component: WebsiteSettingRowComponent;
  let fixture: ComponentFixture<WebsiteSettingRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WebsiteSettingRowComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WebsiteSettingRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
