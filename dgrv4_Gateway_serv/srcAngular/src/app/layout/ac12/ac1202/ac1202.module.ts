import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac1202RoutingModule } from './ac1202-routing.module';
import { Ac1202Component } from './ac1202.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
    imports: [
        CommonModule,
        Ac1202RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac1202Component],
    providers: [TokenExpiredGuard]
})
export class Ac1202Module { }
