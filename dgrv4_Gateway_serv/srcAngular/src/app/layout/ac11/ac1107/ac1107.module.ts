import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1107RoutingModule } from './ac1107-routing.module';
import { Ac1107Component } from './ac1107.component';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac1107RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1107Component],
    providers: [TokenExpiredGuard]
})
export class Ac1107Module { }
