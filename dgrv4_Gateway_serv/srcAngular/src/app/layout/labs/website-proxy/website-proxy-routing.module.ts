import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { WebsiteProxyComponent } from './website-proxy.component';


const routes: Routes = [
  {
    path: '', component: WebsiteProxyComponent, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WebsiteProxyRoutingModule { }
