import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Za0000RoutingModule } from './za0000-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { za0000Component } from './za0000.component';


@NgModule({
  imports: [
    CommonModule,
    Za0000RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [za0000Component],
  providers:[TokenExpiredGuard]
})
export class Za0000Module { }
