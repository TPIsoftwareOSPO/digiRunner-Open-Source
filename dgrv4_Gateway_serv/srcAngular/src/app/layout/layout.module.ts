import { FooterComponent } from './components/footer/footer.component';
import { HeaderComponent } from './components/header/header.component';
import { DialogModule } from 'primeng/dialog';
import { SidebarTemplateComponent } from './components/sidebar/sidebar-template/sidebar-template.component';
import { SharedPipesModule } from './../shared/pipes/shared-pipes.module';
import { SharedModule } from './../shared/shared.module';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { LayoutComponent } from './layout.component';
import { LayoutRoutingModule } from './layout.routing';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from '../shared/guard/auth.guard';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { PrimengModule } from '../shared/primeng.module';


// import {ToolbarModule} from 'primeng/toolbar';


@NgModule({
  imports: [
    CommonModule,
    LayoutRoutingModule,
    SharedModule,
    SharedPipesModule,
    MatSidenavModule,
    MatListModule,
    PrimengModule
  ],
  declarations: [
    LayoutComponent,
    SidebarComponent,
    HeaderComponent,
    FooterComponent,
    SidebarTemplateComponent,
    HeaderComponent
  ],
  providers: [AuthGuard]
})
export class LayoutModule { }
