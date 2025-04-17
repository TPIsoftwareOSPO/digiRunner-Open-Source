import { DialogModule } from 'primeng/dialog';
import { TokenExpiredGuard } from './../../shared/guard/token-expired.guard';
import { DashboardComponent } from './dashboard.component';
import { PrimengModule } from './../../shared/primeng.module';
import { SharedModule } from './../../shared/shared.module';
import { DashboardRoutingModule } from './dashboard.routing';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import { BadAttemptListComponent } from './bad-attempt-list/bad-attempt-list.component';



@NgModule({
  declarations: [DashboardComponent,BadAttemptListComponent],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    SharedModule,
    PrimengModule,
    ReactiveFormsModule,
    FormsModule,
    ScrollPanelModule
  ],
  providers:[TokenExpiredGuard]
})
export class DashboardModule { }
