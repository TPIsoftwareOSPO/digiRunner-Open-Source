import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { DPB0095Item, DPB0095Req } from 'src/app/models/api/OpenApiService/dpb0095.interface';
import { DPB0169RespItem } from 'src/app/models/api/ServerService/dpb0169.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import * as dayjs from 'dayjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0202Req, DPB0202RespItem } from 'src/app/models/api/ServerService/dpb0202.interface';
import { DialogService } from 'primeng/dynamicdialog';
import { ConnectionInfoListComponent } from 'src/app/shared/connection-info-list/connection-info-list.component';
import { DPB0190RespItem } from 'src/app/models/api/ServerService/dpb0190.interface';
import { DPB0204Req } from 'src/app/models/api/ServerService/dpb0204.interface';
import { DPB0203Req, DPB0203Resp } from 'src/app/models/api/ServerService/dpb0203.interface';
import { DPB0205Req } from 'src/app/models/api/ServerService/dpb0205.interface';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-ac0229',
  templateUrl: './ac0229.component.html',
  styleUrls: ['./ac0229.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class Ac0229Component extends BaseComponent implements OnInit {

  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  formEdit!: FormGroup;
  clientListCols: { field: string; header: string; }[] = [];
  clientList: Array<DPB0095Item> = [];
  currentClient?: DPB0095Item;
  currentAction: string = '';
  gtwIdpList: Array<DPB0202RespItem> = [];
  gtwIdPInfo?: DPB0203Resp;
  // methodsList: { label: string; value: string }[] = [
  //   { value: 'POST', label: 'POST' },
  //   { value: 'GET', label: 'GET' }
  // ];
  _fileSrc: any = null;

  serectAlgorithmList: { label: string; value: string; }[] = [
    { label: 'Bcrypt', value: 'Bcrypt' },
    { label: 'SHA256', value: 'SHA256' },
    { label: 'SHA512', value: 'SHA512' },
    { label: 'Plain', value: 'Plain' },
  ];

  idTokenExp: string = `{
    "at_hash": "FOiDD5zzh0sLqMMYGkovbw",
    "aud": "aud",
    "sub": "{{$user_id%}}",
    "iss": "https://domain/dgrv4/ssotoken/API",
    "name": "{{$user_name_th%}} {{$user_name_en%}}",
    "exp": 1690611646,
    "iat": 1685427646,
    "email": "{{$user_email%}}",
    "picture": "{{$user_picture%}}"
}`;

  rawExp: string = `{
    "username": "{{$username%}}",
    "password": "{{$password%}}",
    "uid": "{{$ip%}}",
    "check": "Y"
}`;

  paramsExp: string = `["{{$username%}}", "1"]`;
  sqlResult: string = `For example, the user account entered is tspuser :
select * from users u, tsmp_user ts where upper(u.username) = upper('tspuser') and u.enabled = '1' and u.username = ts.user_name`
  colnameExp: string = `{{$user_password%}}`;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private openApiService: OpenApiKeyService,
    private messageService: MessageService,
    private dialogService: DialogService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private sanitizer: DomSanitizer
  ) {
    super(route, tr);
  }

  async ngOnInit() {

    this.form = this.fb.group({
      keyword: new FormControl(''),
    });
    this.formEdit = this.fb.group({
      id: new FormControl({ value: '', disabled: true }),
      clientId: new FormControl({ value: '', disabled: true }),
      status: new FormControl(''),
      remark: new FormControl(''),

      connectionName: new FormControl(''),
      sqlPtmt: new FormControl(''),
      sqlParams: new FormControl(''),
      userMimaAlg: new FormControl(''),
      userMimaColName: new FormControl(''),

      idtSub: new FormControl(''),
      idtName: new FormControl(''),
      idtEmail: new FormControl(''),
      idtPicture: new FormControl(''),

      iconFile: new FormControl(''),
      pageTitle: new FormControl(''),

      createUser: new FormControl(''),
      createDateTime: new FormControl(''),
      updateUser: new FormControl(''),
      updateDateTime: new FormControl('')
    });

    const codes = ['client_id', 'client_name', 'client_alias'];
    const dict = await this.toolService.getDict(codes);

    this.clientListCols = [
      { field: 'clientId', header: dict['client_id'] },
      { field: 'clientName', header: dict['client_name'] },
      { field: 'clientAlias', header: dict['client_alias'] }
    ];

    this.openApiService.queryClientListByRegStatusLike_ignore1298({ keyword: this.keyword!.value, regStatus: '2' }).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.clientList = res.RespBody.dataList;
      }
    });
  }

  queryClientList() {

    this.clientList = [];
    this.currentClient = undefined;

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

  headerReturn() {
    // console.log(first)

    if (this.pageNum > 2) {
      this.pageNum = 2;
      this.currentTitle = `${this.title} > GTW JDBC IdP Client List`
    }
    else
      this.changePage('queryClientList')
  }

  viewGtwIdPinfoList(rowData: DPB0095Item) {
    this.currentClient = rowData;
    // console.log(rowData)
    this.queryGtwIdPInfoByClientId_jdbc();
  }

  queryGtwIdPInfoByClientId_jdbc() {
    this.pageNum = 2;
    this.currentTitle = `${this.title} > GTW JDBC IdP Client List`
    let reqBody = {
      clientId: this.currentClient?.clientId
    } as DPB0202Req
    this.gtwIdpList = [];
    this.serverService.queryGtwIdPInfoByClientId_jdbc(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        // console.log('resp', res.RespBody.dataList)
        this.gtwIdpList = res.RespBody.dataList;
      }
    })
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

  async changePage(action: string, rowData?: DPB0169RespItem) {
    const codes = ['button.detail', 'button.create', 'button.update', 'cfm_del', 'message.delete', 'message.success'];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);
    this.currentAction = action;
    this.formEdit.enable();
    this.clearFile();

    switch (action) {
      case 'queryClientList':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'clientList':
        this.currentTitle = `${this.title} > GTW JDBC IdP Client List`
        this.pageNum = 2;
        break;
      case 'detail':
        let reqDetail = {
          id: rowData?.id
        } as DPB0203Req;
        this.serverService.queryGtwIdPInfoDetail_jdbc(reqDetail).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.detail']}`
            this.pageNum = 4;
            this.gtwIdPInfo = res.RespBody;
            this.formEdit.disable();
            this.id.setValue(res.RespBody.id);
            this.clientId.setValue(res.RespBody.clientId);
            this.status.setValue(res.RespBody.status);
            this.remark.setValue(res.RespBody.remark);

            this.connectionName.setValue(res.RespBody.connectionName);
            this.sqlPtmt.setValue(res.RespBody.sqlPtmt);
            this.sqlParams.setValue(res.RespBody.sqlParams);
            this.userMimaAlg.setValue(res.RespBody.userMimaAlg);
            this.userMimaColName.setValue(res.RespBody.userMimaColName);

            this.idtSub.setValue(res.RespBody.idtSub);
            this.idtName.setValue(res.RespBody.idtName);
            this.idtEmail.setValue(res.RespBody.idtEmail);
            this.idtPicture.setValue(res.RespBody.idtPicture);
            if (res.RespBody.iconFile) this._fileSrc = res.RespBody.iconFile;
            this.pageTitle.setValue(res.RespBody.pageTitle);

            this.createUser.setValue(res.RespBody.createUser);
            this.createDateTime.setValue(res.RespBody.createDateTime ? this.toolService.setformate(new Date(res.RespBody.createDateTime), 'YYYY-MM-DD HH:mm:ss') : '');
            this.updateUser.setValue(res.RespBody.updateUser);
            this.updateDateTime.setValue(res.RespBody.updateDateTime ? this.toolService.setformate(new Date(res.RespBody.updateDateTime), 'YYYY-MM-DD HH:mm:ss') : '');

          }
        })
        break;
      case 'create':
        this.serverService.createGtwIdPInfo_jdbc_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.create']}`
            this.pageNum = 4;

            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.clientId?.setValue(this.currentClient?.clientId);
            this.clientId?.disable();
            this.status?.markAsTouched();
            this.connectionName.markAsTouched();

          }
        })
        break;
      case 'update':
        let reqD = {
          id: rowData?.id
        } as DPB0203Req;
        this.serverService.queryGtwIdPInfoDetail_jdbc(reqD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {

            this.gtwIdPInfo = res.RespBody;
            // console.log(this.gtwIdPInfo)

            this.serverService.updateGtwIdPInfo_jdbc_before().subscribe(resValid => {
              if (this.toolService.checkDpSuccess(resValid.ResHeader)) {
                this.currentTitle += `> ${dict['button.update']}`
                this.pageNum = 4;
                this.addFormValidator(this.formEdit, resValid.RespBody.constraints);

                this.id.setValue(res.RespBody.id);
                this.id.disable();
                this.clientId.setValue(res.RespBody.clientId);
                this.clientId.disable();
                this.status.setValue(res.RespBody.status);
                this.remark.setValue(res.RespBody.remark);

                this.connectionName.setValue(res.RespBody.connectionName);
                this.sqlPtmt.setValue(res.RespBody.sqlPtmt);
                this.sqlParams.setValue(res.RespBody.sqlParams);
                this.userMimaAlg.setValue(res.RespBody.userMimaAlg);
                this.userMimaColName.setValue(res.RespBody.userMimaColName);

                this.idtSub.setValue(res.RespBody.idtSub);
                this.idtName.setValue(res.RespBody.idtName);
                this.idtEmail.setValue(res.RespBody.idtEmail);
                this.idtPicture.setValue(res.RespBody.idtPicture);
                if (res.RespBody.iconFile) this._fileSrc = res.RespBody.iconFile;
                this.pageTitle.setValue(res.RespBody.pageTitle);

              }
            })

          }
        })
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${rowData?.id}`,
          accept: () => {

            this.serverService.deleteGtwIdPInfo_jdbc({ id: rowData!.id }).subscribe(async res => {

              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({
                  severity: 'success', summary: `${dict['message.delete']} GTW JDBC IdP Client List`,
                  detail: `${dict['message.delete']} ${dict['message.success']}!`
                });
                this.queryGtwIdPInfoByClientId_jdbc();
              }
            })

          }
        });
        break;
    }

  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') : '';
  }

  openFileBrowser() {
    $('#fileName').click();
  }

  public clearFile() {
    this._fileSrc = null;
    $('#fileName').val('');
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

        this._fileSrc = fileReader.result;

      }
      fileReader.readAsDataURL(files.item(0)!);
    }
    else {
      this._fileSrc = null;
    }
  }

  async create() {

    let reqCreate = {
      clientId: this.clientId.value,
      status: this.status.value,
      remark: this.remark.value,
      connectionName: this.connectionName.value,
      sqlPtmt: this.sqlPtmt.value,
      sqlParams: this.sqlParams.value,
      userMimaAlg: this.userMimaAlg.value,
      userMimaColName: this.userMimaColName.value,
      idtSub: this.idtSub.value,
      idtName: this.idtName.value,
      idtEmail: this.idtEmail.value,
      idtPicture: this.idtPicture.value,
      pageTitle: this.pageTitle.value
    } as DPB0204Req;


    if (this._fileSrc) reqCreate.iconFile = this._fileSrc

    this.serverService.createGtwIdPInfo_jdbc(reqCreate).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} GTW JDBC IdP Client List`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.queryGtwIdPInfoByClientId_jdbc();
      }
    })
  }

  async update() {

    let reqbody = {
      id: this.id.value,
      clientId: this.clientId.value,
      status: this.status.value,
      remark: this.remark.value,
      connectionName: this.connectionName.value,
      sqlPtmt: this.sqlPtmt.value,
      sqlParams: this.sqlParams.value,
      userMimaAlg: this.userMimaAlg.value,
      userMimaColName: this.userMimaColName.value,
      idtSub: this.idtSub.value,
      idtName: this.idtName.value,
      idtEmail: this.idtEmail.value,
      idtPicture: this.idtPicture.value,
      pageTitle: this.pageTitle.value
    } as DPB0205Req;

    if (this._fileSrc) reqbody.iconFile = this._fileSrc

    this.serverService.updateGtwIdPInfo_jdbc(reqbody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} GTW JDBC IdP Client List`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.queryGtwIdPInfoByClientId_jdbc();
      }
    })

  }

  queryRdbConnectionInfoList() {
    const ref = this.dialogService.open(ConnectionInfoListComponent, {
      header: 'Connection Info',
      width: '1000px'
    })

    ref.onClose.subscribe((res: DPB0190RespItem) => {
      if (res) {
        //console.log(res)
        this.connectionName.setValue(res.connectionName)
      }
      // else {
      //   this.connectionName.setValue('')
      // }
      this.connectionName.markAsTouched();
    });
  }

  public get keyword() { return this.form.get('keyword'); };

  public get id() { return this.formEdit.get('id')!; };
  public get clientId() { return this.formEdit.get('clientId')!; };
  public get status() { return this.formEdit.get('status')!; };
  public get remark() { return this.formEdit.get('remark')!; };

  public get connectionName() { return this.formEdit.get('connectionName')!; };
  public get sqlPtmt() { return this.formEdit.get('sqlPtmt')!; };
  public get sqlParams() { return this.formEdit.get('sqlParams')!; };
  public get userMimaAlg() { return this.formEdit.get('userMimaAlg')!; };

  public get userMimaColName() { return this.formEdit.get('userMimaColName')!; };
  public get idtSub() { return this.formEdit.get('idtSub')!; };
  public get idtName() { return this.formEdit.get('idtName')!; };
  public get idtEmail() { return this.formEdit.get('idtEmail')!; };
  public get idtPicture() { return this.formEdit.get('idtPicture')!; };

  public get iconFile() { return this.formEdit.get('iconFile')!; };
  public get pageTitle() { return this.formEdit.get('pageTitle')!; };

  public get createUser() { return this.formEdit.get('createUser')!; };
  public get createDateTime() { return this.formEdit.get('createDateTime')!; };
  public get updateUser() { return this.formEdit.get('updateUser')!; };
  public get updateDateTime() { return this.formEdit.get('updateDateTime')!; };

}
