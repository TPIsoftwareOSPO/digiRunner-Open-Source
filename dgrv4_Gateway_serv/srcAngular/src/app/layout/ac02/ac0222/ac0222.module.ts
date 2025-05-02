import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0222RoutingModule } from './ac0222-routing.module';
import { Ac0222Component } from './ac0222.component';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0222RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0222Component],
    providers: [TokenExpiredGuard]
})
export class Ac0222Module { }
