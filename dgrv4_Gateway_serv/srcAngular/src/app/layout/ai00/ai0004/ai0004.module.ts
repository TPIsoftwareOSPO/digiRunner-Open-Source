import { PrimengModule } from '../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0004Component } from './ai0004.component';
import { Ai0004RoutingModule } from './ai0004-routing.module';

@NgModule({
  imports: [
    CommonModule,
    Ai0004RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ai0004Component],
  providers: [TokenExpiredGuard],
})
export class Ai0004Module {}
