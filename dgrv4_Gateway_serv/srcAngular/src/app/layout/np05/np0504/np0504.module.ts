import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0504RoutingModule } from './np0504-routing.module';
import { Np0504Component } from './np0504.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
    imports: [
        CommonModule,
        Np0504RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0504Component],
    providers: [TokenExpiredGuard]
})
export class Np0504Module { }
