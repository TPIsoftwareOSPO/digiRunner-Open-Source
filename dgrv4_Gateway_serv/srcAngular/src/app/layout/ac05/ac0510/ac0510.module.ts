import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0510RoutingModule } from './ac0510-routing.module';
import { Ac0510Component } from './ac0510.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0510RoutingModule,
    SharedModule,
  ],
  declarations: [Ac0510Component],
  providers:[TokenExpiredGuard]
})
export class Ac0510Module { }
