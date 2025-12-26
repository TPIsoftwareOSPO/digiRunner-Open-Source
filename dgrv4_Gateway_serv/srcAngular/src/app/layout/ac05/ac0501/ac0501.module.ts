import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0501RoutingModule } from './ac0501-routing.module';
import { Ac0501Component } from './ac0501.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { TokenExpiredGuard } from 'src/app/shared';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
    imports: [
        CommonModule,
        Ac0501RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0501Component],
    providers: [TokenExpiredGuard]
})
export class Ac0501Module { }
