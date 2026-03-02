import { LdapComponent } from './ldap.component';
import { SharedModule } from './../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LdapRoutingModule } from './ldap.routing';



@NgModule({
  declarations: [LdapComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    LdapRoutingModule
  ]
})
export class LdapModule { }
