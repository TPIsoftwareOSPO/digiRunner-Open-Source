
import { PrimengModule } from '../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ai0001RoutingModule } from './ai0001-routing.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0001Component } from './ai0001.component';

@NgModule({
  imports: [
    CommonModule,
    Ai0001RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ai0001Component],
  providers:[TokenExpiredGuard]
})
export class Ai0001Module { }
