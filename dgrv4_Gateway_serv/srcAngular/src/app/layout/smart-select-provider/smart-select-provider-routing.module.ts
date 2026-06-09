import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SmartSelectProviderComponent } from './smart-select-provider.component';

const routes: Routes = [
  { path: '', component: SmartSelectProviderComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SmartSelectProviderRoutingModule {}
