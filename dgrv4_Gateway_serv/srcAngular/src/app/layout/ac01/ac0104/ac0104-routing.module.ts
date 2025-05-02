import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0104Component } from './ac0104.component';

const routes: Routes = [
    {
        path: '', component: Ac0104Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0104RoutingModule { }
