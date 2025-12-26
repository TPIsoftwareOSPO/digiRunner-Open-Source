
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GtwidpComponent } from './gtwidp.component';
import { GtwidpRoutingModule } from './gtwidp.routing';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  declarations: [GtwidpComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    GtwidpRoutingModule,
    PrimengModule
  ]
})
export class GtwidpModule { }
