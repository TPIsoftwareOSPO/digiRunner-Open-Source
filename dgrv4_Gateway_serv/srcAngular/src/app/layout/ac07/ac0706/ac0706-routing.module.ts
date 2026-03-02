import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0706Component } from 'src/app/layout/ac07/ac0706/ac0706.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0706Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0706RoutingModule { }
