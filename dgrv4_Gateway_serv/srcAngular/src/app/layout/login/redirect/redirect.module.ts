
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RedirectRoutingModule } from './redirect.routing';
import { RedirectComponent } from './redirect.component';



@NgModule({
  declarations: [RedirectComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    RedirectRoutingModule
  ]
})
export class RedirectModule { }
