import { Routes, RouterModule } from "@angular/router";
import { Ac1002Component } from './ac1002.component';
import { TokenExpiredGuard } from "src/app/shared";
import { NgModule } from "@angular/core";

const routes: Routes = [
    {
        path: '', component: Ac1002Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Ac1002RoutingModule { }