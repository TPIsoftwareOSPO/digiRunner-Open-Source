import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { SmartSelectProviderRoutingModule } from './smart-select-provider-routing.module';
import { SmartSelectProviderComponent } from './smart-select-provider.component';

@NgModule({
  declarations: [SmartSelectProviderComponent],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    SmartSelectProviderRoutingModule,
  ]
})
export class SmartSelectProviderModule { }
