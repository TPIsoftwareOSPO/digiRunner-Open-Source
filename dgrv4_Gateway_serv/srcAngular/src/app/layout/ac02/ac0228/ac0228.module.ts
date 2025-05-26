import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0228RoutingModule } from './ac0228-routing.module';
import { Ac0228Component } from './ac0228.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { KeyValueFormComponent } from './key-value-form/key-value-form.component';
import { KeyValueFieldComponent } from './key-value-field/key-value-field.component';

@NgModule({
    imports: [
        CommonModule,
        Ac0228RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0228Component, KeyValueFormComponent, KeyValueFieldComponent],
    providers: [TokenExpiredGuard],
    exports:[KeyValueFormComponent, KeyValueFieldComponent]
})
export class Ac0228Module { }
