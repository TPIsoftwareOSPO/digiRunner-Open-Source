import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0115RoutingModule } from './np0115-routing.module';
import { Np0115Component } from './np0115.component';
import { DetailComponent } from './detail/detail.component';

@NgModule({
    imports: [
        CommonModule,
        Np0115RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    entryComponents: [DetailComponent],
    declarations: [Np0115Component, DetailComponent],
    providers: [TokenExpiredGuard]
})
export class Np0115Module { }
