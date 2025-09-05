import { PrimengModule } from '../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0005Component } from './ai0005.component';
import { Ai0005RoutingModule } from './ai0005-routing.module';

@NgModule({
  imports: [
    CommonModule,
    Ai0005RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ai0005Component],
  providers: [TokenExpiredGuard],
})
export class Ai0005Module {}
