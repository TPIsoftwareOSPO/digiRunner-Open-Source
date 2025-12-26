import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0212RoutingModule } from './ac0212-routing.module';
import { Ac0212Component } from './ac0212.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0212RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0212Component],
  providers:[TokenExpiredGuard]
})
export class Ac0212Module { }
