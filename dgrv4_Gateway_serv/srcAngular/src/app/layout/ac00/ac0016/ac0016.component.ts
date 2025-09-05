import { map } from 'rxjs/operators';
import { DPB0163Req } from './../../../models/api/ServerService/dpb0163.interface';
import { DPB0148Req } from './../../../models/api/ServerService/dpb0148.interface';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0147Req } from './../../../models/api/ServerService/dpb0147.interface';
import { DPB0145Resp, DPB0145RespItem } from './../../../models/api/ServerService/dpb0145.interface';
import { DPB0146Req, DPB0146Resp } from './../../../models/api/ServerService/dpb0146.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { TOrgService } from 'src/app/shared/services/org.service';
import { AA1002List } from 'src/app/models/api/OrgService/aa1002.interface';
import { OrganizationComponent } from 'src/app/shared/organization/organization.component';
import { RoleMappingListLovComponent } from 'src/app/shared/role-mapping-list-lov/role-mapping-list-lov.component';
import { TranslateService } from '@ngx-translate/core';
import { DialogService } from 'primeng/dynamicdialog';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-ac0016',
  templateUrl: './ac0016.component.html',
  styleUrls: ['./ac0016.component.css'],
  providers: [ConfirmationService]
})
export class Ac0016Component extends BaseComponent implements OnInit {

  currentTitle: string = this.title;
  pageNum: number = 1;
  cols: { field: string; header: string }[] = [];
  tableData: Array<any> = new Array();
  form!: FormGroup;
  formU!: FormGroup;
  orgList: AA1002List[] = [];
  userInfo?:DPB0145RespItem;
  currentAction:string = 'create';

  idpTypeList:{ label: string; value: string; }[] = [
    {label:'GOOGLE', value:'GOOGLE'},
    {label:'LDAP', value:'LDAP'},
    {label:'MLDAP', value:'MLDAP'},
    {label:'MS', value:'MS'},
    {label:'API', value:'API'},
    {label:'CUS', value:'CUS'},
    {label:'OIDC', value:'OIDC'},
  ];

  idpStatusList:{ label: string; value: string; }[] = [
    {label:'Request', value:'1'},
    {label:'Allow', value:'2'},
    {label:'Deny', value:'3'},
  ];



  selectItem: DPB0146Resp | undefined;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private toolService: ToolService,
    private fb: FormBuilder,
    private dialogService: DialogService,
    private translate: TranslateService,
    private orgService: TOrgService,
    private serverService: ServerService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
  ) {
    super(route, tr)
  }

  async ngOnInit() {

    // const code = ['key', 'value', 'memo'];
    // const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'id', header: 'ID' },
      { field: 'userName', header: 'User Name' },
      { field: 'userAlias', header: 'User Alias' },
      { field: 'orgName', header: 'Organization' },
      { field: 'roleAlias', header: 'Role' },
      { field: 'statusName', header: 'Status' },
      { field: 'idpType', header: 'Type' },
    ];

    this.form = this.fb.group({
      userName: new FormControl(''),
      userAlias: new FormControl(''),
      status: new FormControl(''),
      userEmail: new FormControl(''),
      idpType: new FormControl(''),

      longId: new FormControl(),
      roleIdList: new FormControl([]),
      // newRoleIdList: new FormControl([]),
      roleAliasList: new FormControl([]),//呈現用
      orgId: new FormControl(),
      orgName: new FormControl(),
      // newOrgId: new FormControl(),
      // newOrgName: new FormControl(),//呈現用
    })

    this.formU = this.fb.group({
      longId: new FormControl(''),
      userName: new FormControl(''),
      newUserName: new FormControl(''),
      newUserAlias: new FormControl(''),
      newStatus: new FormControl(''),
      newUserEmail: new FormControl(''),
      newIdpType: new FormControl(''),

      roleIdList: new FormControl([]),
      newRoleIdList: new FormControl([]),
      newRoleAliasList: new FormControl([]),//呈現用
      orgId: new FormControl(),
      newOrgId: new FormControl(),
      newOrgName: new FormControl(),//呈現用
    })

    this.getOrgList();

    this.queryAll();

  }

  queryAll() {
    this.serverService.QueryALL().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        // console.log(res.RespBody.dataList)
        this.tableData = res.RespBody.dataList;
      }else this.tableData = [];
    })
  }

  async changePage(actionType: string, rowData?: DPB0145RespItem) {

    this.userInfo = rowData;
    this.currentAction = actionType;

    switch (actionType) {
      case 'create':
        this.resetFormValidator(this.form);
        this.serverService.createIdPUser_before().subscribe(async res => {
          if(this.toolService.checkDpSuccess(res.ResHeader)){
            this.pageNum = 2;
            const code = ['button.create'];
            const dict = await this.toolService.getDict(code);
            this.currentTitle = `${this.title} > ${dict['button.create']}`;

            this.addFormValidator(this.form,res.RespBody.constraints)
            // this.newRoleIdList.setValue([]);
          }

        })
        break;
      case 'update':
        this.resetFormValidator(this.formU);
        let reqBody = {
          longId: rowData?.longId
        } as DPB0146Req;
        this.serverService.QueryDetail(reqBody).subscribe(async res => {
          this.pageNum = 3;
          const code = ['button.update'];
          const dict = await this.toolService.getDict(code);
          this.currentTitle = `${this.title} > ${dict['button.update']}`;

          this.longId_u.setValue(res.RespBody.longId);
          this.userName_u.setValue(res.RespBody.userName);
          this.newUserName_u.setValue(res.RespBody.userName);

          this.newUserAlias_u.setValue(res.RespBody.userAlias);
          this.newStatus_u.setValue(res.RespBody.status);
          this.newUserEmail_u.setValue(res.RespBody.userEmail);
          this.newIdpType_u.setValue(res.RespBody.idpType)

          this.roleIdList_u.setValue(res.RespBody.roleId);
          this.newRoleIdList_u.setValue(res.RespBody.roleId);
          this.newRoleAliasList_u.setValue(res.RespBody.roleAlias);

          this.orgId_u.setValue(res.RespBody.orgId);
          this.newOrgId_u.setValue(res.RespBody.orgId);
          this.newOrgName_u.setValue(res.RespBody.orgName);

          this.selectItem = res.RespBody;
          this.serverService.UpdateOne_Role_Org_before()
          .subscribe(res => {
            let constraints = res.RespBody.constraints;
            // let newTmp = constraints.filter(item=>{
            //   if(item.field == 'newRoleIdList') return false;
            //   return true;
            // })
            // console.log(newTmp)
            this.addFormValidator(this.formU, constraints)

          })

        })

        break
      case 'query':
        this.headerReturn();
        break;
      case 'delete':
        const code = ['cfm_del'];
        const dict = await this.toolService.getDict(code);
        // console.log('dict', dict)
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.onConfirm(rowData);
          }
        });

        break;
    }
  }

  onConfirm(rowData?: DPB0145RespItem) {
    let ReqBody = {
      longId: rowData?.longId,
    } as DPB0148Req;
    this.serverService.DeleteOne(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'message.user', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.delete']} ${dict['message.user']}`,
          detail: `${dict['message.delete']} ${dict['message.success']}!`
        });

        this.queryAll();
      }
    })

  }

  headerReturn() {
    this.pageNum = 1;
    this.currentTitle = this.title;
  }

  updateData() {
    // console.log(this.selectItem)
    let reqBody = {
      longId: this.longId_u.value,
      userName: this.userName_u.value,
      newUserName: this.newUserName_u.value,
      newUserAlias: this.newUserAlias_u.value,
      newStatus: this.newStatus_u.value,
      newUserEmail: this.newUserEmail_u.value,
      newIdpType: this.newIdpType_u.value,
      roleIdList: this.roleIdList_u.value,
      newRoleIdList: this.newRoleIdList_u.value,
      orgId: this.orgId_u.value,
      newOrgId: this.newOrgId_u.value,
      idpType: this.selectItem?.idpType,
    } as DPB0147Req;
    // console.log(reqBody);
    // return;
    this.serverService.UpdateOne_Role_Org(reqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {

        const code = ['dialog.update', 'message.success', 'message.update', 'message.user'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} ${dict['message.user']}`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.pageNum = 1;
        this.currentTitle = this.title;
        this.queryAll();
      }
    })

  }

  queryRoleMappingList() {

    const code = ['role_list'];
    this.translate.get(code).subscribe(dict => {

      const ref = this.dialogService.open(RoleMappingListLovComponent, {
        header: dict['role_list'],
        width: '700px'
      })

      ref.onClose.subscribe(res => {
        if (res) {

          let tmpRoleIdList = this.currentAction == 'update' ? [...this.newRoleIdList_u.value]:[...this.roleIdList.value];
          let tmpRoleAliasList = this.currentAction == 'update' ?[...this.newRoleAliasList_u.value] : [...this.roleAliasList.value];

          let set = new Set();

          if(this.currentAction == 'update'){
            if(this.newRoleIdList_u.value) this.newRoleIdList_u.value.forEach((item: string) => {
              set.add(item)
            });

            res.forEach(item => {
              // console.log('item', item)
              if (!set.has(item.roleId)) {
                tmpRoleIdList.push(item.roleId)
                tmpRoleAliasList.push(item.roleAlias)
              }
            });
            // console.log(this.selectItem)
            this.newRoleIdList_u.setValue(tmpRoleIdList);
            this.newRoleAliasList_u.setValue(tmpRoleAliasList);
          }
          else{
            if(this.roleIdList.value) this.roleIdList.value.forEach((item: string) => {
              set.add(item)
            });

            res.forEach(item => {
              // console.log('item', item)
              if (!set.has(item.roleId)) {
                tmpRoleIdList.push(item.roleId)
                tmpRoleAliasList.push(item.roleAlias)
              }
            });
            // console.log(this.selectItem)
            this.roleIdList.setValue(tmpRoleIdList);
            this.roleAliasList.setValue(tmpRoleAliasList);
          }



        }
      });

    });
  }

  async openOrgDialog() {
    const codes = ['org_chart'];
    const dict = await this.toolService.getDict(codes);
    const refDialog = this.dialogService.open(OrganizationComponent, {
      header: dict['org_chart'],
      modal: true,
      data: {
        orgList: this.orgList,
        showFooterBtn: true,
      },
      width: '90vw',
      height: '100vh'
    })

    refDialog.onClose.subscribe(res => {
      if (res) {
        if(this.currentAction == 'update'){
          this.newOrgId_u.setValue(res.data.orgID);
          this.newOrgName_u.setValue(res.data.orgName);
        }
        else{
          this.orgId.setValue(res.data.orgID);
          this.orgName.setValue(res.data.orgName);
        }
      }
    });
  }

  getOrgList() {
    this.orgService.queryTOrgList({}).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.orgList = res.RespBody.orgList;
      } else this.orgList = [];
    });
  }

  deleteRole(idx: number) {
    let tmpRoleIdList = this.currentAction == 'update' ? [...this.newRoleIdList_u.value] : [...this.roleIdList.value];
    let tmpRoleAliasList = this.currentAction == 'update' ? [...this.newRoleAliasList_u.value] : [...this.roleAliasList.value];

    tmpRoleIdList.splice(idx, 1);
    tmpRoleAliasList.splice(idx, 1);

    if(this.currentAction == 'update')
    {
      this.newRoleIdList_u.setValue(tmpRoleIdList);
      this.newRoleAliasList_u.setValue(tmpRoleAliasList);
    }
    else{
      this.roleIdList.setValue(tmpRoleIdList);
      this.roleAliasList.setValue(tmpRoleAliasList);
    }

  }

  createData(){
    let reqBody = {
      userName: this.userName.value,
      userAlias: this.userAlias.value,
      status: this.status.value,
      userEmail:this.userEmail.value,
      idpType: this.idpType.value,
      orgId: this.orgId.value,
      roleIdList: this.roleIdList.value,
    } as DPB0163Req;

    // console.log(reqBody)
    this.serverService.createIdPUser(reqBody).subscribe(async res=>{
      if (this.toolService.checkDpSuccess(res.ResHeader)) {

        const code = ['dialog.create', 'message.success', 'message.create', 'message.user'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} ${dict['message.user']}`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.pageNum = 1;
        this.currentTitle = this.title;
        this.queryAll();
      }
    })

  }

  // public get newRoleIdList() { return this.form.get('newRoleIdList')!; };
  // public get newRoleAliasList() { return this.form.get('newRoleAliasList')!; };
  // public get newOrgId() { return this.form.get('newOrgId')!; };
  // public get newOrgName() { return this.form.get('newOrgName')!; };
  public get userName() { return this.form.get('userName')!; };
  public get userAlias() { return this.form.get('userAlias')!; };
  public get status() { return this.form.get('status')!; };
  public get userEmail() { return this.form.get('userEmail')!; };
  public get idpType() { return this.form.get('idpType')!; };
  public get orgId() { return this.form.get('orgId')!; };
  public get orgName() { return this.form.get('orgName')!; };
  public get roleIdList() { return this.form.get('roleIdList')!; };
  public get roleAliasList() { return this.form.get('roleAliasList')!; };

  public get longId_u() { return this.formU.get('longId')!; };
  public get userName_u() { return this.formU.get('userName')!; };
  public get newUserName_u() { return this.formU.get('newUserName')!; };
  public get newUserAlias_u() { return this.formU.get('newUserAlias')!; };
  public get newStatus_u() { return this.formU.get('newStatus')!; };
  public get newUserEmail_u() { return this.formU.get('newUserEmail')!; };
  public get newIdpType_u() { return this.formU.get('newIdpType')!; };
  public get roleIdList_u() { return this.formU.get('roleIdList')!; };
  public get newRoleIdList_u() { return this.formU.get('newRoleIdList')!; };
  public get newRoleAliasList_u() { return this.formU.get('newRoleAliasList')!; };
  public get orgId_u() { return this.formU.get('orgId')!; };
  public get newOrgId_u() { return this.formU.get('newOrgId')!; };
  public get newOrgName_u() { return this.formU.get('newOrgName')!; };

}
