import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1116RoutingModule } from './ac1116-routing.module';
import { Ac1116Component } from './ac1116.component';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac1116RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1116Component],
    providers: [TokenExpiredGuard]
})
export class Ac1116Module { }
