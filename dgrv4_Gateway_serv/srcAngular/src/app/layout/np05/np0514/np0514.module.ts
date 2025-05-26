import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0514RoutingModule } from './np0514-routing.module';
import { Np0514Component } from './np0514.component';
import { ScheduleContentComponent } from './schedule-content/schedule-content.component';
import { ScheduleContentFormComponent } from './schedule-content-form/schedule-content-form.component';
import {MenuModule} from 'primeng/menu';
@NgModule({
    imports: [
        CommonModule,
        Np0514RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule,
        MenuModule
    ],
    declarations: [Np0514Component, ScheduleContentComponent, ScheduleContentFormComponent],
    entryComponents: [ScheduleContentComponent, ScheduleContentFormComponent],
    providers: [TokenExpiredGuard]
})
export class Np0514Module { }
