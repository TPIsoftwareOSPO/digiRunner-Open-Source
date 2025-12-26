import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0204RoutingModule } from './np0204-routing.module';
import { Np0204Component } from './np0204.component';

@NgModule({
    imports: [
        CommonModule,
        Np0204RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0204Component],
    providers: [TokenExpiredGuard]
})
export class Np0204Module { }
