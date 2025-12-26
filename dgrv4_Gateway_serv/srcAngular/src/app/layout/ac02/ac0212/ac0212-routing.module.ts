import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0212Component } from './ac0212.component';

const routes: Routes = [
  {
      path: '', component: Ac0212Component  
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0212RoutingModule { }
