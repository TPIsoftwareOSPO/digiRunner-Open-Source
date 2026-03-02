import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0907RoutingModule } from './ac0907-routing.module';
import { Ac0907Component } from './ac0907.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0907RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0907Component],
  providers:[TokenExpiredGuard]
})
export class Ac0907Module { }
