import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0906RoutingModule } from './ac0906-routing.module';
import { Ac0906Component } from './ac0906.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0906RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0906Component],
  providers:[TokenExpiredGuard]
})
export class Ac0906Module { }
