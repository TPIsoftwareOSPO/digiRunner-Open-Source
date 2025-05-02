import { PrimengModule } from './../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1303RoutingModule } from './ac1303-routing.module';
import { Ac1303Component } from './ac1303.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';


@NgModule({
    imports: [
        CommonModule,
        Ac1303RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1303Component],
    providers: [TokenExpiredGuard]
})
export class Ac1303Module { }
