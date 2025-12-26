import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0908RoutingModule } from './ac0908-routing.module';
import { Ac0908Component } from './ac0908.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  imports: [
    CommonModule,
    Ac0908RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0908Component],
  providers:[TokenExpiredGuard]
})
export class Ac0908Module { }
