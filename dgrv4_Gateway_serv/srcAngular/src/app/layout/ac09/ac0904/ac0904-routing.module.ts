import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0904Component } from './ac0904.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0904Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0904RoutingModule { }
