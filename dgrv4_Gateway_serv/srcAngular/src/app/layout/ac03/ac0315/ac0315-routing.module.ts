import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0315Component } from './ac0315.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0315Component ,canActivate :[TokenExpiredGuard]  
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0315RoutingModule { }
