import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0906Component } from './ac0906.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0906Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0906RoutingModule { }
