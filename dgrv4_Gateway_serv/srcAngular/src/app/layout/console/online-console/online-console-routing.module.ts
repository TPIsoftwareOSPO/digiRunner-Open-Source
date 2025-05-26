import { OnlineConsoleComponent } from './online-console.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [
  {
    path: '', component: OnlineConsoleComponent, canActivate: [TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OnlineConsoleRoutingModule { }
