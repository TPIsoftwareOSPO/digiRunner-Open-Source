import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0202Component } from './ac0202.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0202Component  ,canActivate:[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0202RoutingModule { }
