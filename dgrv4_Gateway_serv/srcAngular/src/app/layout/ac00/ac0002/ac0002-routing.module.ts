import { Ac0002Component } from './ac0002.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SidebarService } from '../../components/sidebar/sidebar.service';
import { TokenExpiredGuard } from 'src/app/shared';
const routes: Routes = [
  {
    path: '', component: Ac0002Component, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0002RoutingModule {
  constructor(private sidebarService: SidebarService) {
  }
}
