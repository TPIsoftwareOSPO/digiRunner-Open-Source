import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0502RoutingModule } from './ac0502-routing.module';
import { Ac0502Component } from './ac0502.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0502RoutingModule,
    SharedModule,
  ],
  declarations: [Ac0502Component],
  providers:[TokenExpiredGuard]
})
export class Ac0502Module { }
