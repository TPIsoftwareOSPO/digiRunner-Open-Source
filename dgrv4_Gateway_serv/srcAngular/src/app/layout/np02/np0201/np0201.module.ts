import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0201RoutingModule } from './np0201-routing.module';
import { Np0201Component } from './np0201.component';

@NgModule({
    imports: [
        CommonModule,
        Np0201RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0201Component],
    providers: [TokenExpiredGuard]
})
export class Np0201Module { }
