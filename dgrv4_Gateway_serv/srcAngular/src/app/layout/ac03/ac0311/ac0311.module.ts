import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0311RoutingModule } from './ac0311-routing.module';
import { Ac0311Component } from './ac0311.component';

import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { FileAccessorDirective } from 'src/app/shared/directives/file-accessor-directive';
import { RegHostService } from 'src/app/shared/services/api-reg-host.service';
// import { SrcUrlRegComponent } from './src-url-reg/src-url-reg.component';
// import { SrcUrlRegDetailComponent } from './src-url-reg-detail/src-url-reg-detail.component';
import { HostnameConfigComponent } from './hostname-config/hostname-config.component';
import { HostnameConfigFormComponent } from './hostname-config-form/hostname-config-form.component';

@NgModule({
    imports: [
        CommonModule,
        Ac0311RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Ac0311Component, FileAccessorDirective, HostnameConfigComponent, HostnameConfigFormComponent],
    providers: [TokenExpiredGuard, RegHostService]
})
export class Ac0311Module { }
