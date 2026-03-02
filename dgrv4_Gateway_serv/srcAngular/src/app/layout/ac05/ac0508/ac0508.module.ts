import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0508RoutingModule } from './ac0508-routing.module';
import { Ac0508Component } from './ac0508.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0508RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0508Component],
  providers:[TokenExpiredGuard]
})
export class Ac0508Module { }
