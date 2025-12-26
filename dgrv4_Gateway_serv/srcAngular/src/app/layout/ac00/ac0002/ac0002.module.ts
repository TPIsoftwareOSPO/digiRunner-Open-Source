
import { PrimengModule } from './../../../shared/primeng.module';
import { UserService } from './../../../shared/services/api-user.service';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0002RoutingModule } from './ac0002-routing.module';
import { Ac0002Component } from './ac0002.component';

import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0002RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ac0002Component],
  providers:[UserService,TokenExpiredGuard]
})
export class Ac0002Module { }
