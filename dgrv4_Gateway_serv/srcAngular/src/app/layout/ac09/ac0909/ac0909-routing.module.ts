import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0909Component } from './ac0909.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0909Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0909RoutingModule { }
