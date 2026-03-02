import { PrimengModule } from './../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1304RoutingModule } from './ac1304-routing.module';
import { Ac1304Component } from './ac1304.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';


@NgModule({
  imports: [
    CommonModule,
    Ac1304RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac1304Component],
  providers: [TokenExpiredGuard]
})
export class Ac1304Module { }
