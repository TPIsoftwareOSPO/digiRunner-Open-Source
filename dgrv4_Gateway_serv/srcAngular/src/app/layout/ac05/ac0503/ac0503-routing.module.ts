import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0503Component } from './ac0503.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0503Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0503RoutingModule { }
