import { Lb0011RoutingModule } from './lb0011-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { Lb0011Component } from './lb0011.component';
import { KeyValueFormComponent } from './key-value-form/key-value-form.component';
import { KeyValueFieldComponent } from './key-value-field/key-value-field.component';
import { MappingUrlFormComponent } from './mapping-url-form/mapping-url-form.component';
import { MappingUrlFieldComponent } from './mapping-url-field/mapping-url-field.component';

@NgModule({
  imports: [
    CommonModule,
    Lb0011RoutingModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [
    Lb0011Component,
    KeyValueFormComponent,
    KeyValueFieldComponent,
    MappingUrlFormComponent,
    MappingUrlFieldComponent
  ],
  providers: [TokenExpiredGuard],
})
export class Lb0011Module {}
