import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { TsmpdpitemsRoutingModule } from './tsmpdpitems-routing.module';
import { TsmpdpitemsComponent } from './tsmpdpitems.component';
import { TokenExpiredGuard } from 'src/app/shared';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { LocaleItemComponent } from './locale-item/locale-item.component';
import { ParamItemComponent } from './param-item/param-item.component';
import { LocaleComponent } from './locale/locale.component';
import { SubitemsComponent } from './subitems/subitems.component';



@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    TsmpdpitemsRoutingModule
  ],
  declarations: [TsmpdpitemsComponent, LocaleItemComponent, ParamItemComponent, LocaleComponent, SubitemsComponent],
  entryComponents : [LocaleItemComponent, ParamItemComponent, LocaleComponent],
  providers:[TokenExpiredGuard]
})
export class TsmpdpitemsModule { }
