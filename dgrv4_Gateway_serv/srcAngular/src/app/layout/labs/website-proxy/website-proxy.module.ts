import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { WebsiteProxyRoutingModule } from './website-proxy-routing.module';
import { WebsiteProxyComponent } from './website-proxy.component';
import { WebsiteSettingComponent } from './website-setting/website-setting.component';
import { WebsiteSettingRowComponent } from './website-setting-row/website-setting-row.component';


@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    WebsiteProxyRoutingModule
  ],
  declarations: [WebsiteProxyComponent, WebsiteSettingComponent, WebsiteSettingRowComponent],
  providers:[TokenExpiredGuard]
})
export class WebsiteProxyModule { }
