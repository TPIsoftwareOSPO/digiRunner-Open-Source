import { KeyValueDetailComponent } from './key-value/key-value-detail/key-value-detail.component';
import { KeyValueGridModule } from './key-value-grid/key-value-grid.module';
import { KeyValueGridComponent } from './key-value-grid/key-value-grid.component';
import { ApiTestComponent } from './../layout/ac03/ac0316/api-test/api-test.component';
import { LogoutService } from './services/logout.service';
import { ApiListComponent } from './../layout/np03/np0304/api-list/api-list.component';
import { OpenApiKeyFormComponent } from './../layout/np03/np0304/open-api-key-form/open-api-key-form.component';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { ClientAuthorizeApiComponent } from './../layout/np03/np0303/client-authorize-api/client-authorize-api.component';
import { ApiOnOffComponent } from './../layout/np03/np0301/api-on-off/api-on-off.component';
import { ApiShelvesComponent } from './../layout/np03/np0301/api-shelves/api-shelves.component';
import { ThemeLovComponent } from './../layout/np03/np0301/theme-lov/theme-lov.component';
import { ApiLovComponent } from './../layout/np03/np0301/api-lov/api-lov.component';
import { CADetailComponent } from './../layout/np02/np0202/ca-detail/ca-detail.component';
import { FrameComponent } from './../layout/components/frame/frame.component';
import { OrgFormComponent } from './../layout/ac10/ac1002/org-form/org-form.component';
import { TimepickerComponent } from './../layout/components/timepicker/timepicker.component';
// import { HostInputDetailComponent } from './../layout/ac02/ac0202/host-input-detail/host-input-detail.component';
import { DialogComponent } from './dialog/dialog.component';
import { ListGroupsComponent } from 'src/app/shared/list-group/list-groups.component';
import { ListGroupComponent } from './list-group/list-group.component';
import { RoleFormComponent } from '../layout/ac00/ac0012/role-form/role-form.component';
import { FuncListComponent } from '../layout/ac00/ac0012/func-list/func-list.component';
import { RoleMappingListLovComponent } from './role-mapping-list-lov/role-mapping-list-lov.component';
import { RoleListLovComponent } from './role-list-lov/role-list-lov.component';
import { DialogService, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { KeyValueComponent } from 'src/app/shared/key-value/key-value.module';
import { MessageService, ConfirmationService } from 'primeng/api';
import { OrganizationComponent } from './organization/organization.component';
import { ContainerComponent } from './container/container.component';

import { KeyValueModule } from './key-value/key-value.module';
import { SharedPipesModule } from './pipes/shared-pipes.module';
import { TransformMenuNamePipe } from './pipes/transform-menu-name.pipe';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { SidebarService } from './../layout/components/sidebar/sidebar.service';
import { PrimengModule } from './primeng.module';
import { ApiBaseService } from './services/api-base.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TranslateModule } from '@ngx-translate/core';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TOrgService } from './services/org.service';
import { EditFuncListComponent } from '../layout/ac00/ac0012/edit-func-list/edit-func-list.component';
import { HostInputComponent } from '../layout/ac02/ac0202/host-input/host-input.component';
import { HostInputDetailComponent } from '../layout/ac02/ac0202/host-input-detail/host-input-detail.component';
import { JobFormComponent } from '../layout/np05/np0513/job-form/job-form.component';
import { KeyValueGridDetailComponent } from './key-value-grid/key-value--grid-detail/key-value-grid-detail.component';
import { FieldsFormComponent } from '../layout/components/fields/fields-form/fields-form.component';
import { FieldsFormDetailComponent } from '../layout/components/fields/fields-form-detail/fields-form-detail.component';
import { SourceIpFormComponent } from '../layout/components/source-ip/source-ip-form/source-ip-form.component';
import { SourceIpFormDetailComponent } from '../layout/components/source-ip/source-ip-form-detail/source-ip-form-detail.component';
import { SrcUrlRegDetailComponent } from '../layout/ac03/ac0311/src-url-reg-detail/src-url-reg-detail.component';
import { SrcUrlRegComponent } from '../layout/ac03/ac0311/src-url-reg/src-url-reg.component';
import { SrcUrlInputDetailComponent } from '../layout/ac03/ac0301/src-url-input-detail/src-url-input-detail.component';
import { SrcUrlInputComponent } from '../layout/ac03/ac0301/src-url-input/src-url-input.component';
import { ConnectionInfoListComponent } from './connection-info-list/connection-info-list.component';
import { LabelListComponent } from './label-list/label-list.component';
import { ManagerGroupListComponent } from './manager-group-list/manager-group-list.component';
import { TargetSiteListComponent } from './target-site-list/target-site-list.component';
import { ApiUrlSettingComponent } from '../layout/ac03/ac0319/api-url-setting/api-url-setting.component';
import { ApiUrlSettingDetailComponent } from '../layout/ac03/ac0319/api-url-setting-detail/api-url-setting-detail.component';
import { LabelResetComponent } from './label-reset/label-reset.component';
import { DatepickerComponent } from '../layout/components/datepicker/datepicker.component';
import { WhiteListFormComponent } from '../layout/labs/lb0010/white-list-form/white-list-form.component';
import { WhiteListDetailComponent } from '../layout/labs/lb0010/white-list-detail/white-list-detail.component';
import { ApiDetailComponent } from '../layout/ac03/ac0301/api-detail/api-detail.component';
import { ApiStatusModifyComponent } from '../layout/ac03/ac0301/api-status-modify/api-status-modify.component';
import { ApiStatusComponent } from '../layout/ac03/ac0301/api-status/api-status.component';
import { CustomAlertComponent } from './custom-alert/custom-alert.component';
import { ClientCAComponent } from './../layout/np02/np0202/client-ca/client-ca.component';
import { UserDetailComponent } from '../layout/ac00/ac0002/user-detail/user-detail.component';
import { SearchInputComponent } from '../layout/components/search-input/search-input.component';
import { NotifyListComponent } from './notify-list/notify-list.component';

@NgModule({
  imports: [
    PrimengModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    SharedPipesModule,
    TranslateModule,
    KeyValueModule,
    KeyValueGridModule,
  ],
  exports: [
    TranslateModule,
    SharedPipesModule,
    ContainerComponent,
    OrganizationComponent,
    KeyValueComponent,
    RoleListLovComponent,
    RoleMappingListLovComponent,
    OrganizationComponent,
    EditFuncListComponent,
    FuncListComponent,
    RoleFormComponent,
    ListGroupComponent,
    ListGroupsComponent,
    DialogComponent,
    HostInputComponent,
    HostInputDetailComponent,
    TimepickerComponent,
    OrgFormComponent,
    FrameComponent,
    JobFormComponent,
    CADetailComponent,
    ApiLovComponent,
    ThemeLovComponent,
    ApiShelvesComponent,
    ApiOnOffComponent,
    ClientAuthorizeApiComponent,
    OpenApiKeyFormComponent,
    ApiListComponent,
    ApiTestComponent,
    KeyValueGridComponent,
    KeyValueDetailComponent,
    KeyValueGridDetailComponent,
    FieldsFormComponent,
    FieldsFormDetailComponent,
    SourceIpFormComponent,
    SourceIpFormDetailComponent,
    SrcUrlRegComponent,
    SrcUrlRegDetailComponent,
    SrcUrlInputComponent,
    SrcUrlInputDetailComponent,
    ConnectionInfoListComponent,
    LabelListComponent,
    ManagerGroupListComponent,
    TargetSiteListComponent,
    ApiUrlSettingComponent,
    ApiUrlSettingDetailComponent,
    LabelResetComponent,
    DatepickerComponent,
    WhiteListFormComponent,
    WhiteListDetailComponent,
    ApiDetailComponent,
    ApiStatusComponent,
    ApiStatusModifyComponent,
    CustomAlertComponent,
    ClientCAComponent,
    UserDetailComponent,
    SearchInputComponent,
    NotifyListComponent,
  ],
  declarations: [
    ContainerComponent,
    OrganizationComponent,
    RoleListLovComponent,
    RoleMappingListLovComponent,
    OrganizationComponent,
    EditFuncListComponent,
    FuncListComponent,
    RoleFormComponent,
    ListGroupComponent,
    ListGroupsComponent,
    DialogComponent,
    HostInputComponent,
    HostInputDetailComponent,
    TimepickerComponent,
    OrgFormComponent,
    FrameComponent,
    JobFormComponent,
    CADetailComponent,
    ApiLovComponent,
    ThemeLovComponent,
    ApiShelvesComponent,
    ApiOnOffComponent,
    ClientAuthorizeApiComponent,
    OpenApiKeyFormComponent,
    ApiListComponent,
    ApiTestComponent,
    KeyValueDetailComponent,
    KeyValueGridDetailComponent,
    FieldsFormComponent,
    FieldsFormDetailComponent,
    SourceIpFormComponent,
    SourceIpFormDetailComponent,
    SrcUrlRegComponent,
    SrcUrlRegDetailComponent,
    SrcUrlInputComponent,
    SrcUrlInputDetailComponent,
    ConnectionInfoListComponent,
    LabelListComponent,
    ManagerGroupListComponent,
    TargetSiteListComponent,
    ApiUrlSettingComponent,
    ApiUrlSettingDetailComponent,
    LabelResetComponent,
    DatepickerComponent,
    WhiteListFormComponent,
    WhiteListDetailComponent,
    ApiDetailComponent,
    ApiStatusComponent,
    ApiStatusModifyComponent,
    CustomAlertComponent,
    ClientCAComponent,
    UserDetailComponent,
    SearchInputComponent,
    NotifyListComponent
  ],
  providers: [ApiBaseService, MessageService],
})
export class SharedModule {
  static forRoot(): ModuleWithProviders<SharedModule> {
    return {
      ngModule: SharedModule,
      providers: [
        ToolService,
        ApiBaseService,
        SidebarService,
        ToolService,
        AlertService,
        TokenService,
        ClientService,
        TransformMenuNamePipe,
        // ExcelService,
        // SearchService,
        TOrgService,
        // MessageService
        // DialogService
        DialogService,
        // ConfirmationService,
        LogoutService,
        DynamicDialogConfig,
      ],
    };
  }
}
