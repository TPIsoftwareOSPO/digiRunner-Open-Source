import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0510Component } from './ac0510.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0510Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0510RoutingModule { }
