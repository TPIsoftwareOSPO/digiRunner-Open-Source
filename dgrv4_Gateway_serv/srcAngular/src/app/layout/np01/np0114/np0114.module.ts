import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0114RoutingModule } from './np0114-routing.module';
import { Np0114Component } from './np0114.component';

@NgModule({
    imports: [
        CommonModule,
        Np0114RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0114Component],
    providers: [TokenExpiredGuard]
})
export class Np0114Module { }
