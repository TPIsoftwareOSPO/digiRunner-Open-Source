import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0103RoutingModule } from './ac0103-routing.module';
import { Ac0103Component } from './ac0103.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0103RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0103Component],
    providers: [TokenExpiredGuard]
})
export class Ac0103Module { }
