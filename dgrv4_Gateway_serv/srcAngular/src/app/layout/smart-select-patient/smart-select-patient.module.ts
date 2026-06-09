import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { SmartSelectPatientRoutingModule } from './smart-select-patient-routing.module';
import { SmartSelectPatientComponent } from './smart-select-patient.component';

@NgModule({
  declarations: [SmartSelectPatientComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    SmartSelectPatientRoutingModule,
  ]
})
export class SmartSelectPatientModule { }
