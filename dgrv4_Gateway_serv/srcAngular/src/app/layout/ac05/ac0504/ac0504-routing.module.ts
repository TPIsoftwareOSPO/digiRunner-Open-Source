import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0504Component } from './ac0504.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0504Component  ,canActivate:[TokenExpiredGuard] 
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0504RoutingModule { }
