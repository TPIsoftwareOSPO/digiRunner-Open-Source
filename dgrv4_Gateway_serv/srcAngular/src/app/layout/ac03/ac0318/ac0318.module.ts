import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0318RoutingModule } from './ac0318-routing.module';
import { Ac0318Component } from './ac0318.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0318RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0318Component],
    providers: [TokenExpiredGuard]
})
export class Ac0318Module { }
