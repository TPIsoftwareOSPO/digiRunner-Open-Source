import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0105Component } from './ac0105.component';

const routes: Routes = [
  {
      path: '', component: Ac0105Component  ,canActivate :[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0105RoutingModule { }
