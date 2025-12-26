import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RdbConnectionComponent } from './rdb-connection.component';

const routes: Routes = [
  {
    path: '', component: RdbConnectionComponent, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RdbConnectionRoutingModule { }
