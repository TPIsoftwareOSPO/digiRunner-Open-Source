import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0902RoutingModule } from './ac0902-routing.module';
import { Ac0902Component } from './ac0902.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0902RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0902Component],
  providers:[TokenExpiredGuard]
})
export class Ac0902Module { }
