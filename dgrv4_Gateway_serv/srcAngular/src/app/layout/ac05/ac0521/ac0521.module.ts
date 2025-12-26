import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0521RoutingModule } from './ac0521-routing.module';
import { Ac0521Component } from './ac0521.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0521RoutingModule,
        SharedModule,
    ],
    declarations: [Ac0521Component],
    providers: [TokenExpiredGuard]
})
export class Ac0521Module { }
