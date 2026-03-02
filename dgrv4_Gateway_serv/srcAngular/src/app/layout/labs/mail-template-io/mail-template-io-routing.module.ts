import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { MailTemplateIoComponent } from './mail-template-io.component';

const routes: Routes = [
    {
        path: '', component: MailTemplateIoComponent, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class MailTemplateIoRoutingModule { }
