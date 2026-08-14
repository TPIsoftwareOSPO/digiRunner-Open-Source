import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';

import { SmartConsentRoutingModule } from './smart-consent-routing.module';
import { SmartConsentComponent } from './smart-consent.component';
import { SmartStatusPanelComponent } from '../smart-shared/smart-status-panel/smart-status-panel.component';

@NgModule({
  declarations: [SmartConsentComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    TranslateModule,
    SmartStatusPanelComponent,
    SmartConsentRoutingModule,
  ]
})
export class SmartConsentModule { }
