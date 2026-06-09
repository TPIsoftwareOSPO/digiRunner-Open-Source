import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SmartSelectPatientComponent } from './smart-select-patient.component';

const routes: Routes = [
  { path: '', component: SmartSelectPatientComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SmartSelectPatientRoutingModule {}
