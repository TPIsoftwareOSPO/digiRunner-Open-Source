import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1302RoutingModule } from './ac1302-routing.module';
import { Ac1302Component } from './ac1302.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';


@NgModule({
    imports: [
        CommonModule,
        Ac1302RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1302Component],
    providers: [TokenExpiredGuard]
})
export class Ac1302Module { }
