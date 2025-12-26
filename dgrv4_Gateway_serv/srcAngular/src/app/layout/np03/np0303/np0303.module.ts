import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0303RoutingModule } from './np0303-routing.module';
import { Np0303Component } from './np0303.component';

@NgModule({
    imports: [
        CommonModule,
        Np0303RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0303Component],
    providers: [TokenExpiredGuard]
})
export class Np0303Module { }
