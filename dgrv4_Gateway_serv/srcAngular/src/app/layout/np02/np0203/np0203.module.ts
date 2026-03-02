import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0203RoutingModule } from './np0203-routing.module';
import { Np0203Component } from './np0203.component';

@NgModule({
    imports: [
        CommonModule,
        Np0203RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0203Component],
    providers: [TokenExpiredGuard]
})
export class Np0203Module { }
