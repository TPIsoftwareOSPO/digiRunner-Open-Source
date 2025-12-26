import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0907Component } from './ac0907.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0907Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0907RoutingModule { }
