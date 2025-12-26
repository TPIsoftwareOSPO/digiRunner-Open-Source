import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1302Component } from './ac1302.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac1302Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1302RoutingModule { }
