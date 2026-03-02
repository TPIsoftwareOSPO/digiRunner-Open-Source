import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1304Component } from './ac1304.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac1304Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1304RoutingModule { }
