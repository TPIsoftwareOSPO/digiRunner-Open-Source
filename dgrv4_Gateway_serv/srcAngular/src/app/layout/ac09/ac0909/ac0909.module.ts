import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0909RoutingModule } from './ac0909-routing.module';
import { Ac0909Component } from './ac0909.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0909RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0909Component],
  providers:[TokenExpiredGuard]
})
export class Ac0909Module { }
