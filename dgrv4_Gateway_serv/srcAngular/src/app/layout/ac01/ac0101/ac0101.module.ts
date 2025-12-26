import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0101RoutingModule } from './ac0101-routing.module';
import { Ac0101Component } from './ac0101.component';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0101RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0101Component],
  providers:[TokenExpiredGuard]
})
export class Ac0101Module { }
