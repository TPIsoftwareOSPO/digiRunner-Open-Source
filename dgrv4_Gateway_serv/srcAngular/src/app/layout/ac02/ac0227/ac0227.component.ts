
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0083RespItem } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { DPB0095Item, DPB0095Req } from 'src/app/models/api/OpenApiService/dpb0095.interface';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { DPB0164RespItem } from 'src/app/models/api/ServerService/dpb0164.interface';

import * as dayjs from 'dayjs';
import { APIStatusPipe } from 'src/app/shared/pipes/api-status.pipe';
import { DPB0165Req, DPB0165Resp } from 'src/app/models/api/ServerService/dpb0165.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0166Req } from 'src/app/models/api/ServerService/dpb0166.interface';
import { DPB0167Req } from 'src/app/models/api/ServerService/dpb0167.interface';
import { ConfirmationService, MessageService } from 'primeng/api';

@Component({
  selector: 'app-ac0227',
  templateUrl: './ac0227.component.html',
  styleUrls: ['./ac0227.component.css'],
  providers: [ConfirmationService]
})
export class Ac0227Component extends BaseComponent implements OnInit {

  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  formIndex!: FormGroup;
  clientListCols: { field: string; header: string; }[] = [];
  clientList: Array<DPB0083RespItem> = [];
  selected: Array<DPB0083RespItem> = [];
  currentClient?: DPB0095Item | {};

  gtwIdPInfoList: Array<DPB0164RespItem> = [];
  gtwIdPInfoListCols: { field: string; header: string; }[] = [];
  selectedGtwIdPinfo?: DPB0083RespItem;
  selectedGtwIdP?: DPB0165Resp;
  selectClientId: string = '';

  _fileSrc: any = null;
  currentAction: string = '';
  // oriKeyword:string = '';


  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private openApiService: OpenApiKeyService,
    private serverService: ServerService,
    private alertService: AlertService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.formIndex = this.fb.group({
      keyword: new FormControl(''),
    });
    this.form = this.fb.group({
      id: new FormControl({ value: '', disabled: true }),
      clientId: new FormControl({ value: '', disabled: true }),
      status: new FormControl(''),
      remark: new FormControl(''),
      ldapUrl: new FormControl(''),
      ldapBaseDn: new FormControl(''),
      ldapDn: new FormControl(''),
      ldapTimeout: new FormControl(''),
      iconFile: new FormControl(''),
      pageTitle: new FormControl(''),
    });

    const codes = ['client_id', 'client_name', 'client_alias'];
    const dict = await this.toolService.getDict(codes);

    this.clientListCols = [
      { field: 'clientId', header: dict['client_id'] },
      { field: 'clientName', header: dict['client_name'] },
      { field: 'clientAlias', header: dict['client_alias'] }
    ];

    this.queryClientList();
  }

  queryClientList() {
    this.clientList = [];
    this.currentClient = {};

    let ReqBody = {
      keyword: this.keyword!.value,
      regStatus: '2'
    } as DPB0095Req;
    this.openApiService.queryClientListByRegStatusLike(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.clientList = res.RespBody.dataList;
      }
    });
  }

  moreClientList() {
    let ReqBody = {
      clientId: this.clientList[this.clientList.length - 1].clientId,
      keyword: this.keyword!.value,
      regStatus: '2'
    } as DPB0095Req;
    this.openApiService.queryClientListByRegStatusLike(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.clientList = this.clientList.concat(res.RespBody.dataList);
      }
    });
  }

  viewGtwIdPinfoList(rowData: DPB0083RespItem) {
    this.selectedGtwIdPinfo = rowData;
    this.selectClientId = rowData.clientId;
    this.queryGtwIdPInfoByClientId_ldap();
  }

  queryGtwIdPInfoByClientId_ldap() {
    this.pageNum = 2;
    this.currentTitle = `${this.title} > Client ID`;
    this.gtwIdPInfoList = [];
    this.serverService.queryGtwIdPInfoByClientId_ldap({ clientId: this.selectClientId }).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.gtwIdPInfoList = res.RespBody.dataList;
      }
    })
  }

  headerReturn() {

    if (this.pageNum > 2) {
      this.pageNum = 2;
      this.currentTitle = `${this.title} > Client ID`
    }
    else
      this.changePage('queryClientList')
  }

  async changePage(action: string, rowData?: DPB0165Req) {
    this.currentAction = action;
    const codes = ['button.detail', 'button.create', 'button.update'];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.form);

    // console.log(this.keyword?.value)
    switch (action) {
      case 'queryClientList':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;

      case 'ClientIDList':
        this.pageNum = 2;
        this.currentTitle = `${this.title} > Client ID`
        break;
      case 'detail':

        let reqDetail = {
          id: rowData?.id
        } as DPB0165Req;
        this.serverService.queryIdPUserDetail_ldap(reqDetail).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.detail']}`
            this.pageNum = 3;
            this.selectedGtwIdP = res.RespBody;
          }
        })
        break;
      case 'create':
        this.serverService.createGtwIdPInfo_ldap_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.create']}`
            this.pageNum = 4;
            this.addFormValidator(this.form, res.RespBody.constraints);
            this.clientId?.setValue(this.selectClientId);
            this.status?.markAsTouched();


          }
        })
        break;
      case 'update':
        let req = {
          id: rowData?.id
        } as DPB0165Req;
        this.serverService.queryIdPUserDetail_ldap(req).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.selectedGtwIdP = res.RespBody;
            this.serverService.updateGtwIdPInfo_ldap_before().subscribe(res => {
              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);

                this.currentTitle += `> ${dict['button.update']}`
                this.pageNum = 4;

                this.clientId?.setValue(this.selectedGtwIdP?.clientId);
                this.id?.setValue(this.selectedGtwIdP?.id);
                this.status?.setValue(this.selectedGtwIdP?.status);
                this.remark?.setValue(this.selectedGtwIdP?.remark);
                this.ldapUrl?.setValue(this.selectedGtwIdP?.ldapUrl);
                this.ldapDn?.setValue(this.selectedGtwIdP?.ldapDn);
                this.ldapTimeout?.setValue(this.selectedGtwIdP?.ldapTimeout);
                this.ldapBaseDn?.setValue(this.selectedGtwIdP?.ldapBaseDn);
                // this.iconFile?.setValue(this.selectedGtwIdP?.iconFile);
                this.pageTitle?.setValue(this.selectedGtwIdP?.pageTitle);
                this._fileSrc = this.selectedGtwIdP?.iconFile;
              }
            })
          }
        })
        break;
    }
  }


  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') : '';
  }

  activeSelectedITem() {
    console.log('selected', this.selected)
  }

  update() {
    let reqBody = {
      id: this.id?.value,
      clientId: this.clientId?.value,
      status: this.status?.value,
      remark: this.remark?.value,
      ldapUrl: this.ldapUrl?.value,
      ldapBaseDn: this.ldapBaseDn?.value,
      ldapDn: this.ldapDn?.value,
      ldapTimeout: this.ldapTimeout?.value,
      pageTitle: this.pageTitle?.value,
      iconFile: this._fileSrc
    } as DPB0167Req;


    this.serverService.updateGtwIdPInfo_ldap(reqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} GTW LDAP IdP`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.queryGtwIdPInfoByClientId_ldap();
      }
    })
  }

  create() {
    let reqBody = {
      clientId: this.clientId?.value,
      status: this.status?.value,
      remark: this.remark?.value,
      ldapUrl: this.ldapUrl?.value,
      ldapBaseDn: this.ldapBaseDn?.value,
      ldapDn: this.ldapDn?.value,
      ldapTimeout: this.ldapTimeout?.value,
      pageTitle: this.pageTitle?.value,
    } as DPB0166Req;
    if (this._fileSrc) reqBody.iconFile = this._fileSrc;

    this.serverService.createGtwIdPInfo_ldap(reqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} GTW LDAP IdP`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.queryGtwIdPInfoByClientId_ldap();
      }
    })
  }

  async deleteItem(rowData: DPB0165Req) {
    const code = ['cfm_del',];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: dict['cfm_del'],
      message: `${rowData.id}`,
      accept: () => {

        this.serverService.deleteGtwIdPInfo_ldap({ id: rowData.id }).subscribe(async res => {
          const code = ['message.delete', 'message.success'];
          const dict = await this.toolService.getDict(code);
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success', summary: `${dict['message.delete']} GTW LDAP IdP`,
              detail: `${dict['message.delete']} ${dict['message.success']}!`
            });

            this.queryGtwIdPInfoByClientId_ldap();
          }
        })
      }
    });
  }

  openFileBrowser() {
    $('#fileName').click();
  }

  async fileChange(files: FileList) {
    const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
    const dict = await this.toolService.getDict(code);
    if (files.length != 0) {
      let fileReader = new FileReader();
      fileReader.onloadend = () => {

        if (files.item(0)!.size > 2000) {
          this.alertService.ok('Error', 'File size cannot exceed 2k.');
          this.clearFile();
          return;
        }

        this.messageService.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });

        this._fileSrc = fileReader.result!;


      }
      fileReader.readAsDataURL(files.item(0)!);
    }
    else {
      this._fileSrc = null;
    }
  }

  public clearFile() {
    this._fileSrc = null;
    $('#fileName').val('');
  }

  public get keyword() { return this.formIndex.get('keyword'); };

  public get clientId() { return this.form.get('clientId'); };
  public get id() { return this.form.get('id'); };
  public get status() { return this.form.get('status'); };
  public get remark() { return this.form.get('remark'); };
  public get ldapUrl() { return this.form.get('ldapUrl'); };
  public get ldapBaseDn() { return this.form.get('ldapBaseDn'); };
  public get ldapDn() { return this.form.get('ldapDn'); };
  public get ldapTimeout() { return this.form.get('ldapTimeout'); };
  public get iconFile() { return this.form.get('iconFile'); };
  public get pageTitle() { return this.form.get('pageTitle'); };

}

