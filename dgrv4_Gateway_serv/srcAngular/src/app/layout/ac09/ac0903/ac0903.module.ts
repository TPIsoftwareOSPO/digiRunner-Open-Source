import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0903RoutingModule } from './ac0903-routing.module';
import { Ac0903Component } from './ac0903.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0903RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0903Component],
  providers:[TokenExpiredGuard]
})
export class Ac0903Module { }
