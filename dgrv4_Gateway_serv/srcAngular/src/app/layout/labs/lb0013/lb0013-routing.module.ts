import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Lb0013Component } from './lb0013.component';

const routes: Routes = [
  {
    path: '', component: Lb0013Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Lb0013RoutingModule { }
