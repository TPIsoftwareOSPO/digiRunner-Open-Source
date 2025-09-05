import { PrimengModule } from '../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0003RoutingModule } from './ai0003-routing.module';
import { Ai0003Component } from './ai0003.component';

@NgModule({
  imports: [
    CommonModule,
    Ai0003RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ai0003Component],
  providers: [TokenExpiredGuard],
})
export class Ai0003Module {}
