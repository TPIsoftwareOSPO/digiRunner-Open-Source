import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {  Ac0104RoutingModule } from './ac0104-routing.module';
import {  Ac0104Component } from './ac0104.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SharedPipesModule, TokenExpiredGuard } from 'src/app/shared';
// import { IndexService } from 'srcAngular/app/shared/services/api-index.service';

@NgModule({
    imports: [
        CommonModule,
        Ac0104RoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0104Component],
    providers: [TokenExpiredGuard]
})
export class Ac0104Module { }
