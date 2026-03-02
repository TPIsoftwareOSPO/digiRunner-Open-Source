import { TokenExpiredGuard } from 'src/app/shared';
import { RdbConnectionRoutingModule } from './rdb-connection-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RdbConnectionComponent } from './rdb-connection.component';
import { Ac0228Module } from '../../ac02/ac0228/ac0228.module';


@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    RdbConnectionRoutingModule,
    Ac0228Module
  ],
  declarations: [RdbConnectionComponent],
  providers:[TokenExpiredGuard]
})
export class RdbConnectionModule { }
