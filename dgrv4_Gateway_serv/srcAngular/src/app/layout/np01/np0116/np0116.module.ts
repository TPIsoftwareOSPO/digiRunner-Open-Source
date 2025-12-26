import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0116RoutingModule } from './np0116-routing.module';
import { Np0116Component } from './np0116.component';
import { AnnouncementComponent } from './announcement/announcement.component';

@NgModule({
    imports: [
        CommonModule,
        Np0116RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0116Component, AnnouncementComponent],
    entryComponents: [AnnouncementComponent],
    providers: [TokenExpiredGuard]
})
export class Np0116Module { }
