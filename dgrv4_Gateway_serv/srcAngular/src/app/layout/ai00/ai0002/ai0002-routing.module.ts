
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0002Component } from './ai0002.component';

const routes: Routes = [
  {
    path: '', component: Ai0002Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ai0002RoutingModule {}
