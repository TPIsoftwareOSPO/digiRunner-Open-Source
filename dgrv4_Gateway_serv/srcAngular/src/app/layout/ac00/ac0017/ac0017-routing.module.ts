import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0017Component } from './ac0017.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0017Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0017RoutingModule { }
