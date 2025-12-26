import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0227RoutingModule } from './ac0227-routing.module';
import { Ac0227Component } from './ac0227.component';



@NgModule({
    imports: [
        CommonModule,
        Ac0227RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0227Component],
    providers: [TokenExpiredGuard]
})
export class Ac0227Module { }
