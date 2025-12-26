import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0231RoutingModule } from './ac0231-routing.module';
import { Ac0231Component } from './ac0231.component';


@NgModule({
    imports: [
        CommonModule,
        Ac0231RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0231Component],
    providers: [TokenExpiredGuard],

})
export class Ac0231Module { }
