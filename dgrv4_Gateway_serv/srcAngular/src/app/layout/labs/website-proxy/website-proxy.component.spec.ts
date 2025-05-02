import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WebsiteProxyComponent } from './website-proxy.component';

describe('WebsiteProxyComponent', () => {
  let component: WebsiteProxyComponent;
  let fixture: ComponentFixture<WebsiteProxyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WebsiteProxyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WebsiteProxyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
