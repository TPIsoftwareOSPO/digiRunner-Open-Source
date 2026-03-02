import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0421Component } from './ac0421.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0421Component   ,canActivate :[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0421RoutingModule { }
