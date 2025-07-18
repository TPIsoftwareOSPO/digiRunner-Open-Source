import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import {
  DPB0095Item,
  DPB0095Req,
} from 'src/app/models/api/OpenApiService/dpb0095.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import {
  DPB0169Req,
  DPB0169RespItem,
} from 'src/app/models/api/ServerService/dpb0169.interface';
import * as dayjs from 'dayjs';
import {
  DPB0170Req,
  DPB0170Resp,
} from 'src/app/models/api/ServerService/dpb0170.interface';
import { DPB0171Req } from 'src/app/models/api/ServerService/dpb0171.interface';
import { ConfirmationService, MessageService } from 'primeng/api';
import { DPB0172Req } from 'src/app/models/api/ServerService/dpb0172.interface';

@Component({
  selector: 'app-ac0226',
  templateUrl: './ac0226.component.html',
  styleUrls: ['./ac0226.component.css'],
  providers: [ConfirmationService],
})
export class Ac0226Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  formEdit!: FormGroup;
  clientListCols: { field: string; header: string }[] = [];
  clientList: Array<DPB0095Item> = [];
  currentClient?: DPB0095Item;
  gtwIdpList: Array<DPB0169RespItem> = [];
  gtwIdPInfo?: DPB0170Resp;
  currentAction: string = '';

  idpTypeList: { label: string; value: string }[] = [
    { label: 'GOOGLE', value: 'GOOGLE' },
    { label: 'MS', value: 'MS' },
    { label: 'OIDC', value: 'OIDC' },
  ];

  showPassword: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private serverService: ServerService,
    private openApiService: OpenApiKeyService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
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
      idpType: new FormControl(''),
      status: new FormControl(''),
      remark: new FormControl(''),
      idpClientId: new FormControl(''),
      idpClientMima: new FormControl(''),
      idpClientName: new FormControl(''),
      wellKnownUrl: new FormControl(''),
      callbackUrl: new FormControl(''),
      authUrl: new FormControl(''),
      accessTokenUrl: new FormControl(''),
      scope: new FormControl(''),
    });

    const codes = ['client_id', 'client_name', 'client_alias'];
    const dict = await this.toolService.getDict(codes);

    this.clientListCols = [
      { field: 'clientId', header: dict['client_id'] },
      { field: 'clientName', header: dict['client_name'] },
      { field: 'clientAlias', header: dict['client_alias'] },
    ];

    this.openApiService
      .queryClientListByRegStatusLike_ignore1298({ regStatus: '2' })
      .subscribe((res) => {
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
      regStatus: '2',
    } as DPB0095Req;

    this.openApiService
      .queryClientListByRegStatusLike(ReqBody)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.clientList = res.RespBody.dataList;
        }
      });
  }

  moreClientList() {
    let ReqBody = {
      clientId: this.clientList[this.clientList.length - 1].clientId,
      keyword: this.keyword!.value,
      regStatus: '2',
    } as DPB0095Req;
    this.openApiService
      .queryClientListByRegStatusLike(ReqBody)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.clientList = this.clientList.concat(res.RespBody.dataList);
        }
      });
  }

  headerReturn() {
    if (this.pageNum > 2) {
      this.pageNum = 2;
      this.currentTitle = `${this.title} > GTW OAuth 2.0 IdP Client List`;
    } else this.changePage('queryClientList');
  }

  viewGtwIdPinfoList(rowData: DPB0095Item) {
    this.currentClient = rowData;
    this.queryGtwIdPInfoByClientId_oauth2();
  }

  queryGtwIdPInfoByClientId_oauth2() {
    this.pageNum = 2;
    this.currentTitle = `${this.title} > GTW OAuth 2.0 IdP Client List`;
    let reqBody = {
      clientId: this.currentClient?.clientId,
    } as DPB0169Req;
    this.gtwIdpList = [];
    this.serverService
      .queryGtwIdPInfoByClientId_oauth2(reqBody)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.gtwIdpList = res.RespBody.dataList;
        }
      });
  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'
      ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss')
      : '';
  }

  async changePage(action: string, rowData?: DPB0169RespItem) {
    const codes = [
      'button.detail',
      'button.create',
      'button.update',
      'cfm_del',
      'message.delete',
      'message.success',
    ];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);
    this.currentAction = action;
    this.showPassword = false;
    switch (action) {
      case 'queryClientList':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'clientList':
        this.currentTitle = `${this.title} > GTW OAuth 2.0 IdP Client List`;
        this.pageNum = 2;
        break;
      case 'detail':
        let reqDetail = {
          id: rowData?.id,
        } as DPB0170Req;
        this.serverService
          .queryGtwIdPInfo_oauth2(reqDetail)
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.currentTitle += `> ${dict['button.detail']}`;
              this.pageNum = 3;
              this.gtwIdPInfo = res.RespBody;
            }
          });
        break;
      case 'create':
        this.serverService.createGtwIdPInfo_oauth2_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.create']}`;
            this.pageNum = 4;
            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.clientId?.setValue(this.currentClient?.clientId);
            this.status?.markAsTouched();
          }
        });
        break;
      case 'update':
        let reqD = {
          id: rowData?.id,
        } as DPB0170Req;
        this.serverService.queryGtwIdPInfo_oauth2(reqD).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.gtwIdPInfo = res.RespBody;

            this.serverService
              .updateGtwIdPInfo_oauth2_before()
              .subscribe((resValidate) => {
                if (this.toolService.checkDpSuccess(resValidate.ResHeader)) {
                  this.currentTitle += `> ${dict['button.update']}`;
                  this.pageNum = 4;
                  this.addFormValidator(
                    this.formEdit,
                    resValidate.RespBody.constraints
                  );

                  this.clientId?.setValue(this.currentClient?.clientId);
                  this.id?.setValue(this.gtwIdPInfo?.id);
                  this.idpType?.setValue(this.gtwIdPInfo?.idpType);
                  this.status?.setValue(this.gtwIdPInfo?.status);
                  this.remark?.setValue(this.gtwIdPInfo?.remark);
                  if (this.gtwIdPInfo?.idpClientId)
                    this.idpClientId?.setValue(this.gtwIdPInfo?.idpClientId);
                  if (this.gtwIdPInfo?.idpClientMima)
                    this.idpClientMima?.setValue(
                      this.gtwIdPInfo?.idpClientMima
                    );
                  if (this.gtwIdPInfo?.idpClientName)
                    this.idpClientName?.setValue(
                      this.gtwIdPInfo?.idpClientName
                    );
                  if (this.gtwIdPInfo?.wellKnownUrl)
                    this.wellKnownUrl?.setValue(this.gtwIdPInfo?.wellKnownUrl);
                  if (this.gtwIdPInfo?.callbackUrl)
                    this.callbackUrl?.setValue(this.gtwIdPInfo?.callbackUrl);
                  if (this.gtwIdPInfo?.authUrl)
                    this.authUrl?.setValue(this.gtwIdPInfo?.authUrl);
                  if (this.gtwIdPInfo?.accessTokenUrl)
                    this.accessTokenUrl?.setValue(
                      this.gtwIdPInfo?.accessTokenUrl
                    );
                  if (this.gtwIdPInfo?.scope)
                    this.scope?.setValue(this.gtwIdPInfo?.scope);
                  this.status?.markAsTouched();

                  this.formEdit.markAllAsTouched();
                }
              });
          }
        });
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${rowData?.id}`,
          accept: () => {
            this.serverService
              .deleteGtwIdPInfo_oauth2({ id: rowData!.id })
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']} GTW OAuth 2.0 IdP Client List`,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.queryGtwIdPInfoByClientId_oauth2();
                }
              });
          },
        });
        break;
    }
  }

  create() {
    let reqBody = {
      clientId: this.clientId?.value,
      idpType: this.idpType?.value,
      status: this.status?.value,

      idpClientId: this.idpClientId?.value,
      idpClientMima: this.idpClientMima?.value,
      idpClientName: this.idpClientName?.value,
      wellKnownUrl: this.wellKnownUrl?.value,
      callbackUrl: this.callbackUrl?.value,
    } as DPB0171Req;
    if (this.remark?.value) reqBody.remark = this.remark.value;
    if (this.authUrl?.value) reqBody.authUrl = this.authUrl.value;
    if (this.accessTokenUrl?.value)
      reqBody.accessTokenUrl = this.accessTokenUrl.value;
    if (this.scope?.value) reqBody.scope = this.scope.value;

    this.serverService
      .createGtwIdPInfo_oauth2(reqBody)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['message.create', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['message.create']} GTW OAuth 2.0 IdP Client List`,
            detail: `${dict['message.create']} ${dict['message.success']}!`,
          });

          this.queryGtwIdPInfoByClientId_oauth2();
        }
      });
  }

  update() {
    let reqBody = {
      id: this.id?.value,
      clientId: this.clientId?.value,
      idpType: this.idpType?.value,
      status: this.status?.value,

      idpClientId: this.idpClientId?.value,
      idpClientMima: this.idpClientMima?.value,
      idpClientName: this.idpClientName?.value,
      wellKnownUrl: this.wellKnownUrl?.value,
      callbackUrl: this.callbackUrl?.value,
    } as DPB0172Req;
    if (this.remark?.value) reqBody.remark = this.remark.value;
    if (this.authUrl?.value) reqBody.authUrl = this.authUrl.value;
    if (this.accessTokenUrl?.value)
      reqBody.accessTokenUrl = this.accessTokenUrl.value;
    if (this.scope?.value) reqBody.scope = this.scope.value;

    this.serverService
      .updateGtwIdPInfo_oauth2(reqBody)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['message.update', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['message.update']} GTW OAuth 2.0 IdP Client List`,
            detail: `${dict['message.update']} ${dict['message.success']}!`,
          });

          this.queryGtwIdPInfoByClientId_oauth2();
        }
      });
  }

  copyToClipboard(value?: string) {
    if (value) {
      navigator.clipboard
        .writeText(value)
        .then(async () => {
          const code = ['copy', 'data', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['copy']} ${dict['data']}`,
            detail: `${dict['copy']} ${dict['message.success']}`,
          });

          console.log('複製成功');
        })
        .catch((err) => {
          console.error('複製失敗', err);
          // 可加上錯誤提示
        });
    } else {
      console.warn('無資料可複製');
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  maskAllWithDots(input:string = '') {
    return input?.length? '●'.repeat(input.length) : '';
  }
  public get keyword() {
    return this.form.get('keyword');
  }

  public get id() {
    return this.formEdit.get('id');
  }
  public get clientId() {
    return this.formEdit.get('clientId');
  }
  public get status() {
    return this.formEdit.get('status');
  }
  public get idpType() {
    return this.formEdit.get('idpType');
  }
  public get remark() {
    return this.formEdit.get('remark');
  }
  public get idpClientId() {
    return this.formEdit.get('idpClientId');
  }
  public get idpClientMima() {
    return this.formEdit.get('idpClientMima');
  }
  public get idpClientName() {
    return this.formEdit.get('idpClientName');
  }
  public get wellKnownUrl() {
    return this.formEdit.get('wellKnownUrl');
  }
  public get callbackUrl() {
    return this.formEdit.get('callbackUrl');
  }
  public get authUrl() {
    return this.formEdit.get('authUrl');
  }
  public get accessTokenUrl() {
    return this.formEdit.get('accessTokenUrl');
  }
  public get scope() {
    return this.formEdit.get('scope');
  }
}
