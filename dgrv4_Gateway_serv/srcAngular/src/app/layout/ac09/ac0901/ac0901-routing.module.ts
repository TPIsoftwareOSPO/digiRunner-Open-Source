import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0901Component } from './ac0901.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0901Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0901RoutingModule { }
