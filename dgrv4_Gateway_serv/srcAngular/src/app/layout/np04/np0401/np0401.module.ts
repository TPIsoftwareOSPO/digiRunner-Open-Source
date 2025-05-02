import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0401RoutingModule } from './np0401-routing.module';
import { Np0401Component } from './np0401.component';
import { RequisitionFormComponent } from './requisition-form/requisition-form.component';

@NgModule({
    imports: [
        CommonModule,
        Np0401RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0401Component, RequisitionFormComponent],
    entryComponents: [RequisitionFormComponent],
    providers: [TokenExpiredGuard]
})
export class Np0401Module { }
