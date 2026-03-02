import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0902Component } from './ac0902.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0902Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0902RoutingModule { }
