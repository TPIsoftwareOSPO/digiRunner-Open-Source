import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Lb0013Component } from './lb0013.component';
import { Lb0013RoutingModule } from './lb0013-routing.module';
import { DiversionListRowComponent } from './diversion-list-row/diversion-list-row.component';
import { DiversionListComponent } from './diversion-list/diversion-list.component';
import { ClientInfoListComponent } from './client-info-list/client-info-list.component';
import { StickyListComponent } from './sticky-list/sticky-list.component';

@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    Lb0013RoutingModule
  ],
  declarations: [Lb0013Component, DiversionListRowComponent, DiversionListComponent, ClientInfoListComponent, StickyListComponent],
  providers:[TokenExpiredGuard]
})
export class Lb0013Module { }
