import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0516RoutingModule } from './np0516-routing.module';
import { Np0516Component } from './np0516.component';

@NgModule({
    imports: [
        CommonModule,
        Np0516RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0516Component],
    providers: [TokenExpiredGuard]
})
export class Np0516Module { }
