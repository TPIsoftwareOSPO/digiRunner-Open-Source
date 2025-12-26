import { PrimengModule } from './../../../shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1305RoutingModule } from './ac1305-routing.module';
import { Ac1305Component } from './ac1305.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';


@NgModule({
    imports: [
        CommonModule,
        Ac1305RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1305Component],
    providers: [TokenExpiredGuard]
})
export class Ac1305Module { }
