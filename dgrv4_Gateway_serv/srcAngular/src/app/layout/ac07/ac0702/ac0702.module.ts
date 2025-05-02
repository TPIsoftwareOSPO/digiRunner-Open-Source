import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { Ac0702RoutingModule } from './ac0702-routing.module';
import { Ac0702Component } from './ac0702.component';

@NgModule({
    imports: [
        CommonModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule,
        Ac0702RoutingModule
    ],
    declarations: [Ac0702Component],
    providers: [TokenExpiredGuard]
})
export class Ac0702Module { }
