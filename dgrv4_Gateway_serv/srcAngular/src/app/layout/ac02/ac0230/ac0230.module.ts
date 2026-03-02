import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0230RoutingModule } from './ac0230-routing.module';
import { Ac0230Component } from './ac0230.component';



@NgModule({
    imports: [
        CommonModule,
        Ac0230RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0230Component],
    providers: [TokenExpiredGuard],

})
export class Ac0230Module { }
