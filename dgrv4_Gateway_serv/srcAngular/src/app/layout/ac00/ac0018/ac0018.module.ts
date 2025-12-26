
import { Ac0018Component } from './ac0018.component';
import { Ac0018RoutingModule } from './ac0018-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { FileSizePipe } from 'src/app/shared/pipes/file-size-pipe';


@NgModule({
    imports: [
        CommonModule,
        Ac0018RoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule,
    ],
    declarations: [Ac0018Component],
    providers: [TokenExpiredGuard]
})
export class Ac0018Module { }
