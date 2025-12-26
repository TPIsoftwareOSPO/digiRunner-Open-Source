import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0900RoutingModule } from './ac0900-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { Ac0900Component } from './ac0900.component';


@NgModule({
  imports: [
    CommonModule,
    Ac0900RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0900Component],
  providers:[TokenExpiredGuard]
})
export class Ac0900Module { }
