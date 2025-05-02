import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0301RoutingModule } from './np0301-routing.module';
import { Np0301Component } from './np0301.component';

@NgModule({
    imports: [
        CommonModule,
        Np0301RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0301Component],
    providers: [TokenExpiredGuard]
})
export class Np0301Module { }
