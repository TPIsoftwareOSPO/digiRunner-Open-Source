import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0228Component } from './ac0228.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0228Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0228RoutingModule { }
