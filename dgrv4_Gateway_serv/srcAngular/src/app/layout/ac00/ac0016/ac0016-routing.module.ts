import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0016Component } from './ac0016.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0016Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0016RoutingModule { }
