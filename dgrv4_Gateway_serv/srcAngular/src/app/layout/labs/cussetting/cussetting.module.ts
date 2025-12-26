import { TokenExpiredGuard } from 'src/app/shared';
import { CussettingRoutingModule } from './cussetting-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CussettingComponent } from './cussetting.component';

@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    CussettingRoutingModule
  ],
  declarations: [CussettingComponent],
  providers:[TokenExpiredGuard]
})
export class CussettingModule { }
