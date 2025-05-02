import { IdpssoRoutingModule } from './idpsso.routing';
import { SharedModule } from './../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { IdpssoComponent } from './idpsso.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ProgressBarModule} from 'primeng/progressbar';


@NgModule({

  imports: [
    CommonModule,
    IdpssoRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    ProgressBarModule
  ],
  declarations: [IdpssoComponent],
})
export class IdpssoModule { }
