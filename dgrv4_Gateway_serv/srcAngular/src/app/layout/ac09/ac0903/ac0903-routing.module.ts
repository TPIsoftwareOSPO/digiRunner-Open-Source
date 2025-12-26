import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0903Component } from './ac0903.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0903Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0903RoutingModule { }
