import { Ac0019RoutingModule } from './ac0019-routing.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { Ac0019Component } from './ac0019.component';
import { LdapDataListComponent } from './ldap-data-list/ldap-data-list.component';
import { LdapDataListDetailComponent } from './ldap-data-list-detail/ldap-data-list-detail.component';


@NgModule({
    imports: [
        CommonModule,
        Ac0019RoutingModule,
        PrimengModule,
        SharedModule,
        SharedPipesModule,
        ReactiveFormsModule,
        FormsModule,
    ],
    declarations: [Ac0019Component, LdapDataListComponent, LdapDataListDetailComponent],
    providers: [TokenExpiredGuard]
})
export class Ac0019Module { }
