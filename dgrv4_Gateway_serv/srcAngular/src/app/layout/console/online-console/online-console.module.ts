import { OnlineConsoleRoutingModule } from './online-console-routing.module';
import { OnlineConsoleComponent } from './online-console.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';


@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    OnlineConsoleRoutingModule
  ],
  declarations: [OnlineConsoleComponent],
  providers:[TokenExpiredGuard]
})
export class OnlineConsoleModule { }
