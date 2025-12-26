import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ac0105RoutingModule } from './ac0105-routing.module';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0105Component } from './ac0105.component';
import { LocaleFuncDetailComponent } from './locale-func-detail/locale-func-detail.component';
import { LocaleFuncFormComponent } from './locale-func-form/locale-func-form.component';

@NgModule({
  imports: [
    CommonModule,
    Ac0105RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [Ac0105Component, LocaleFuncDetailComponent, LocaleFuncFormComponent],
  providers: [TokenExpiredGuard],
})
export class Ac0105Module {}
