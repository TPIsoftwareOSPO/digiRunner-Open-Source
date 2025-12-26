import { PrimengModule } from './../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1301RoutingModule } from './ac1301-routing.module';
import { Ac1301Component } from './ac1301.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';


@NgModule({
    imports: [
        CommonModule,
        Ac1301RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1301Component],
    providers: [TokenExpiredGuard]
})
export class Ac1301Module { }
