import { Ac0006Component } from './ac0006.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
const routes: Routes = [
  {
      path: '', component: Ac0006Component  ,canActivate :[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0006RoutingModule { }
