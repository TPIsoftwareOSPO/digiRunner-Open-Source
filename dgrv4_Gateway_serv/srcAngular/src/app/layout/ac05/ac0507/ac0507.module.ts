import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0507RoutingModule } from './ac0507-routing.module';
import { Ac0507Component } from './ac0507.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0507RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0507Component],
    providers: [TokenExpiredGuard]
})
export class Ac0507Module { }
