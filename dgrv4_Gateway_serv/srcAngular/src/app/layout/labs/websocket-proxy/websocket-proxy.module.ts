import { TokenExpiredGuard } from 'src/app/shared';

import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { WebsocketProxyComponent } from './websocket-proxy.component';
import { WebsocketProxyRoutingModule } from './websocket-proxy-routing.module';

@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    WebsocketProxyRoutingModule
  ],
  declarations: [WebsocketProxyComponent],
  providers:[TokenExpiredGuard]
})
export class WebsocketProxyModule { }
