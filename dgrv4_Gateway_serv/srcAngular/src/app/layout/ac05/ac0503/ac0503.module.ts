import { PrimengModule } from '../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0503RoutingModule } from './ac0503-routing.module';
import { Ac0503Component } from './ac0503.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';


@NgModule({
    imports: [
        CommonModule,
        Ac0503RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0503Component],
    providers: [TokenExpiredGuard]
})
export class Ac0503Module { }
