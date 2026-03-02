import { Lb0010RoutingModule } from './lb0010-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { Lb0010Component } from './lb0010.component';

@NgModule({
    imports: [
        CommonModule,
        Lb0010RoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule,
    ],
    declarations: [Lb0010Component],
    providers: [TokenExpiredGuard]
})
export class Lb0010Module { }
