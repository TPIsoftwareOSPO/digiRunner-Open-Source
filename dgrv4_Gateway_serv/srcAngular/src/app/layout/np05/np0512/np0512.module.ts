import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0512RoutingModule } from './np0512-routing.module';
import { Np0512Component } from './np0512.component';

@NgModule({
    imports: [
        CommonModule,
        Np0512RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0512Component],
    providers: [TokenExpiredGuard]
})
export class Np0512Module { }
