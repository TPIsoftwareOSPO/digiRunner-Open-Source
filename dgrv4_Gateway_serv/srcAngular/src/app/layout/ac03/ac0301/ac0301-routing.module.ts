import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0301Component } from './ac0301.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0301Component ,canActivate :[TokenExpiredGuard]  
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0301RoutingModule { }
