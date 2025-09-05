
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0001Component } from './ai0001.component';
const routes: Routes = [
  {
    path: '', component: Ai0001Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ai0001RoutingModule {}
