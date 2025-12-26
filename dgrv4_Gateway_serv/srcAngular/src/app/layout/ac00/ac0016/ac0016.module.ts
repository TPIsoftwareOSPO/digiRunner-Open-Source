import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0016RoutingModule } from './ac0016-routing.module';
import { Ac0016Component } from './ac0016.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac0016RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0016Component],
    providers: [TokenExpiredGuard]
})
export class Ac0016Module { }
