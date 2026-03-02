import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0202RoutingModule } from './np0202-routing.module';
import { Np0202Component } from './np0202.component';

@NgModule({
    imports: [
        CommonModule,
        Np0202RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0202Component],
    providers: [TokenExpiredGuard]
})
export class Np0202Module { }
