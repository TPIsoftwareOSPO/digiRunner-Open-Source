import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { WebsocketProxyComponent } from './websocket-proxy.component';

const routes: Routes = [
  {
    path: '', component: WebsocketProxyComponent, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WebsocketProxyRoutingModule { }
