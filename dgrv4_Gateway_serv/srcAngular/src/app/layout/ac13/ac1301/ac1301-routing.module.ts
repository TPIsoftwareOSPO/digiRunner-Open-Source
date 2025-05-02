import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1301Component } from './ac1301.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac1301Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac1301RoutingModule { }
