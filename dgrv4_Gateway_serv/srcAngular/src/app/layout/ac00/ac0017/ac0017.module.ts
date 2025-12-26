import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0017RoutingModule } from './ac0017-routing.module';
import { Ac0017Component } from './ac0017.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0017RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0017Component],
    providers: [TokenExpiredGuard]
})
export class Ac0017Module { }
