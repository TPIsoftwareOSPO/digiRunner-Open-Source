import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { SmartConsentRoutingModule } from './smart-consent-routing.module';
import { SmartConsentComponent } from './smart-consent.component';

@NgModule({
  declarations: [SmartConsentComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    SmartConsentRoutingModule,
  ]
})
export class SmartConsentModule { }
