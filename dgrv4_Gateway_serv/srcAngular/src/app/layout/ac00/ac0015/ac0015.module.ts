import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0015RoutingModule } from './ac0015-routing.module';
import { Ac0015Component } from './ac0015.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0015RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0015Component],
    providers: [TokenExpiredGuard]
})
export class Ac0015Module { }
