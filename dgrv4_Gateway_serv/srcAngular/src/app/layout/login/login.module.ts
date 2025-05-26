import { SharedModule } from './../../shared/shared.module';
import { TranslateModule } from '@ngx-translate/core';
import { LoginComponent } from './login.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginRoutingModule } from './login.routing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RedirectComponent } from './redirect/redirect.component';



@NgModule({
  imports: [
    CommonModule,
    LoginRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule
  ],
  declarations: [LoginComponent],
})
export class LoginModule { }
