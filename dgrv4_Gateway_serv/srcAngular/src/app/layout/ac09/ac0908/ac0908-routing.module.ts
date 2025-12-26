import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0908Component } from './ac0908.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
    path: '', component: Ac0908Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0908RoutingModule { }
