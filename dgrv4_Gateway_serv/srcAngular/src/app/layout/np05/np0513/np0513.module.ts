import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0513RoutingModule } from './np0513-routing.module';
import { Np0513Component } from './np0513.component';
import { JobDetailComponent } from './job-detail/job-detail.component';

@NgModule({
    imports: [
        CommonModule,
        Np0513RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0513Component, JobDetailComponent],
    entryComponents: [JobDetailComponent],
    providers: [TokenExpiredGuard]
})
export class Np0513Module { }
