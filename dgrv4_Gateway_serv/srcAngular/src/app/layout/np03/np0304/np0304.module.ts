import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0304RoutingModule } from './np0304-routing.module';
import { Np0304Component } from './np0304.component';

@NgModule({
    imports: [
        CommonModule,
        Np0304RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0304Component],
    providers: [TokenExpiredGuard]
})
export class Np0304Module { }
