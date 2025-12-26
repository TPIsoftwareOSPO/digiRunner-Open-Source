import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { Ac0706RoutingModule } from './ac0706-routing.module';
import { Ac0706Component } from './ac0706.component';

@NgModule({
    imports: [
        CommonModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule,
        Ac0706RoutingModule
    ],
    declarations: [Ac0706Component],
    providers: [TokenExpiredGuard]
})
export class Ac0706Module { }
