import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac1107Component } from './ac1107.component';

const routes: Routes = [
    {
        path: '', component: Ac1107Component
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1107RoutingModule { }
