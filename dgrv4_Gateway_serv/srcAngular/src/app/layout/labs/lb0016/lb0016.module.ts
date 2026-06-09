import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Textarea } from 'primeng/textarea';
import { Lb0016Component } from './lb0016.component';
import { EhrSimComponent } from './ehr-sim/ehr-sim.component';
import { SampleAppComponent } from './sample-app/sample-app.component';
import { SampleAppCallbackComponent } from './sample-app/sample-app-callback.component';
import { Lb0016RoutingModule } from './lb0016-routing.module';
import { SmartFhirService } from './smart-fhir.service';

@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    Textarea,
    Lb0016RoutingModule
  ],
  declarations: [Lb0016Component, EhrSimComponent, SampleAppComponent, SampleAppCallbackComponent],
  providers: [TokenExpiredGuard, SmartFhirService]
})
export class Lb0016Module { }
