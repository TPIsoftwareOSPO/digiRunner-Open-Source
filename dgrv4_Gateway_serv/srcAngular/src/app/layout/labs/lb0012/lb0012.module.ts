import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { Lb0012Component } from './lb0012.component';
import { Lb0012RoutingModule } from './lb0012-routing.module';

@NgModule({
  imports: [
    CommonModule,
    Lb0012RoutingModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Lb0012Component],
  providers: [TokenExpiredGuard],
})
export class Lb0012Module {}
