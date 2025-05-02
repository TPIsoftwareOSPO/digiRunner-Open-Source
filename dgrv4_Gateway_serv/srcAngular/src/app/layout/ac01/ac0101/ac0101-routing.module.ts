import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0101Component } from './ac0101.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0101Component  ,canActivate :[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0101RoutingModule { }
