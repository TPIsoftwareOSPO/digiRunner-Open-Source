import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WebsocketProxyComponent } from './websocket-proxy.component';

describe('WebsocketProxyComponent', () => {
  let component: WebsocketProxyComponent;
  let fixture: ComponentFixture<WebsocketProxyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WebsocketProxyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WebsocketProxyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
