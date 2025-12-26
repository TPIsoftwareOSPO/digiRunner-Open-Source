import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0432RoutingModule } from './ac0432-routing.module';
import { Ac0432Component } from './ac0432.component';
import { PrimengModule } from 'src/app/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { SiteDetailComponent } from 'src/app/layout/ac04/ac0432/site-detail/site-detail.component';

@NgModule({
  imports: [
    CommonModule,
    Ac0432RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0432Component, SiteDetailComponent],
  entryComponents: [SiteDetailComponent],
  providers: [TokenExpiredGuard]
})
export class Ac0432Module { }
