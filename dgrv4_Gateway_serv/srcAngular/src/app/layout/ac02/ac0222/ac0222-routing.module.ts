import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0222Component } from './ac0222.component';

const routes: Routes = [
    {
        path: '', component: Ac0222Component
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac0222RoutingModule { }
