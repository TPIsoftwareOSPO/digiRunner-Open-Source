
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0003Component } from './ai0003.component';

const routes: Routes = [
  {
    path: '', component: Ai0003Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ai0003RoutingModule {}
