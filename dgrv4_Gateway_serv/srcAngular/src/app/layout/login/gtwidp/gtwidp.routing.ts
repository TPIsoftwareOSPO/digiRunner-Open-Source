import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GtwidpComponent } from './gtwidp.component';


const routes: Routes = [
    {
        path: '',
        component: GtwidpComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class GtwidpRoutingModule {}
