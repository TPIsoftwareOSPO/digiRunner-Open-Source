import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0905Component } from './ac0905.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0905Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0905RoutingModule { }
