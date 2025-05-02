
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ProfileRoutingModule } from './profile-routing.module';
import { ProfileComponent } from './profile.component';
import { TokenExpiredGuard, SharedPipesModule } from 'src/app/shared';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { UserService } from 'src/app/shared/services/api-user.service';

@NgModule({
  imports: [
    CommonModule,
    ProfileRoutingModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [ProfileComponent],
  providers:[TokenExpiredGuard,UserService,ToolService]
})
export class ProfileModule { }
