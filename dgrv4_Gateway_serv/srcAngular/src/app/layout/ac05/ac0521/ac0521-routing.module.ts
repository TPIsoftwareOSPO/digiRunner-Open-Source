import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0521Component } from './ac0521.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0521Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0521RoutingModule { }
