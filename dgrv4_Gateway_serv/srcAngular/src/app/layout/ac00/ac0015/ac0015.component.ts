import { DialogService } from 'primeng/dynamicdialog';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormOperate } from 'src/app/models/common.enum';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { AA0017Req } from 'src/app/models/api/RoleService/aa0017.interface';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { AA0022Req, AA0022List } from 'src/app/models/api/RoleService/aa0022.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { AA0021Req } from 'src/app/models/api/RoleService/aa0021.interface';
import { AA0016Req } from 'src/app/models/api/RoleService/aa0016.interface';
import { AA0018Req } from 'src/app/models/api/RoleService/aa0018.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { RoleListLovComponent } from 'src/app/shared/role-list-lov/role-list-lov.component';

@Component({
  selector: 'app-ac0015',
  templateUrl: './ac0015.component.html',
  styleUrls: ['./ac0015.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class Ac0015Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;

  cols: { field: string; header: string }[] = [];
  dialogTitle: string = '';
  formOperate = FormOperate;
  deleteData?: AA0022List;
  form: FormGroup;
  roleRoleMappingList: Array<AA0022List> = new Array<AA0022List>();
  rowcount: number = 0;
  pageNum: number = 1; // 1: 查詢，2: 建立、更新
  currentTitle: string = this.title;
  currentAction: string = 'query';
  btnName: string = '';
  currentRoleName: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private translate: TranslateService,
    private tool: ToolService,
    private message: MessageService,
    private fb: FormBuilder,
    private roleService: RoleService,
    private dialogService: DialogService,
    private confirmationService: ConfirmationService,
  ) {
    super(route, tr);

    this.form = this.fb.group({
      keyword: new FormControl(''),
      roleName: new FormControl('', ValidatorFns.requiredValidator()),
      roleAlias: new FormControl({ value: '', disabled: true }),
      roleAliasMapping: new FormControl([]),
      roleNameMapping: new FormControl([], ValidatorFns.requiredValidator())
    });
  }

  async ngOnInit() {

    const codes = ['login_role', 'role_alias_mapping'];
    const dict = await this.tool.getDict(codes);
    this.cols = [
      { field: 'roleAlias', header: dict['login_role'] },
      { field: 'roleRoleMappingInfo', header: dict['role_alias_mapping'] }
    ];
    this.roleRoleMappingList = [];
    this.rowcount = this.roleRoleMappingList.length;
    let ReqBody = {
      keyword: this.form.get('keyword')!.value
    } as AA0022Req;
    this.roleService.queryTRoleRoleMap_ignore1298(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleRoleMappingList = res.RespBody.roleRoleMappingList;
        this.rowcount = this.roleRoleMappingList.length;
      }
    });
  }

  submitForm() {
    this.roleRoleMappingList = [];
    this.rowcount = this.roleRoleMappingList.length;
    let ReqBody = {
      keyword: this.form.get('keyword')!.value
    } as AA0022Req;
    this.roleService.queryTRoleRoleMap(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleRoleMappingList = res.RespBody.roleRoleMappingList;
        this.rowcount = this.roleRoleMappingList.length;
      }
    });
  }

  moreData() {
    let ReqBody = {
      roleId: this.roleRoleMappingList[this.roleRoleMappingList.length - 1].roleId,
      keyword: this.form.get('keyword')!.value
    } as AA0022Req;
    this.roleService.queryTRoleRoleMap(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleRoleMappingList = this.roleRoleMappingList.concat(res.RespBody.roleRoleMappingList);
        this.rowcount = this.roleRoleMappingList.length;
      }
    });
  }

  showDialog(rowData: AA0022List, operation: FormOperate) {
    const codes = ['cfm_del','dialog.update', 'message.success', 'message.update', 'message.role_role_mapping', 'cfm_del_role_role_mapping'];
    this.translate.get(codes).pipe(
      switchMap(dict => this.openDialog$(rowData, operation, dict))
    ).subscribe();
  }

  openDialog$(rowData: AA0022List, operation: FormOperate, dict: any): Observable<boolean> {
    return Observable.create(obser => {
      switch (operation) {
        case FormOperate.delete:
          this.message.clear();
          this.deleteData = rowData;
          // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_role_role_mapping'] });
          // obser.next(true);



    this.confirmationService.confirm({
      header: ' ',
      message: dict['cfm_del_role_role_mapping'],
      accept: () => {
          this.onConfirm();
      }
    });
          break;
      }
    });
  }

  async createOrUpdate() {
    const code = ['message.create', 'message.update', 'message.success', 'message.role_role_mapping'];
    const dict = await this.tool.getDict(code);
    switch (this.currentAction) {
      case 'create':
        let createReqBody = {
          roleName: this.roleName!.value,
          roleNameMapping: this.roleNameMapping!.value
        } as AA0016Req;
        this.roleService.addTRoleRoleMap(createReqBody).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.message.add({
              severity: 'success', summary: `${dict['message.create']} ${dict['message.role_role_mapping']}`,
              detail: `${dict['message.create']} ${dict['message.success']}!`
            });
            this.submitForm();
            this.changePage('query');
          }
        });
        break;
      case 'update':
        let updateReqBody = {
          roleName: this.roleName!.value,
          roleNameMapping: this.roleNameMapping!.value
        } as AA0018Req;
        this.roleService.updateTRoleRoleMap(updateReqBody).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.message.add({
              severity: 'success', summary: `${dict['message.update']} ${dict['message.role_role_mapping']}`,
              detail: `${dict['message.update']} ${dict['message.success']}!`
            });
            this.submitForm();
            this.changePage('query');
          }
        });
        break;
    }
  }

  deleteRole(idx: number) {
    this.roleAliasMapping!.value.splice(idx, 1);
    this.roleNameMapping!.value.splice(idx, 1);
  }

  onConfirm() {
    this.message.clear();
    let roleNameMapping: string[] = [];
    this.deleteData!.roleRoleMapping.map(item => {
      roleNameMapping.push(item.roleName);
    });
    let ReqBody = {
      roleName: this.deleteData!.roleName,
    } as AA0017Req;
    // console.log('req 0017 :', req_0017)
    this.roleService.deleteTRoleRoleMap(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'message.role_role_mapping', 'message.success'];
        const dict = await this.tool.getDict(code);
        this.message.add({
          severity: 'success', summary: `${dict['message.delete']} ${dict['message.role_role_mapping']}`,
          detail: `${dict['message.delete']} ${dict['message.success']}!`
        });
        this.submitForm();
      }
    });
  }

  onReject() {
    this.message.clear();
  }

  async changePage(action: string, rowData?: AA0022List) {
    this.currentAction = action;
    const code = ['button.create', 'button.update'];
    const dict = await this.tool.getDict(code);
    this.resetFormValidator(this.form);
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];
        this.roleName!.setValue('');
        this.roleAlias!.setValue('');
        this.roleNameMapping!.setValue([]);
        this.roleAliasMapping!.setValue([]);
        break;
      case 'update':
        this.currentRoleName = rowData!.roleName;
        let ReqBody = {
          roleName: this.currentRoleName
        } as AA0021Req;
        this.roleService.queryTRoleRoleMapDetail(ReqBody).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.currentTitle = `${this.title} > ${dict['button.update']}`;
            this.pageNum = 2;
            this.btnName = dict['button.update'];
            this.roleName!.setValue(res.RespBody.roleName);
            this.roleAlias!.setValue(res.RespBody.roleAlias);
            let _roleNameMapping: string[] = [];
            let _roleAliasMapping: string[] = [];
            res.RespBody.roleMappingInfo.map(item => {
              _roleNameMapping.push(item.roleName);
              _roleAliasMapping.push(item.roleAliasRoleName);
            });
            this.roleNameMapping!.setValue(_roleNameMapping);
            this.roleAliasMapping!.setValue(_roleAliasMapping);
          }
        });
        break;
    }
  }

  queryRoleList(action: string) {
    const code = ['role_list'];
    this.translate.get(code).subscribe(dict => {
      this.dialogTitle = dict['role_list'];
      // let data: FormParams = {
      //     operate: this.currentAction == 'create' ? FormOperate.create : FormOperate.update,
      //     displayInDialog: true,
      //     afterCloseCallback: (res) => {
      //         if (res) {
      //             switch (action) {
      //                 case 'role':
      //                     this.roleAlias!.setValue(res.roleAlias);
      //                     this.roleName!.setValue(res.roleName);
      //                     break;
      //                 case 'mappingRole':
      //                     let set = new Set();
      //                     let _roleAlias:string[] = [];
      //                     let _roleName:string[] = [];
      //                     this.roleNameMapping!.value.map(roleName => {
      //                         set.add(roleName);
      //                         _roleName.push(roleName);
      //                     });
      //                     this.roleAliasMapping!.value.map(roleAlias => {
      //                         _roleAlias.push(roleAlias);
      //                     });
      //                     res.map(item => {
      //                         if (!set.has(item.roleName)) {
      //                             _roleAlias.push(item.roleAlias);
      //                             _roleName.push(item.roleName);
      //                         }
      //                     });
      //                     this.roleAliasMapping!.setValue(_roleAlias);
      //                     this.roleNameMapping!.setValue(_roleName);
      //                     break;
      //             }
      //         }
      //     }
      // }
      // switch (action) {
      //     case 'role':
      //         data.data = { selectionMode: 'single', roleName: this.currentAction == 'create' ? '' : this.currentRoleName };
      //         break;
      //     case 'mappingRole':
      //         data.data = { selectionMode: 'multiple', roleName: this.currentAction == 'create' ? '' : this.currentRoleName };
      //         break;
      // }
      // this._dialog.open(RoleListLovComponent, data);
      let data = {};
      switch (action) {
        case 'role':
          data = { selectionMode: 'single', roleName: this.currentAction == 'create' ? '' : this.currentRoleName };
          break;
        case 'mappingRole':
          data = { selectionMode: 'multiple', roleName: this.currentAction == 'create' ? '' : this.currentRoleName };
          break;
      }

      const ref = this.dialogService.open(RoleListLovComponent, {
        data: data,
        header: dict['role_list'],
        width: '700px'
      });

      ref.onClose.subscribe(res => {
        if (res) {
          switch (action) {
            case 'role':
              this.roleAlias!.setValue(res.roleAlias);
              this.roleName!.setValue(res.roleName);
              break;
            case 'mappingRole':
              let set = new Set();
              let _roleAlias: string[] = [];
              let _roleName: string[] = [];
              this.roleNameMapping!.value.map(roleName => {
                set.add(roleName);
                _roleName.push(roleName);
              });
              this.roleAliasMapping!.value.map(roleAlias => {
                _roleAlias.push(roleAlias);
              });
              res.map(item => {
                if (!set.has(item.roleName)) {
                  _roleAlias.push(item.roleAlias);
                  _roleName.push(item.roleName);
                }
              });
              this.roleAliasMapping!.setValue(_roleAlias);
              this.roleNameMapping!.setValue(_roleName);
              break;
          }
        }

      });

    });
  }

  async copyInfoMsg(infoMsg: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.tool.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = infoMsg;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.message.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  switchOri(rowData:any){
    if(rowData.t){
      rowData.t  =  !rowData.t;
    }
    else rowData.t = true;
  }

  public get keyword() { return this.form.get('keyword'); };
  public get roleName() { return this.form.get('roleName'); };
  public get roleAlias() { return this.form.get('roleAlias'); };
  public get roleAliasMapping() { return this.form.get('roleAliasMapping'); };
  public get roleNameMapping() { return this.form.get('roleNameMapping'); };

}
