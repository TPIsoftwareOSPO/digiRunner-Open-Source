import { FuncService } from './../services/api-func.service';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListboxStringSplitPipe } from './listbox-string-split.pipe';
import { ListboxArraySplitPipe } from './listbox-array-split.pipe';
import { TransformMenuNamePipe } from './transform-menu-name.pipe';
import { APIStatusPipe } from './api-status.pipe';
import { ModuleStatusPipe } from './module.status.pipe';
import { SafePipe } from './safe.pipe';
import { ClientStatusPipe } from './client.status.pipe';
import { DatetimeFormatPipe } from './datetime.format.pipe';
import { UserStatusPipe } from './user-status.pipe';
import { TaskStatusPipe } from './task.status.pipe';
import { APISrcPipe } from './api-src.pipe';
import { TimeSplitPipe } from './time-split.pipe';
import { ClientPublicStatusPipe } from './client-public-status.pipe';
import { APIJWTSStatusPipe } from './api-jwt-status.pipe';
import { StringLengthPipe } from './string-length.pipe';
import { EventFlagPipe } from './event-flag.pipe';
import { UTCDatetimeFormatPipe } from './utc-datetime.format.pipe';
import { IndexStatusPipe } from 'src/app/shared/pipes/index-status.pipe';
import { DataChangeStatusPipe } from './data-change-status.pipe';
import { ReportTypePipe } from './reportType.pipe';
import { AiUsageLimitPolicyPipe } from './ai-usage-limit-policy.pipe';
import { SafeUrlPipe } from './safe-url.pipe';

@NgModule({
  imports: [CommonModule],
  declarations: [
    ListboxStringSplitPipe,
    ListboxArraySplitPipe,
    TransformMenuNamePipe,
    APIStatusPipe,
    ModuleStatusPipe,
    SafePipe,
    ClientStatusPipe,
    DatetimeFormatPipe,
    UserStatusPipe,
    TaskStatusPipe,
    APISrcPipe,
    TimeSplitPipe,
    ClientPublicStatusPipe,
    APIJWTSStatusPipe,
    StringLengthPipe,
    EventFlagPipe,
    IndexStatusPipe,
    UTCDatetimeFormatPipe,
    DataChangeStatusPipe,
    ReportTypePipe,
    AiUsageLimitPolicyPipe,
    SafeUrlPipe,
  ],
  exports: [
    ListboxStringSplitPipe,
    ListboxArraySplitPipe,
    TransformMenuNamePipe,
    APIStatusPipe,
    ModuleStatusPipe,
    SafePipe,
    ClientStatusPipe,
    DatetimeFormatPipe,
    UserStatusPipe,
    TaskStatusPipe,
    APISrcPipe,
    TimeSplitPipe,
    ClientPublicStatusPipe,
    APIJWTSStatusPipe,
    StringLengthPipe,
    EventFlagPipe,
    IndexStatusPipe,
    UTCDatetimeFormatPipe,
    DataChangeStatusPipe,
    ReportTypePipe,
    AiUsageLimitPolicyPipe,
    SafeUrlPipe,
  ],
  providers: [FuncService],
})
export class SharedPipesModule {}
