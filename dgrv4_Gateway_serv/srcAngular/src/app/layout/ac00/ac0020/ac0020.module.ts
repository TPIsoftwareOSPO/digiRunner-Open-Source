import { Ac0020RoutingModule } from './ac0020-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { Ac0020Component } from './ac0020.component';
import { KeyValueFormComponent } from '../../ac02/ac0228/key-value-form/key-value-form.component';
import { KeyValueFieldComponent } from '../../ac02/ac0228/key-value-field/key-value-field.component';
import { Ac0228Module } from '../../ac02/ac0228/ac0228.module';



@NgModule({
    imports: [
        CommonModule,
        Ac0020RoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule,
        Ac0228Module
    ],
    declarations: [Ac0020Component],
    providers: [TokenExpiredGuard]
})
export class Ac0020Module { }
