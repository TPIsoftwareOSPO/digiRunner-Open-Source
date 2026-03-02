import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1116Component } from './ac1116.component';

const routes: Routes = [
    {
        path: '', component: Ac1116Component
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1116RoutingModule { }
