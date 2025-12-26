import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0315RoutingModule } from './ac0315-routing.module';
import { Ac0315Component } from './ac0315.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
    imports: [
        CommonModule,
        Ac0315RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0315Component],
    providers: [TokenExpiredGuard]
})
export class Ac0315Module { }
