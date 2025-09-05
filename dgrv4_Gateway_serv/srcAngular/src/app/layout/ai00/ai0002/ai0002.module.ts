
import { PrimengModule } from '../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ai0002RoutingModule } from './ai0002-routing.module';
import { Ai0002Component } from './ai0002.component';


@NgModule({
  imports: [
    CommonModule,
    Ai0002RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ai0002Component],
  providers:[TokenExpiredGuard]
})
export class Ai0002Module { }
