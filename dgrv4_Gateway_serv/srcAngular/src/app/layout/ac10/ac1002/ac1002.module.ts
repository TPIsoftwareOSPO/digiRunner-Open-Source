import { NgModule } from "@angular/core";
import { TokenExpiredGuard } from "src/app/shared";
import { CommonModule } from "@angular/common";
import { PrimengModule } from "src/app/shared/primeng.module";
import { SharedModule } from "src/app/shared/shared.module";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";
import { Ac1002Component } from './ac1002.component';
import { Ac1002RoutingModule } from './ac1002-routing.module';
import { OrgDetailComponent } from './org-detail/org-detail.component';

@NgModule({
    imports: [
        CommonModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule,
        Ac1002RoutingModule
    ],
    declarations: [Ac1002Component, OrgDetailComponent],
    providers: [TokenExpiredGuard],
    entryComponents: [OrgDetailComponent]
})
export class Ac1002Module { }
