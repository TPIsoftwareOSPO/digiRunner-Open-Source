import { Ac0021RoutingModule } from './ac0021-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { Ac0021Component } from './ac0021.component';

@NgModule({
    imports: [
        CommonModule,
        Ac0021RoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule,
    ],
    declarations: [Ac0021Component],
    providers: [TokenExpiredGuard]
})
export class Ac0021Module { }
