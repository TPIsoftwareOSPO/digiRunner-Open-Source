import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0322RoutingModule } from './ac0322-routing.module';
import { Ac0322Component } from './ac0322.component';

@NgModule({
    imports: [
        CommonModule,
        Ac0322RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0322Component],
    providers: [TokenExpiredGuard],

})
export class Ac0322Module { }
