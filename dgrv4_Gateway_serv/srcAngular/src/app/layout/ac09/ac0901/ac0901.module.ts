import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0901RoutingModule } from './ac0901-routing.module';
import { Ac0901Component } from './ac0901.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0901RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0901Component],
  providers:[TokenExpiredGuard]
})
export class Ac0901Module { }
