import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0910Component } from './ac0910.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
    {
        path: '', component: Ac0910Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0910RoutingModule { }
