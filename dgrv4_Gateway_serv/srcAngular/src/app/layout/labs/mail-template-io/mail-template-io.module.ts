import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { MailTemplateIoRoutingModule } from './mail-template-io-routing.module';
import { MailTemplateIoComponent } from './mail-template-io.component';

@NgModule({
    imports: [
        CommonModule,
        MailTemplateIoRoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [MailTemplateIoComponent],
    providers: [TokenExpiredGuard],

})
export class MailTemplateIoModule { }
