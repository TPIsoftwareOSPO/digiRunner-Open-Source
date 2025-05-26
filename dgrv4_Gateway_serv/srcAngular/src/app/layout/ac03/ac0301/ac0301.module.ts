import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0301RoutingModule } from './ac0301-routing.module';
import { Ac0301Component } from './ac0301.component';

import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';

import { SwaggerComponent } from './swagger/swagger.component';
import { MockHeadersInputComponent } from './mock-headers-input/mock-headers-input.component';
import { MockHeadersInputDetailComponent } from './mock-headers-input-detail/mock-headers-input-detail.component';
import { ApiStatusComponent } from './api-status/api-status.component';
import { ApiStatusModifyComponent } from './api-status-modify/api-status-modify.component';
import { ApiDetailComponent } from './api-detail/api-detail.component';
import { JwtSettingComponent } from './jwt-setting/jwt-setting.component';

@NgModule({
  imports: [
    CommonModule,
    Ac0301RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,

  ],
  declarations: [Ac0301Component,  SwaggerComponent, MockHeadersInputComponent, MockHeadersInputDetailComponent , JwtSettingComponent],
  providers:[TokenExpiredGuard]
})
export class Ac0301Module { }
