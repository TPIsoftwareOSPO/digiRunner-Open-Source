import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0511RoutingModule } from './np0511-routing.module';
import { Np0511Component } from './np0511.component';
import { EventDetailComponent } from './event-detail/event-detail.component';

@NgModule({
    imports: [
        CommonModule,
        Np0511RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0511Component, EventDetailComponent],
    entryComponents: [EventDetailComponent],
    providers: [TokenExpiredGuard]
})
export class Np0511Module { }
