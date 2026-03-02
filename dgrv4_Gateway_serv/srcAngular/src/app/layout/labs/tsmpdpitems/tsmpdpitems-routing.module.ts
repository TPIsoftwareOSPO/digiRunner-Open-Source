import { TsmpdpitemsComponent } from './tsmpdpitems.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';



const routes: Routes = [
  {
    path: '', component: TsmpdpitemsComponent, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TsmpdpitemsRoutingModule { }
