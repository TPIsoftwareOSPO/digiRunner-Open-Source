import { MessageService, ConfirmationService } from 'primeng/api';
import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from 'src/app/layout/base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { UserService } from 'src/app/shared/services/api-user.service';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ApiAlterService } from 'src/app/shared/services/api-alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
// import { MessageService } from 'primeng/components/common/messageservice';
import { AA0705Req } from 'src/app/models/api/AlertService/aa0705.interface';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { AA0706AlertSetting, AA0706Req } from 'src/app/models/api/AlertService/aa0706.interface';
import { AA0023Req, AA0023RoleInfo } from 'src/app/models/api/RoleService/aa0023.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { RoleService } from 'src/app/shared/services/api-role.service';
import * as ValidatorFns from '../../../shared/validator-functions';
import { isString } from 'util';
import { AA0701Req, searchMap } from 'src/app/models/api/AlertService/aa0701.interface';
import { AA0703Req, AA0703Resp, AA0703RoleInfo } from 'src/app/models/api/AlertService/aa0703.interface';
import { TranslateService } from '@ngx-translate/core';
import { AA0704Req } from 'src/app/models/api/AlertService/aa0704.interface';


@Component({
  selector: 'app-ac0706',
  templateUrl: './ac0706.component.html',
  styleUrls: ['./ac0706.component.css'],
  providers: [UserService, ApiAlterService, MessageService, ConfirmationService]
})
export class Ac0706Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;

  form: FormGroup;
  queryStatus: { label: string; value: string; }[] = [];
  createStatus: { label: string; value: string; }[] = [];
  updateStatus: { label: string; value: string; }[] = [];
  dialogTitle: string = '';
  alertSettingListCols: { field: string; header: string; }[] = [];
  alertSettingList: Array<AA0706AlertSetting> = [];
  alertSettingListRowcount: number = 0;
  pageNum: number = 1; // 1: 查詢、2: 查詢下的角色清單、3: 建立、4: 告警角色清單、5: 詳細資料、6: 更新
  currentTitle: string = this.title;
  roleInfoListCols: { field: string; header: string; }[] = [];
  roleInfoList: Array<AA0023RoleInfo> = [];
  roleInfoListRowcount: number = 0;
  selectedRole?: AA0023RoleInfo;
  alertTypes: Array<{ label: string; value: string; }> = new Array();
  exceptionTypes: Array<{ label: string; value: string; }> = new Array();
  selectedAlertRole: Array<AA0023RoleInfo> = [];
  currentAlert?: AA0706AlertSetting;
  currentAlertDetail?: AA0703Resp;
  currentAction: string = 'query';
  exc_days_pl: string = '';
  page1_keyword: string = '';
  page1_alertEnabled: string = '-1';
  page1_roleAlias: string = '';
  roleForm: FormGroup;

  constructor(
    protected router: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private messageService: MessageService,
    private alertService: ApiAlterService,
    private list: ListService,
    private roleService: RoleService,
    private translateService: TranslateService,
    private confirmationService: ConfirmationService
  ) {
    super(router, tr);

    this.form = this.fb.group({
      keyword: new FormControl(''),
      roleAlias: new FormControl({ value: '', disabled: true }),
      roleName: new FormControl(''),
      alertEnabled: new FormControl('-1'),
      alertSys: new FormControl(''),
      alertName: new FormControl(''),
      alertType: new FormControl(''),
      logField: new FormControl(''),
      rtnValue: new FormControl(''),
      moduleName: new FormControl(''),
      txid: new FormControl(''),
      alertMsg: new FormControl(''),
      elapse: new FormControl(''),
      threshold: new FormControl(''),
      duration: new FormControl(''),
      alertInterval: new FormControl(''),
      cFlag: new FormControl(false),
      imFlag: new FormControl(false),
      imType: new FormControl(''),
      imId: new FormControl(''),
      exType: new FormControl('N'),
      exDays: new FormControl(''),
      exTimeS: new FormControl({ hour: 0, minute: 0 }),
      exTimeE: new FormControl({ hour: 0, minute: 0 }),
      exTime: new FormControl(''),
      roleIDList: new FormControl([]),
      alertDesc: new FormControl(''),
      searchMapString: new FormControl(''),
      alertId: new FormControl('')
    });
    this.roleForm = this.fb.group({
      keyword: new FormControl('')
    })
  }

  ngOnInit() {

    // status
    let ReqBody = {
      encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('ENABLE_FLAG')) + ',' + 9,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        let _queryStatus: { label: string, value: string }[] = [];
        let _createStatus: { label: string, value: string }[] = [];
        let _updateStatus: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            if (item.subitemNo != '2') {
              _queryStatus.push({ label: item.subitemName, value: item.subitemNo });
              if (item.subitemNo != '-1') {
                _createStatus.push({ label: item.subitemName, value: item.subitemNo });
                _updateStatus.push({ label: item.subitemName, value: item.subitemNo });
              }
            }
          }
        }
        this.queryStatus = _queryStatus;
        this.createStatus = _createStatus;
        this.updateStatus = _updateStatus;
      }
    });
    this.alertType!.valueChanges.subscribe(type => {
      this.logField!.setValue('');
      this.rtnValue!.setValue('');
      this.moduleName!.setValue('');
      this.txid!.setValue('');
      this.elapse!.setValue('');
      if (type == 'responseTime') {
        this.elapse!.setValidators(ValidatorFns.requiredValidator());
        this.elapse!.updateValueAndValidity();
      }
      else {
        this.elapse!.clearValidators();
        this.elapse!.updateValueAndValidity();
      }
    });
    this.init();
  }

  private async init(): Promise<void> {
    const codes = ['alert_id', 'alert_name', 'alert_sys', 'alert_type', 'status', 'role_id', 'role_name', 'role_alias', 'none', 'daily', 'weekly', 'monthly', 'exc_days_pl'];
    const dict = await this.toolService.getDict(codes);
    this.exc_days_pl = dict['exc_days_pl'];
    this.alertSettingListCols = [
      { field: 'alertID', header: dict['alert_id'] },
      { field: 'alertName', header: dict['alert_name'] },
      { field: 'alertSys', header: dict['alert_sys'] },
      { field: 'alertType', header: dict['alert_type'] },
      { field: 'alertEnabled', header: dict['status'] }
    ]
    this.roleInfoListCols = [
      { field: 'roleId', header: dict['role_id'] },
      { field: 'roleName', header: dict['role_name'] },
      { field: 'roleAlias', header: dict['role_alias'] }
    ];
    // alert 選單
    this.alertTypes.push({ label: "Keyword", value: "keyword" });
    this.alertTypes.push({ label: "Response Time", value: "responseTime" });
    // exception 選單
    this.exceptionTypes.push({ label: dict['none'], value: "N" });
    this.exceptionTypes.push({ label: dict['daily'], value: "D" });
    this.exceptionTypes.push({ label: dict['weekly'], value: "W" });
    this.exceptionTypes.push({ label: dict['monthly'], value: "M" });
    this.alertSettingList = [];
    this.alertSettingListRowcount = this.alertSettingList.length;
    let ReqBody = {
      keyword: this.keyword!.value,
      roleName: this.roleName!.value,
      alertEnabled: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.alertEnabled!.value)) + ',' + this.convertAlertEnabledIndex(this.alertEnabled!.value)
    } as AA0706Req;
    this.alertService.queryAlarmSettings_ignore1298(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.alertSettingList = res.RespBody.alertSettingList;
        this.alertSettingListRowcount = this.alertSettingList.length;
      }
    });
  }

  submitForm() {
    this.alertSettingList = [];
    this.alertSettingListRowcount = this.alertSettingList.length;
    let ReqBody = {
      keyword: this.keyword!.value,
      roleName: this.roleName!.value,
      alertEnabled: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.alertEnabled!.value)) + ',' + this.convertAlertEnabledIndex(this.alertEnabled!.value)
    } as AA0706Req;
    this.alertService.queryAlarmSettings(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.alertSettingList = res.RespBody.alertSettingList;
        this.alertSettingListRowcount = this.alertSettingList.length;
      }
    });
  }

  moreAlertData() {
    let ReqBody = {
      lastAlertId: this.alertSettingList[this.alertSettingList.length - 1].alertID,
      keyword: this.keyword!.value,
      roleName: this.roleName!.value,
      alertEnabled: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.alertEnabled!.value)) + ',' + this.convertAlertEnabledIndex(this.alertEnabled!.value)
    } as AA0706Req;
    this.alertService.queryAlarmSettings(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.alertSettingList = this.alertSettingList.concat(res.RespBody.alertSettingList);
        this.alertSettingListRowcount = this.alertSettingList.length;
      }
    });
  }

  createAlertm() {
    let ReqBody = {
      alertDesc: this.alertDesc!.value,
      alertEnabled: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.alertEnabled!.value)) + ',' + this.convertAlertEnabledIndex(this.alertEnabled!.value),
      alertInterval: this.alertInterval!.value,
      alertMsg: this.alertMsg!.value,
      alertName: this.alertName!.value,
      alertSys: this.alertSys!.value,
      alertType: this.alertType!.value,
      cFlag: this.cFlag!.value.toString(),
      duration: this.duration!.value,
      exDays: this.exDays!.value,
      exType: this.exType!.value,
      imFlag: this.imFlag!.value.toString(),
      imType: this.imType!.value,
      imId: this.imId!.value,
      roleIDList: this.roleIDList!.value,
      threshold: this.threshold!.value,
      searchMapString: JSON.stringify(this.convertSearchMapString(this.alertType!.value))
    } as AA0701Req;
    if (isNaN(this.exTimeS!.value.hour) == false && isNaN(this.exTimeS!.value.minute) == false && isNaN(this.exTimeE!.value.hour) == false && isNaN(this.exTimeE!.value.minute) == false) {
      ReqBody.exTime = this.transferTime(this.exTimeS!.value.hour) + this.transferTime(this.exTimeS!.value.minute) + "-" + this.transferTime(this.exTimeE!.value.hour) + this.transferTime(this.exTimeE!.value.minute)
    }
    if (ReqBody.imFlag == 'true') { ReqBody.imType = "Line"; }
    switch (ReqBody.exType) {
      case "N":
        ReqBody.exDays = "";
        ReqBody.exTime = "0000-0000";
        break;
      case "D":
        ReqBody.exDays = "";
        break;
    }
    // console.log('req :', ReqBody)
    this.alertService.addAlarmSettings(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.alert', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.alert']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
        this.alertEnabled!.setValue('-1');
        this.roleAlias!.setValue('');
        this.roleName!.setValue('');
        this.submitForm();
        this.changePage('query');
      }
    });
  }

  updateAlertm() {
    let ReqBody = {
      alertId: this.currentAlertDetail?.alertId,
      alertDesc: this.alertDesc!.value,
      alertEnabled: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.alertEnabled!.value)) + ',' + this.convertAlertEnabledIndex(this.alertEnabled!.value),
      alertInterval: this.alertInterval!.value,
      alertMsg: this.alertMsg!.value,
      alertName: this.currentAlertDetail?.alertName,
      alertSys: this.alertSys!.value,
      alertType: this.alertType!.value,
      cFlag: this.cFlag!.value.toString(),
      duration: this.duration!.value,
      exDays: this.exDays!.value,
      exType: this.exType!.value,
      imFlag: this.imFlag!.value.toString(),
      imType: this.imType!.value,
      imId: this.imId!.value,
      roleIDList: this.roleIDList!.value,
      threshold: this.threshold!.value,
      searchMapString: JSON.stringify(this.convertSearchMapString(this.alertType!.value))
    } as AA0704Req;
    if (isNaN(this.exTimeS!.value.hour) == false && isNaN(this.exTimeS!.value.minute) == false && isNaN(this.exTimeE!.value.hour) == false && isNaN(this.exTimeE!.value.minute) == false) {
      ReqBody.exTime = this.transferTime(this.exTimeS!.value.hour) + this.transferTime(this.exTimeS!.value.minute) + "-" + this.transferTime(this.exTimeE!.value.hour) + this.transferTime(this.exTimeE!.value.minute)
    }
    if (ReqBody.imFlag == 'true') { ReqBody.imType = "Line"; }
    switch (ReqBody.exType) {
      case "N":
        ReqBody.exDays = "";
        ReqBody.exTime = "0000-0000";
        break;
      case "D":
        ReqBody.exDays = "";
        break;
    }
    this.alertService.updateAlertSetting(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.alert', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({ severity: 'success', summary: `${dict['message.update']} ${dict['message.alert']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
        this.alertEnabled!.setValue('-1');
        this.roleAlias!.setValue('');
        this.roleName!.setValue('');
        this.submitForm();
        this.changePage('query');
      }
    });
  }

  public async delete(item: AA0706AlertSetting): Promise<void> {
    const codes = ['cfm_del_alert', 'alert_id', 'alert_name'];
    const dict = await this.toolService.getDict(codes);
    this.currentAlert = item;
    this.messageService.clear();
    // this.messageService.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_alert'], detail: `${dict['alert_id']} : ${this.currentAlert.alertID} , ${dict['alert_name']} : ${this.currentAlert.alertName}` });
    this.confirmationService.confirm({
      header: dict['cfm_del_alert'],
      message: `${dict['alert_id']} : ${this.currentAlert.alertID} , <br>${dict['alert_name']} : ${this.currentAlert.alertName}`,
      accept: () => {
        this.onDeleteConfirm();
      }
    });
  }

  public onDeleteConfirm(): void {
    this.messageService.clear();
    let ReqBody = {
      alertID: this.currentAlert?.alertID,
      alertName: this.currentAlert?.alertName
    } as AA0705Req;
    this.alertService.deleteAlertSetting(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const codes = ['message.delete', 'message.alert', 'message.success'];
        const dict = await this.toolService.getDict(codes);
        this.messageService.add({ severity: 'success', summary: `${dict['message.delete']}`, detail: `${dict['message.delete']} ${dict['message.success']}!` });
        this.submitForm();
      }
    });
  }

  public onReject(): void {
    this.messageService.clear();
  }

  convertSearchMapString(alertType: string): object {
    let _searchMapString = {} as searchMap;
    switch (alertType) {
      case 'keyword':
        _searchMapString[this.logField!.value] = this.rtnValue!.value;
        // _searchMapString.keyword = this.form.get('keyword').value
        return _searchMapString;
        break;
      case 'responseTime':
        _searchMapString['elapse'] = this.elapse!.value,
          _searchMapString['txid'] = this.txid!.value,
          _searchMapString['moduleName'] = this.moduleName!.value
        return _searchMapString;
      default:
        return _searchMapString;
    }
  }

  transferTime(time: number): string {
    time = Number(time);
    if (time < 10) { return "0" + time; }
    return time.toString();
  }

  convertAlertEnabledIndex(alertEnabled: string): number {
    switch (alertEnabled) {
      case '1':
        return 0;
      case '0':
        return 1;
      case '-1':
        return 2;
      case '2':
        return 3;
      default:
        return -1;
    }
  }

  changeControlStatus() {
    switch (this.exType!.value) {
      case "N":
        this.exTimeS!.setValue("");
        this.exTimeE!.setValue("");
        this.exDays!.setValue("");
        this.exDays!.disable();
        this.exTimeS!.disable();
        this.exTimeE!.disable();
        break;
      case "D":
        this.exDays!.setValue("");
        this.exDays!.disable();
        this.exTimeS!.enable();
        this.exTimeE!.enable();
        break;
      case "W":
        this.exDays!.enable();
        this.exTimeS!.enable();
        this.exTimeE!.enable();
        break;
      case "M":
        this.exDays!.enable();
        this.exTimeS!.enable();
        this.exTimeE!.enable();
        break;
    }
  }

  queryRoleMappingList() {
    this.roleInfoList = [];
    this.roleInfoListRowcount = this.roleInfoList.length;
    let ReqBody = {
      keyword: this.roleForm.get('keyword')!.value
    } as AA0023Req;
    this.roleService.queryRoleRoleList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.roleInfoList = res.RespBody.roleRoleMappingList;
        this.roleInfoListRowcount = this.roleInfoList.length;
      }
    });
  }

  moreData() {
    let ReqBody = {
      keyword: this.roleForm.get('keyword')!.value,
      roleId: this.roleInfoList[this.roleInfoList.length - 1].roleId
    } as AA0023Req;
    this.roleService.queryRoleRoleList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.roleInfoList = this.roleInfoList.concat(res.RespBody.roleRoleMappingList);
        this.roleInfoListRowcount = this.roleInfoList.length;
      }
    });
  }

  chooseRole() {
    this.currentTitle = this.title;
    this.pageNum = 1;
    // this.changePage('query');
    window.setTimeout(() => {
      if (this.selectedRole) {
        this.roleName!.setValue(this.selectedRole.roleName);
        this.roleAlias!.setValue(this.selectedRole.roleAlias);
      }
      else {
        this.roleName!.setValue('');
        this.roleAlias!.setValue('');
      }
    });
  }

  async showAlerRoleList() {
    const code = ['role_alias', 'button.create', 'alert_role_list'];
    const dict = await this.toolService.getDict(code);
    this.currentTitle = `${this.title} > ${dict['button.create']} > ${dict['alert_role_list']}`;
    this.pageNum = 4;
    this.selectedAlertRole = new Array<AA0023RoleInfo>();
    // if (isString(this.roleAlias.value) && isString(this.roleIDList.value)) {
    if (isString(this.roleName!.value) && isString(this.roleIDList!.value)) {
      // this.roleAlias.setValue([]);
      this.roleName!.setValue([]);
      this.roleIDList!.setValue([]);
    }
    this.queryRoleMappingList();
  }

  async chooseAlertRole() {
    const code = ['button.create', 'button.update'];
    const dict = await this.toolService.getDict(code);
    this.roleForm.get('keyword')!.setValue('');
    switch (this.currentAction) {
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 3;
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 6;
        break;
    }
    window.setTimeout(() => {
      let set = new Set();
      // let _roleAlias = [];
      let _roleName: string[] = [];
      let _roleId: string[] = [];
      this.roleIDList!.value.map(roleId => {
        set.add(roleId);
        _roleId.push(roleId);
      });
      // this.roleAlias.value.map(roleAlias => {
      //     _roleAlias.push(roleAlias);
      // });
      this.roleName!.value.map(roleName => {
        _roleName.push(roleName);
      })
      this.selectedAlertRole.map(item => {
        if (!set.has(item.roleId)) {
          // _roleAlias.push(item.roleAlias);
          _roleName.push(item.roleName);
          _roleId.push(item.roleId);
        }
      });
      // this.roleAlias.setValue(_roleAlias);
      this.roleName!.setValue(_roleName);
      this.roleIDList!.setValue(_roleId);
    });
  }

  async chooseAlertRole_return() {
    const code = ['button.create', 'button.update'];
    const dict = await this.toolService.getDict(code);
    this.roleForm.get('keyword')!.setValue('');
    switch (this.currentAction) {
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 3;
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 6;
        break;
    }
  }

  deleteRole(idx: number) {
    // this.roleAlias.value.splice(idx, 1);
    this.roleName!.value.splice(idx, 1);
    this.roleIDList!.value.splice(idx, 1);
  }

  searchMapStringTransform(alertType: string, searchMapString: string): string {
    let _searchMapString = JSON.parse(searchMapString);
    let str = '';
    switch (alertType) {
      case 'keyword':
        Object.keys(_searchMapString).map(key => {
          str = `${key} - ${_searchMapString[key]}`;
        });
        return str;
      case 'responseTime':
        str = `${_searchMapString['moduleName']} - ${_searchMapString['txid']}`;
        return str;
      default:
        return '';
    }
  }

  responseTimeTransform(searchMapString: string): string {
    let _searchMapString = JSON.parse(searchMapString);
    return _searchMapString['elapse'];
  }

  exTypeTransform(exType: string): string {
    let _exType = ''
    this.translateService.get(['none', 'daily', 'weekly', 'monthly']).subscribe(i18n => {
      switch (exType) {
        case 'N':
          _exType = i18n['none'];
          break;
        case 'D':
          _exType = i18n['daily'];
          break;
        case 'W':
          _exType = i18n['weekly'];
          break;
        case 'M':
          _exType = i18n['monthly'];
          break;
      }
    });
    return _exType;
  }

  roleInfoListTransform(roleInfoList: Array<AA0703RoleInfo>): string {
    let _roleName: string[] = [];
    roleInfoList.map(item => {
      _roleName.push(item.roleName);
    });
    return _roleName.join('，');
  }

  async changePage(action: string, rowData?: AA0706AlertSetting) {
    const code = ['role_alias', 'button.create', 'alert_role_list', 'button.detail', 'button.update'];
    const dict = await this.toolService.getDict(code);
    // this.resetFormValidator(this.form);
    this.currentAction = action;
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        this.alertEnabled!.setValue(this.page1_alertEnabled);
        this.roleAlias!.setValue(this.page1_roleAlias);
        this.roleName!.setValue('');
        this.keyword!.setValue(this.page1_keyword);
        break;
      case 'query_role':
        this.roleForm.get('keyword')!.setValue('');
        this.currentTitle = `${this.title} > ${dict['role_alias']}`;
        this.pageNum = 2;
        this.selectedRole = {} as AA0023RoleInfo;
        this.queryRoleMappingList();
        break;
      case 'create':
        this.page1_keyword = this.keyword!.value;
        this.page1_roleAlias = this.roleAlias!.value;
        this.page1_alertEnabled = this.alertEnabled!.value;
        this.resetFormValidator(this.form);
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 3;
        this.alertService.addAlarmSettings_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
            // 預設 alert type
            this.alertType!.setValue('keyword');
            this.exType!.setValue('N');
            // 更改選單狀態
            this.changeControlStatus();
            this.cFlag!.setValue(false);
            this.imFlag!.setValue(false);
            this.alertName!.enable();
            this.alertType!.enable();
          }
        });
        break;
      case 'detail':
        this.currentAlert = rowData;
        let detailReqBody = {
          alertId: rowData!.alertID,
          alertName: rowData!.alertName
        } as AA0703Req;
        this.alertService.queryAlertSettingDetail(detailReqBody).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentAlertDetail = res.RespBody;
            this.currentTitle = `${this.title} > ${dict['button.detail']}`;
            this.pageNum = 5;
          }
        });
        break;
      case 'update':
        this.page1_keyword = this.keyword!.value;
        this.page1_roleAlias = this.roleAlias!.value;
        this.page1_alertEnabled = this.alertEnabled!.value;
        this.resetFormValidator(this.form);
        this.currentAlert = rowData;

        this.alertService.updateAlertSetting_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {

            this.addFormValidator(this.form, res.RespBody.constraints);

            let _detailReqBody = {
              alertId: rowData?.alertID,
              alertName: rowData?.alertName
            } as AA0703Req;
            this.alertService.queryAlertSettingDetail(_detailReqBody).subscribe(detailRes => {
              if (this.toolService.checkDpSuccess(detailRes.ResHeader)) {
                this.currentAlertDetail = detailRes.RespBody;


                this.alertId!.setValue(this.currentAlertDetail.alertId);
                this.alertName!.setValue(this.currentAlertDetail.alertName);
                this.alertName!.disable();
                this.alertSys!.setValue(this.currentAlertDetail.alertSys);
                this.alertType!.setValue(this.currentAlertDetail.alertType);
                this.alertType!.disable();
                this.alertEnabled!.setValue(this.currentAlertDetail.alertEnabled);
                this.alertMsg!.setValue(this.currentAlertDetail.alertMsg);
                this.threshold!.setValue(this.currentAlertDetail.threshold);
                this.duration!.setValue(this.currentAlertDetail.duration);
                this.alertInterval!.setValue(this.currentAlertDetail.alertInterval);
                this.cFlag!.setValue(this.currentAlertDetail.cFlag == 'true' ? true : false);
                this.imFlag!.setValue(this.currentAlertDetail.imFlag == 'true' ? true : false);
                this.imType!.setValue(this.currentAlertDetail.imType);
                this.imId!.setValue(this.currentAlertDetail.imId);
                this.exType!.setValue(this.currentAlertDetail.exType);
                this.exDays!.setValue(this.currentAlertDetail.exDays);
                if (this.currentAlertDetail.exTime != undefined) {
                  let times = this.currentAlertDetail.exTime.split('-');
                  if (times.length > 1) {
                    this.exTimeS!.setValue({ hour: times[0].slice(0, 2), minute: times[0].slice(2, 4) });
                    this.exTimeE!.setValue({ hour: times[1].slice(0, 2), minute: times[1].slice(2, 4) });
                  }
                }
                this.alertDesc!.setValue(this.currentAlertDetail.alertDesc);
                if (this.currentAlertDetail.searchMapString) {
                  let _searchMap = {} as searchMap;
                  _searchMap = JSON.parse(this.currentAlertDetail.searchMapString);
                  window.setTimeout(() => {
                    switch (this.currentAlertDetail!.alertType) {
                      case 'keyword':
                        Object.keys(_searchMap).map(key => {
                          this.logField!.setValue(key);
                          this.rtnValue!.setValue(_searchMap[key]);
                        });
                        break;
                      case 'responseTime':
                        Object.keys(_searchMap).map(key => {
                          this.form.get(key)!.setValue(_searchMap[key]);
                        });
                        break;
                    }
                  }, 500);
                }
                let _roleId: string[] = [];
                let _roleName: string[] = [];
                this.currentAlertDetail.roleInfoList.map(item => {
                  _roleId.push(item.roleId);
                  _roleName.push(item.roleName);
                });
                this.roleIDList!.setValue(_roleId);
                this.roleName!.setValue(_roleName);
                this.changeControlStatus();

                this.currentTitle = `${this.title} > ${dict['button.update']}`;
                this.pageNum = 3;
              }
            });
          }
        });
    }
  }

  headerReturn() {

    if (this.pageNum == 4) {
      this.chooseAlertRole_return();
    }
    else
      this.changePage('query')

  }

  public get keyword() { return this.form.get('keyword'); }
  public get roleAlias() { return this.form.get('roleAlias'); }
  public get roleName() { return this.form.get('roleName'); }
  public get alertEnabled() { return this.form.get('alertEnabled'); }
  public get alertName() { return this.form.get('alertName'); }
  public get alertSys() { return this.form.get('alertSys'); }
  public get alertType() { return this.form.get('alertType'); }
  public get logField() { return this.form.get('logField'); }
  public get rtnValue() { return this.form.get('rtnValue'); }
  public get moduleName() { return this.form.get('moduleName'); }
  public get txid() { return this.form.get('txid'); }
  public get alertMsg() { return this.form.get('alertMsg'); }
  public get elapse() { return this.form.get('elapse'); }
  public get threshold() { return this.form.get('threshold'); }
  public get duration() { return this.form.get('duration'); }
  public get alertInterval() { return this.form.get('alertInterval'); }
  public get cFlag() { return this.form.get('cFlag'); }
  public get imFlag() { return this.form.get('imFlag'); }
  public get imType() { return this.form.get('imType'); }
  public get imId() { return this.form.get('imId'); }
  public get exType() { return this.form.get('exType'); }
  public get exDays() { return this.form.get('exDays'); }
  public get exTime() { return this.form.get('exTime'); }
  public get exTimeS() { return this.form.get('exTimeS'); }
  public get exTimeE() { return this.form.get('exTimeE'); }
  public get roleIDList() { return this.form.get('roleIDList'); }
  public get alertDesc() { return this.form.get('alertDesc'); }
  public get alertId() { return this.form.get('alertId'); }

}
