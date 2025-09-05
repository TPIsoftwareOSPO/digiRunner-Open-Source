
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0004Component } from './ai0004.component';

const routes: Routes = [
  {
    path: '', component: Ai0004Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ai0004RoutingModule {}
