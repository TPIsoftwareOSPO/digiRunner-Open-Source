import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1305Component } from './ac1305.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac1305Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1305RoutingModule { }
