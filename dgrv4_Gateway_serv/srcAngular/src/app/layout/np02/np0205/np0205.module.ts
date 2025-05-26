import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0205Component } from './np0205.component';
import { Np0205RoutingModule } from './np0205-routing.module';

@NgModule({
    imports: [
        CommonModule,
        Np0205RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0205Component],
    providers: [TokenExpiredGuard]
})
export class Np0205Module { }
