import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0226Component } from './ac0226.component';
import { Ac0226RoutingModule } from './ac0226-routing.module';




@NgModule({
    imports: [
        CommonModule,
        Ac0226RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0226Component],
    providers: [TokenExpiredGuard]
})
export class Ac0226Module { }
