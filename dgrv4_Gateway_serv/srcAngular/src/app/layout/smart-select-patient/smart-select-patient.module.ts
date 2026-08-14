import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';

import { SmartSelectPatientRoutingModule } from './smart-select-patient-routing.module';
import { SmartSelectPatientComponent } from './smart-select-patient.component';
import { SmartStatusPanelComponent } from '../smart-shared/smart-status-panel/smart-status-panel.component';

@NgModule({
  declarations: [SmartSelectPatientComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    TranslateModule,
    SmartStatusPanelComponent,
    SmartSelectPatientRoutingModule,
  ]
})
export class SmartSelectPatientModule { }
