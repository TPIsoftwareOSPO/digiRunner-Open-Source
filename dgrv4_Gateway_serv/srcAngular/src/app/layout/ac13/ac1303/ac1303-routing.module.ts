import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1303Component } from './ac1303.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac1303Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1303RoutingModule { }
