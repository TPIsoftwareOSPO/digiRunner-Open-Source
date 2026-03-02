import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0507Component } from './ac0507.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0507Component  ,canActivate:[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0507RoutingModule { }
