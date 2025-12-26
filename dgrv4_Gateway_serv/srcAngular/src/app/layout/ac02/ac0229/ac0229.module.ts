import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0229RoutingModule } from './ac0229-routing.module';
import { Ac0229Component } from './ac0229.component';


@NgModule({
    imports: [
        CommonModule,
        Ac0229RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0229Component],
    providers: [TokenExpiredGuard],

})
export class Ac0229Module { }
