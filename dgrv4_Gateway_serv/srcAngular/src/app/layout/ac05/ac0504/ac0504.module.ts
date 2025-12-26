import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0504RoutingModule } from './ac0504-routing.module';
import { Ac0504Component } from './ac0504.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { UserService } from 'src/app/shared/services/api-user.service';

@NgModule({
  imports: [
    CommonModule,
    Ac0504RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0504Component],
  providers:[TokenExpiredGuard,UtilService,UserService]
})
export class Ac0504Module { }
