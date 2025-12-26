import { TokenExpiredGuard } from 'src/app/shared';
import { TsmpdpFileRoutingModule } from './tsmpdpfile-routing.module';
import { TsmpdpFileComponent } from './tsmpdpfile.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { FileSizePipe } from 'src/app/shared/pipes/file-size-pipe';


@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    TsmpdpFileRoutingModule
  ],
  declarations: [TsmpdpFileComponent,FileSizePipe],
  providers:[TokenExpiredGuard]
})
export class TsmpdpFileModule { }
