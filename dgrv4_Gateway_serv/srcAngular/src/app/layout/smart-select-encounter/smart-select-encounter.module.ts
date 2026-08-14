import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';

import { SmartSelectEncounterRoutingModule } from './smart-select-encounter-routing.module';
import { SmartSelectEncounterComponent } from './smart-select-encounter.component';
import { SmartStatusPanelComponent } from '../smart-shared/smart-status-panel/smart-status-panel.component';

@NgModule({
  declarations: [SmartSelectEncounterComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    TranslateModule,
    SmartStatusPanelComponent,
    SmartSelectEncounterRoutingModule,
  ]
})
export class SmartSelectEncounterModule { }
