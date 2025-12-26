import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0502Component } from './ac0502.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0502Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0502RoutingModule { }
