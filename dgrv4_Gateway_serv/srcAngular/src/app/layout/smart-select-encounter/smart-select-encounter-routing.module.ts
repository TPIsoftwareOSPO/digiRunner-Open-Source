import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SmartSelectEncounterComponent } from './smart-select-encounter.component';

const routes: Routes = [
  { path: '', component: SmartSelectEncounterComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SmartSelectEncounterRoutingModule {}
