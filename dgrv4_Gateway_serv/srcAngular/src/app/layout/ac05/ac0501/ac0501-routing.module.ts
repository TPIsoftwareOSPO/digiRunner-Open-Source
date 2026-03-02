import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0501Component } from './ac0501.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0501Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0501RoutingModule { }
