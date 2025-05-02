import { TranslateModule } from '@ngx-translate/core';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';



@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    TranslateModule,PrimengModule
  ]
})
export class HeaderModule { }
