import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0402RoutingModule } from './np0402-routing.module';
import { Np0402Component } from './np0402.component';

@NgModule({
    imports: [
        CommonModule,
        Np0402RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0402Component],
    providers: [TokenExpiredGuard]
})
export class Np0402Module { }
