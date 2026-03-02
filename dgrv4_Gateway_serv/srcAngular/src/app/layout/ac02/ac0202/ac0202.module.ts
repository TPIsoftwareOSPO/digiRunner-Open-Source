import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0202RoutingModule } from './ac0202-routing.module';
import { Ac0202Component } from './ac0202.component';

import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0202RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0202Component],
  providers:[TokenExpiredGuard]
})
export class Ac0202Module {
  submitForm(){}
}
