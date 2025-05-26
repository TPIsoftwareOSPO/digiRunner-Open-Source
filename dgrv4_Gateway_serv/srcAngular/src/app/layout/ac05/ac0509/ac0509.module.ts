import { SharedModule } from './../../../shared/shared.module';
import { Ac0509Component } from './ac0509.component';
import { Ac0509RoutingModule } from './ac0509-routing.module';
import { UserService } from '../../../shared/services/api-user.service';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';

import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { ParamItemComponent } from './param-item/param-item.component';


@NgModule({
  imports: [
    CommonModule,
    Ac0509RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0509Component,ParamItemComponent],
  providers:[UserService,TokenExpiredGuard],
  entryComponents:[ParamItemComponent]
})
export class Ac0509Module { }
