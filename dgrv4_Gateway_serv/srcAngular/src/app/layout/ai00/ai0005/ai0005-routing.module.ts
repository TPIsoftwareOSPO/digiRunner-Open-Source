
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0005Component } from './ai0005.component';

const routes: Routes = [
  {
    path: '', component: Ai0005Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ai0005RoutingModule {}
