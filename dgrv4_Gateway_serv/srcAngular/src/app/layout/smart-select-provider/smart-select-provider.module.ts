import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';

import { SmartSelectProviderRoutingModule } from './smart-select-provider-routing.module';
import { SmartSelectProviderComponent } from './smart-select-provider.component';
import { SmartStatusPanelComponent } from '../smart-shared/smart-status-panel/smart-status-panel.component';

@NgModule({
  declarations: [SmartSelectProviderComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    TranslateModule,
    SmartStatusPanelComponent,
    SmartSelectProviderRoutingModule,
  ]
})
export class SmartSelectProviderModule { }
