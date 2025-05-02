import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from './../primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    SharedModule,
  ]
})
export class DialogModule { }
