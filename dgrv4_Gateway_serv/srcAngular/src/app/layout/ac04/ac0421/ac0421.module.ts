import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0421RoutingModule } from './ac0421-routing.module';
import { Ac0421Component } from './ac0421.component';
import { PrimengModule } from 'src/app/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0421RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0421Component],
  providers:[TokenExpiredGuard]
})
export class Ac0421Module { }
