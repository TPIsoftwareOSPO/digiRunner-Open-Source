import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0910RoutingModule } from './ac0910-routing.module';
import { Ac0910Component } from './ac0910.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0910RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0910Component],
  providers:[TokenExpiredGuard]
})
export class Ac0910Module { }
