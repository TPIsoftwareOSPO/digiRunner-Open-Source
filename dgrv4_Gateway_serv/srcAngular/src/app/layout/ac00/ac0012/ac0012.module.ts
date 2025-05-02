import { UserService } from './../../../shared/services/api-user.service';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0012RoutingModule } from './ac0012-routing.module';
import { Ac0012Component } from './ac0012.component';

import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0012RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0012Component],
  providers:[UserService,TokenExpiredGuard]
})
export class Ac0012Module { }
