
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutRoutingModule } from './about-routing.module';
import { AboutComponent } from './about.component';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

@NgModule({
    imports: [
        CommonModule,
        AboutRoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [AboutComponent],
    providers: [TokenExpiredGuard]
})
export class AboutModule { }
