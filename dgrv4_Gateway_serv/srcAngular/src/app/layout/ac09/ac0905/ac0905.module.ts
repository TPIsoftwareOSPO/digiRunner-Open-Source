import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0905RoutingModule } from './ac0905-routing.module';
import { Ac0905Component } from './ac0905.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0905RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0905Component],
  providers:[TokenExpiredGuard]
})
export class Ac0905Module { }
