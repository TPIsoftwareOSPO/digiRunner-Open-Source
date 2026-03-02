import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0316Component } from './ac0316.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0316Component ,canActivate :[TokenExpiredGuard]  
  },
  {
    path: ':apiKey/:moduleName/:apiSrc', component: Ac0316Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers:[TokenExpiredGuard]
})
export class Ac0316RoutingModule { }
