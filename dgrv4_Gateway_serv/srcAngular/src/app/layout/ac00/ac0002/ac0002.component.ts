import { LogoutService } from './../../../shared/services/logout.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { UserService } from './../../../shared/services/api-user.service';
import { DialogComponent } from './../../../shared/dialog/dialog.component';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute, Router } from '@angular/router';
import { FormParams } from '../../../models/api/form-params.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ConfirmationService, MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
// import { TOrgService } from 'src/app/shared/services/org.service';
import { AA1002List } from 'src/app/models/api/OrgService/aa1002.interface';
import { AA0003Req, AA0003Resp } from 'src/app/models/api/UserService/aa0003.interface';
import { AA0019Req, AA0019List } from 'src/app/models/api/UserService/aa0019.interface';
// import { RoleListLovComponent } from 'src/app/shared/role-list-lov/role-list-lov.component';
// import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import { AA0005Req } from 'src/app/models/api/UserService/aa0005.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
// import { ListService } from 'src/app/shared/services/api-list.service';
// import { RoleMappingListLovComponent } from 'src/app/shared/role-mapping-list-lov/role-mapping-list-lov.component';
import { AA0001Req } from 'src/app/models/api/UserService/aa0001.interface';
import { AA0004Req } from 'src/app/models/api/UserService/aa0004.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { RoleListLovComponent } from 'src/app/shared/role-list-lov/role-list-lov.component';
import { RoleMappingListLovComponent } from 'src/app/shared/role-mapping-list-lov/role-mapping-list-lov.component';
import { TOrgService } from 'src/app/shared/services/org.service';
import { DialogService } from 'primeng/dynamicdialog';
import { OrganizationComponent } from 'src/app/shared/organization/organization.component';


@Component({
  selector: 'app-ac0002',
  templateUrl: './ac0002.component.html',
  styleUrls: ['./ac0002.component.css'],
  providers: [ConfirmationService]
})
export class Ac0002Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;



  orgList: AA1002List[] = [];
  form: FormGroup;
  roles: { label: string; value: string; }[] = [];
  displayOrgChart: boolean = false;
  dialogTitle: string = '';
  canCreate: boolean = false;
  canUpdate: boolean = false;
  canDelete: boolean = false;
  rowcount: number = 0;
  cols: { field: string; header: string; }[] = [];
  i18n: any;
  userInfoList: Array<AA0019List> = new Array<AA0019List>();
  closable: boolean = true;
  pageNum: number = 1; // 1: 查詢、2: 詳細資料 & 刪除、3: 建立 & 更新
  currentTitle: string = this.title;
  userDetail?: AA0003Resp;
  currentAction: string = 'query';
  userNameLimitChar = { value: 50 };
  userAliasLimitChar = { value: 30 };
  userMailLimitChar = { value: 100 };
  userBlockLimitChar = { value: 128 };
  createStatus: { label: string; value: string; }[] = [];
  updateStatus: { label: string; value: string; }[] = [];
  roleAliasList: string[] = [];
  newRoleAliasList: string[] = [];

  newUserAliasLimitChar = { value: 30 }

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private userService: UserService,
    private toolService: ToolService,
    private messageService: MessageService,
    private translate: TranslateService,
    private orgService: TOrgService,
    private roleService: RoleService,
    private listService: ListService,
    private alert: AlertService,
    private router: Router,
    private dialogService: DialogService,
    private confirmationService: ConfirmationService,
    private logoutService: LogoutService
  ) {
    super(route, tr);
    this.form = this.fb.group({
      roleAlias: new FormControl({ value: '', disabled: true }),
      roleName: new FormControl(''),
      orgName: new FormControl({ value: '', disabled: true }),
      keyword: new FormControl(''),
      userName: new FormControl(''),
      userAlias: new FormControl(''),
      userBlock: new FormControl(''),
      confirmUserBlock: new FormControl(''),
      userMail: new FormControl(''),
      encodeStatus: new FormControl(''),
      orgID: new FormControl(''),
      roleIDList: new FormControl([]),
      newUserName: new FormControl(''),
      newUserMail: new FormControl(''),
      newUserAlias: new FormControl(''),
      newStatus: new FormControl(''),
      newRoleIDList: new FormControl([]),
      newOrgID: new FormControl(''),
      newOrgName: new FormControl({ value: '', disabled: true }),
      resetBlock: new FormControl(false),
      resetPwdFailTimes: new FormControl(false)
    });
    const codes = ['user_id', 'user_name', 'user_alias', 'org_name', 'role_alias', 'status'];
    this.translate.get(codes).subscribe(i18n => {
      this.i18n = i18n;
      this.cols = [
        { field: 'userID', header: i18n.user_id },
        { field: 'userName', header: i18n.user_name },
        { field: 'userAlias', header: i18n.user_alias },
        { field: 'orgName', header: i18n.org_name },
        { field: 'roleAlias', header: i18n.role_alias },
        { field: 'statusName', header: i18n.status }
      ];
    });
  }

  ngOnInit() {
    this.roleService.queryRTMapByUk({ txIdList: ['AC0001', 'AC0004', 'AC0005'] } as DPB0115Req).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.canCreate = res.RespBody.dataList.find(item => item.txId === 'AC0001') ? res.RespBody.dataList.find(item => item.txId === 'AC0001')!.available : false;
        this.canUpdate = res.RespBody.dataList.find(item => item.txId === 'AC0004') ? res.RespBody.dataList.find(item => item.txId === 'AC0004')!.available : false;
        this.canDelete = res.RespBody.dataList.find(item => item.txId === 'AC0005') ? res.RespBody.dataList.find(item => item.txId === 'AC0005')!.available : false;
      }
    });
    // status
    let ReqBody = {
      encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('ENABLE_FLAG')) + ',' + 9,
      isDefault: 'N'
    } as DPB0047Req;
    this.listService.querySubItemsByItemNo(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        let _createStatus: { label: string, value: string }[] = [];
        let _updateStatus: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems!) {
            if (item.subitemNo != '-1') {
              _updateStatus.push({ label: item.subitemName, value: item.param1! });
              if (item.subitemNo != '2') {
                _createStatus.push({ label: item.subitemName, value: item.param1! });
              }
            }
          }
        }
        this.createStatus = _createStatus;
        this.updateStatus = _updateStatus;
      }
    });
    this.getOrgList();
    this.userInfoList = [];
    this.rowcount = this.userInfoList.length;
    //預埋api
    let queryReqBody = {
      keyword: this.keyword!.value,
      roleName: this.roleName!.value,
      orgName: this.orgName!.value
    } as AA0019Req;
    this.userService.queryTUserList_v3_ignore1298(queryReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.userInfoList = res.RespBody.userInfoList;
        this.rowcount = this.userInfoList.length;
      }
    });
  }

  queryRoleList() {
    this.closable = false;
    const code = ['role_list'];
    this.translate.get(code).subscribe(dict => {
      this.dialogTitle = dict['role_list'];
      // let data: FormParams = {
      //   displayInDialog: true,
      //   data: { selectionMode: 'single' },
      //   afterCloseCallback: (res) => {
      //     if (res) {
      //       this.roleAlias!.setValue(res.roleAlias);
      //       this.roleName!.setValue(res.roleName);
      //     }
      //     else {
      //       this.roleAlias!.setValue('');
      //       this.roleName!.setValue('');
      //     }
      //   }
      // }
      // this._dialog.open(RoleListLovComponent, data);

      const ref = this.dialogService.open(RoleListLovComponent, {
        data: { selectionMode: 'single' },
        header: dict['role_list'],
        width: '700px'
      })

      ref.onClose.subscribe(res => {
        if (res) {
          this.roleAlias!.setValue(res.roleAlias);
          this.roleName!.setValue(res.roleName);
        }
        else {
          this.roleAlias!.setValue('');
          this.roleName!.setValue('');
        }

      });

    });
  }

  queryRoleMappingList() {
    this.closable = true;
    const code = ['role_list'];
    this.translate.get(code).subscribe(dict => {
      this.dialogTitle = dict['role_list'];
      // let data: FormParams = {
      //   displayInDialog: true,
      //   afterCloseCallback: (res) => {
      //     if (res) {
      //       let set = new Set();
      //       let _roleAliasList: string[] = [];
      //       let _roleIDList: string[] = [];
      //       switch (this.currentAction) {
      //         case 'create':
      //           this.roleIDList!.value.map(roleId => {
      //             set.add(roleId);
      //             _roleIDList.push(roleId);
      //           });
      //           this.roleAliasList.map(roleAlias => {
      //             _roleAliasList.push(roleAlias);
      //           });
      //           res.map(item => {
      //             if (!set.has(item.roleId)) {
      //               _roleAliasList.push(item.roleAlias);
      //               _roleIDList.push(item.roleId);
      //             }
      //           });
      //           this.roleAliasList = _roleAliasList;
      //           this.roleIDList!.setValue(_roleIDList);
      //           break;
      //         case 'update':
      //           this.newRoleIDList!.value.map(roleId => {
      //             set.add(roleId);
      //             _roleIDList.push(roleId);
      //           });
      //           this.newRoleAliasList.map(roleAlias => {
      //             _roleAliasList.push(roleAlias);
      //           });
      //           res.map(item => {
      //             if (!set.has(item.roleId)) {
      //               _roleAliasList.push(item.roleAlias);
      //               _roleIDList.push(item.roleId);
      //             }
      //           });
      //           this.newRoleAliasList = _roleAliasList;
      //           this.newRoleIDList!.setValue(_roleIDList);
      //           break;
      //       }
      //     }
      //   }
      // }
      // this._dialog.open(RoleMappingListLovComponent, data);

      const ref = this.dialogService.open(RoleMappingListLovComponent, {
        // data: { data: data },
        header: dict['role_list'],
        width:'700px'
      })

      ref.onClose.subscribe(res => {
        if (res) {
          let set = new Set();
          let _roleAliasList: string[] = [];
          let _roleIDList: string[] = [];

          switch (this.currentAction) {
            case 'create':
              this.roleIDList!.value.map(roleId => {
                set.add(roleId);
                _roleIDList.push(roleId);
              });
              this.roleAliasList.map(roleAlias => {
                _roleAliasList.push(roleAlias);
              });
              res.map(item => {
                if (!set.has(item.roleId)) {
                  _roleAliasList.push(item.roleAlias);
                  _roleIDList.push(item.roleId);
                }
              });
              this.roleAliasList = _roleAliasList;
              this.roleIDList!.setValue(_roleIDList);
              break;
            case 'update':
              this.newRoleIDList!.value.map(roleId => {
                set.add(roleId);
                _roleIDList.push(roleId);
              });
              this.newRoleAliasList.map(roleAlias => {
                _roleAliasList.push(roleAlias);
              });
              res.map(item => {
                if (!set.has(item.roleId)) {
                  _roleAliasList.push(item.roleAlias);
                  _roleIDList.push(item.roleId);
                }
              });
              this.newRoleAliasList = _roleAliasList;
              this.newRoleIDList!.setValue(_roleIDList);
              break;
          }

        }
      });

    });
  }

  getOrgList() {
    this.orgService.queryTOrgList({}).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.orgList = res.RespBody.orgList;
      }
    });
  }

  submitForm() {
    this.userInfoList = [];
    this.rowcount = this.userInfoList.length;
    //預埋api
    let ReqBody = {
      keyword: this.keyword!.value,
      roleName: this.roleName!.value,
      orgName: this.orgName!.value
    } as AA0019Req;
    this.userService.queryTUserList_v3(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.userInfoList = res.RespBody.userInfoList;
        this.rowcount = this.userInfoList.length;
      }
    });
  }

  moreData() {
    let ReqBody = {
      userId: this.userInfoList[this.userInfoList.length - 1].userID,
      keyword: this.keyword!.value,
      roleName: this.roleName!.value,
      orgName: this.orgName!.value
    } as AA0019Req;
    this.userService.queryTUserList_v3(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.userInfoList = this.userInfoList.concat(res.RespBody.userInfoList);
        this.rowcount = this.userInfoList.length;
      }
    });
  }

  getOrgNode(node: AA1002List) {
    switch (this.currentAction) {
      case 'query':
      case 'create':
        this.orgID!.setValue(node.orgID);
        this.orgName!.setValue(node.orgName);
        break;
      case 'update':
        this.newOrgID!.setValue(node.orgID);
        this.newOrgName!.setValue(node.orgName);
        break;
    }
  }

  cancelOrg() {
    switch (this.currentAction) {
      case 'query':
      case 'create':
        this.orgID!.setValue('');
        this.orgName!.setValue('');
        break;
      case 'update':
        this.newOrgID!.setValue('');
        this.newOrgName!.setValue('');
        break;
    }
    this.displayOrgChart = false;
  }

  createUser() {
    let createReqBody = {
      userName: this.userName!.value,
      userAlias: this.userAlias!.value,
      userBlock: this.toolService.Base64Encoder(this.userBlock!.value),
      userMail: this.userMail!.value,
      roleIDList: this.roleIDList!.value,
      orgID: this.orgID!.value,
      encodeStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.encodeStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.encodeStatus!.value)
    } as AA0001Req;
    // console.log('create :', createReqBody)
    this.userService.addTUser(createReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['dialog.create', 'user', 'message.create', 'message.user', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.user']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
        this.form.reset('');
        this.submitForm();
        this.changePage('query');
      }
    });
  }

  updateUser() {
    let ReqBody = {
      userID: this.userDetail!.userID,
      userName: this.userDetail!.userName,
      newUserName: this.newUserName!.value,
      userAlias: this.userDetail!.userAlias,
      newUserAlias: this.newUserAlias!.value,
      userMail: this.userDetail!.userMail,
      newUserMail: this.newUserMail!.value,
      status: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.userDetail!.status)) + ',' + this.convertEncodeStatusIndex(this.userDetail!.status),
      newStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.newStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.newStatus!.value),
      roleIDList: this.userDetail!.roleID,
      newRoleIDList: this.newRoleIDList!.value,
      orgID: this.userDetail!.orgId,
      newOrgID: this.newOrgID!.value,
      resetPwdFailTimes: this.resetPwdFailTimes!.value,
      resetBlock: this.resetBlock!.value
    } as AA0004Req;
    this.userService.updateTUserState(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['dialog.update', 'message.success', 'message.update', 'message.user', 'plz_login_again'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} ${dict['message.user']}`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });
        if (this.userDetail!.userID == this.toolService.getUserID() && ReqBody.newUserName != this.userDetail!.userName) {
          this.alert.logout(`${dict['message.update']} ${dict['message.success']}!`, `${dict['plz_login_again']}!`);
          window.setTimeout(() => {
            this.toolService.removeAll();
            // this.router.navigate(['/login']);
            this.logoutService.logout();
          }, 3000);
        }
        else {
          this.form.reset();
          this.submitForm();
          this.changePage('query');
        }
      }
    });
  }

  async deleteUser() {
    // const code = ['cfm_del_user', 'user_name', 'user_alias'];
    // const dict = await this.toolService.getDict(code);
    // this.messageService.add({ key: 'deleteUser', sticky: true, severity: 'error', summary: dict['cfm_del_user'], detail: `${dict['user_name']}：${this.userDetail!.userName}、${dict['user_alias']}：${this.userDetail!.userAlias}` });

    const code = ['cfm_del', 'user_name', 'user_alias'];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: dict['cfm_del'],
      message: `${dict['user_name']}：${this.userDetail!.userName}、${dict['user_alias']}：${this.userDetail!.userAlias}`,
      accept: () => {
          this.onDeleteUserConfirm();
      }
    });
  }

  onDeleteUserConfirm() {
    this.messageService.clear();
    let ReqBody = {
      userID: this.userDetail!.userID,
      userName: this.userDetail!.userName
    } as AA0005Req;
    this.userService.deleteTUser(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'message.user', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.delete']} ${dict['message.user']}`,
          detail: `${dict['message.delete']} ${dict['message.success']}!`
        });
        this.submitForm();
        this.changePage('query');
      }
    });
  }

  onReject() {
    this.messageService.clear();
  }

  deleteRole(idx: number) {
    switch (this.currentAction) {
      case 'create':
        this.roleAliasList.splice(idx, 1);
        this.roleIDList!.value.splice(idx, 1);
        break;
      case 'update':
        this.newRoleIDList!.value.splice(idx, 1);
        this.newRoleAliasList.splice(idx, 1);
        break;
    }
  }

  convertEncodeStatusIndex(encodeStatus: string): number {
    switch (encodeStatus) {
      case '1':
        return 0;
      case '2':
        return 1;
      case '3':
        return 3;
      default:
        return -1;
    }
  }

  async changePage(action: string, rowData?: AA0019List) {
    const code = ['button.detail', 'button.delete', 'button.create', 'button.update'];
    const dict = await this.toolService.getDict(code);
    this.currentAction = action;
    this.resetFormValidator(this.form);
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'detail':
        let detailReqBody = {
          userID: rowData!.userID,
          userName: rowData!.userName
        } as AA0003Req;
        this.userService.queryTUserDetail(detailReqBody).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.userDetail = res.RespBody;
            this.currentTitle = `${this.title} > ${dict['button.detail']}`;
            this.pageNum = 2;
          }
        });
        break;
      case 'delete':
        let deleteReqBody = {
          userID: rowData!.userID,
          userName: rowData!.userName
        } as AA0003Req;
        this.userService.queryTUserDetail(deleteReqBody).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.userDetail = res.RespBody;
            this.currentTitle = `${this.title} > ${dict['button.delete']}`;
            this.pageNum = 2;
          }
        });
        break;
      case 'create':

        this.userService.addTUser_before().subscribe(res => {
          this.addFormValidator(this.form, res.RespBody.constraints);

        // $(`#userName_label`).addClass('required');
        // this.userName.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.userNameLimitChar.value), ValidatorFns.stringCharValidator()]);
        // $(`#userAlias_label`).addClass('required');
        // this.userAlias.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.userAliasLimitChar.value)]);
        this.userBlock!.setValidators([ValidatorFns.confirmPasswordForUserValidator(this.form), ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.userBlockLimitChar.value)]);
        this.confirmUserBlock!.setValidators([ValidatorFns.confirmPasswordForUserValidator(this.form), ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.userBlockLimitChar.value)]);
        // $(`#userMail_label`).addClass('required');
        // this.userMail.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.userMailLimitChar.value), ValidatorFns.mailValidator()]);
        this.roleIDList!.setValue([]);
        this.roleAliasList = [];
        // $(`#orgName_label`).addClass('required');
        // this.orgID.setValidators(ValidatorFns.requiredValidator());
        // $(`#encodeStatus_label`).addClass('required');
        // this.encodeStatus.setValidators(ValidatorFns.requiredValidator());
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 3;
        });
        break;
      case 'update':
        let updateReqBody = {
          userID: rowData!.userID,
          userName: rowData!.userName
        } as AA0003Req;
        this.userService.queryTUserDetail(updateReqBody).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.userService.updateTUserState_before().subscribe(resbefore => {
              this.addFormValidator(this.form, resbefore.RespBody.constraints);
              this.userDetail = res.RespBody;
              const _userDetail = JSON.parse(JSON.stringify(this.userDetail));
              this.currentTitle = `${this.title} > ${dict['button.update']}`;
              this.pageNum = 4;
              this.newUserName!.setValue(_userDetail.userName);
              this.newUserAlias!.setValue(_userDetail.userAlias);
              this.newUserMail!.setValue(_userDetail.userMail);
              this.newOrgID!.setValue(_userDetail.orgId);
              this.newOrgName!.setValue(_userDetail.orgName);
              this.newStatus!.setValue(_userDetail.status);
              this.resetBlock!.setValue(false);
              this.resetPwdFailTimes!.setValue(false);
              this.newRoleIDList!.setValue(_userDetail.roleID);
              this.newRoleAliasList = _userDetail.roleAlias;


            });
          }
        });
        // $(`#newUserName_label`).addClass('required');
        // this.newUserName.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.userNameLimitChar.value), ValidatorFns.stringCharValidator()]);
        // $(`#newUserMail_label`).addClass('required');
        // this.newUserMail.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.userMailLimitChar.value), ValidatorFns.mailValidator()]);
        // this.newUserAlias.setValidators([ValidatorFns.maxLengthValidator(this.userAliasLimitChar.value), ValidatorFns.stringAliasValidator(this.newUserAliasLimitChar.value)]);
        // $(`#newRoleIDList_label`).addClass('required');
        // this.newRoleIDList.setValidators(ValidatorFns.requiredValidator());
        // $(`#newOrgName_label`).addClass('required');
        // this.newOrgID.setValidators(ValidatorFns.requiredValidator());


        break;
    }
  }

  headerReturn(){
    this.changePage('query');
  }

  async  openOrgDialog(){
    const codes = ['org_chart'];
    const dict = await this.toolService.getDict(codes);


    // let orgName = "";
    // switch (this.currentAction) {
    //   case 'query':
    //   case 'create':
    //     orgName = this.orgName?.value;
    //     break;
    //   case 'update':
    //     orgName = this.newOrgName?.value;
    //     break;
    // }

    const refDialog = this.dialogService.open(OrganizationComponent, {
        header: dict['org_chart'],
        modal:true,
        data: {
            orgList: this.orgList,
            showFooterBtn:true,
            // orgName:orgName
          },
          width: '90vw',
          height: '100vh'
      })


      refDialog.onClose.subscribe(res => {
        if (res) {
         switch (this.currentAction) {
          case 'query':
          case 'create':
            this.orgID!.setValue(res.data.orgID);
            this.orgName!.setValue(res.data.orgName);
            break;
          case 'update':
            this.newOrgID!.setValue(res.data.orgID);
            this.newOrgName!.setValue(res.data.orgName);
            break;
        }

        }
        else {
          switch (this.currentAction) {
            case 'query':
            case 'create':
              this.orgID!.setValue('');
              this.orgName!.setValue('');
              break;
            case 'update':
              // this.newOrgID!.setValue('');
              // this.newOrgName!.setValue('');
              break;
          }
        }

      });

}

  public get keyword() { return this.form.get('keyword'); };
  public get roleName() { return this.form.get('roleName'); };
  public get roleAlias() { return this.form.get('roleAlias'); };
  public get orgName() { return this.form.get('orgName'); };
  public get userName() { return this.form.get('userName'); };
  public get userAlias() { return this.form.get('userAlias'); };
  public get userBlock() { return this.form.get('userBlock'); };
  public get confirmUserBlock() { return this.form.get('confirmUserBlock'); };
  public get userMail() { return this.form.get('userMail'); };
  public get encodeStatus() { return this.form.get('encodeStatus'); };
  public get orgID() { return this.form.get('orgID'); };
  public get roleIDList() { return this.form.get('roleIDList'); };
  public get newUserName() { return this.form.get('newUserName'); };
  public get newUserAlias() { return this.form.get('newUserAlias'); };
  public get newUserMail() { return this.form.get('newUserMail'); };
  public get newOrgName() { return this.form.get('newOrgName'); };
  public get newOrgID() { return this.form.get('newOrgID'); };
  public get newStatus() { return this.form.get('newStatus'); };
  public get newRoleIDList() { return this.form.get('newRoleIDList'); };
  public get resetBlock() { return this.form.get('resetBlock'); };
  public get resetPwdFailTimes() { return this.form.get('resetPwdFailTimes'); };

}
