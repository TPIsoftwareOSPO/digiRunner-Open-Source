import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1202Component } from './ac1202.component';

const routes: Routes = [
    {
        path: '', component: Ac1202Component
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1202RoutingModule { }
