import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0319RoutingModule } from './ac0319-routing.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0319Component } from './ac0319.component';
import { FailHandlePolicyComponent } from './fail-handle-policy/fail-handle-policy.component';

@NgModule({
    imports: [
        CommonModule,
        Ac0319RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0319Component, FailHandlePolicyComponent],
    providers: [TokenExpiredGuard]
})
export class Ac0319Module { }
