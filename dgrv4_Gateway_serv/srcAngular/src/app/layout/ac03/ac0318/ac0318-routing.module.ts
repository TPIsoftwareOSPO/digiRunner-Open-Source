import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0318Component } from './ac0318.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0318Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0318RoutingModule { }