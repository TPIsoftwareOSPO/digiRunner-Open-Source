import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0302RoutingModule } from './np0302-routing.module';
import { Np0302Component } from './np0302.component';

@NgModule({
    imports: [
        CommonModule,
        Np0302RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0302Component],
    providers: [TokenExpiredGuard]
})
export class Np0302Module { }
