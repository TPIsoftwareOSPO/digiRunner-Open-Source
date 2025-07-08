import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0320Component } from './ac0320.component';
import { Ac0320RoutingModule } from './ac0320-routing.module';

@NgModule({
    imports: [
        CommonModule,
        Ac0320RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0320Component],
    providers: [TokenExpiredGuard]
})
export class Ac0320Module { }
