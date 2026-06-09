import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Lb0016Component } from './lb0016.component';
import { EhrSimComponent } from './ehr-sim/ehr-sim.component';
import { SampleAppComponent } from './sample-app/sample-app.component';
import { SampleAppCallbackComponent } from './sample-app/sample-app-callback.component';

const routes: Routes = [
  { path: '', component: Lb0016Component, canActivate: [TokenExpiredGuard] },
  { path: 'ehr', component: EhrSimComponent, canActivate: [TokenExpiredGuard] },
  { path: 'sample', component: SampleAppComponent },
  { path: 'sample/callback', component: SampleAppCallbackComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Lb0016RoutingModule { }
