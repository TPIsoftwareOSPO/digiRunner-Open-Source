import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0103Component } from './ac0103.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0103Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0103RoutingModule { }