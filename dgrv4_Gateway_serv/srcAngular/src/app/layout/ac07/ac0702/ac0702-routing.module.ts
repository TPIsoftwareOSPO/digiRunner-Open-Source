import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0702Component } from 'src/app/layout/ac07/ac0702/ac0702.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0702Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0702RoutingModule { }
