import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0015Component } from './ac0015.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0015Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0015RoutingModule { }