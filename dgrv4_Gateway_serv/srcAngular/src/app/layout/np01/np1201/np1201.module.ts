import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np1201RoutingModule } from './np1201-routing.module';
import { Np1201Component } from './np1201.component';

@NgModule({
    imports: [
        CommonModule,
        Np1201RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    entryComponents: [],
    declarations: [Np1201Component],
    providers: [TokenExpiredGuard]
})
export class Np1201Module { }
