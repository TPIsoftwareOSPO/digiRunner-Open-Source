import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0904RoutingModule } from './ac0904-routing.module';
import { Ac0904Component } from './ac0904.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0904RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0904Component],
  providers:[TokenExpiredGuard]
})
export class Ac0904Module { }
