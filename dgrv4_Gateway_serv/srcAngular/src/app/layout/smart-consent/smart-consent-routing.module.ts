import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SmartConsentComponent } from './smart-consent.component';

const routes: Routes = [
  { path: '', component: SmartConsentComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SmartConsentRoutingModule {}
