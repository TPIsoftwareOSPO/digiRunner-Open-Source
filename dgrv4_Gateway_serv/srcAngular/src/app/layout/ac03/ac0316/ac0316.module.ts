import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0316RoutingModule } from './ac0316-routing.module';
import { Ac0316Component } from './ac0316.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { ApiTestComponent } from './api-test/api-test.component';

@NgModule({
    imports: [
        CommonModule,
        Ac0316RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0316Component],
    providers: [DynamicDialogConfig, ]
})
export class Ac0316Module { }
