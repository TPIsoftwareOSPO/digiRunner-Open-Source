import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Ac0019Component } from './ac0019.component';

const routes: Routes = [
    {
        path: '', component: Ac0019Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0019RoutingModule { }
