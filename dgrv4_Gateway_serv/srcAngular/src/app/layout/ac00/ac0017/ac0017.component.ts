import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { DPB0149Item } from 'src/app/models/api/ServerService/dpb0149.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { DPB0150Req } from 'src/app/models/api/ServerService/dpb0150.interface';
import { DPB0151Req } from 'src/app/models/api/ServerService/dpb0151.interface';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0152Req } from 'src/app/models/api/ServerService/dpb0152.interface';
import * as dayjs from 'dayjs';

@Component({
  selector: 'app-ac0017',
  templateUrl: './ac0017.component.html',
  styleUrls: ['./ac0017.component.css'],
  providers: [ConfirmationService],
})
export class Ac0017Component extends BaseComponent implements OnInit {
  currentTitle: string = this.title;
  form!: FormGroup;
  cols: { field: string; header: string }[] = [];
  pageNum: number = 1; // 1: 查詢、2: 詳細資料 & 刪除、3: 建立 & 更新
  currentAction: string = 'query';
  //read only
  acIpdInfoId!: string;
  createUser!: string;
  createDateTime!: string;
  updateUser!: string;
  updateDateTime!: string;
  acAuthList: any;
  userDetail?: DPB0149Item;
  updateId?: string;

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
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {
    super(route, tr);
    this.cols = [
      { field: 'id', header: 'ID' }, //
      { field: 'idpType', header: 'Type' },
      { field: 'clientId', header: 'Client Id' }, //
      { field: 'clientMima', header: 'Client Secret' },
      { field: 'clientName', header: 'Client Name' },
      { field: 'idpWellKnownUrl', header: 'IdpWellKnownUrl' },
      { field: 'callbackUrl', header: 'Callback Url' },
      { field: 'createUser', header: 'Create User' },
      { field: 'createDateTime', header: 'Create Time' },
      { field: 'updateUser', header: 'Update User' },
      { field: 'updateDateTime', header: 'Update Time' },
      { field: 'clientStatus', header: 'Client Status' },
      { field: 'authUrl', header: 'Auth Url' },
      { field: 'accessTokenUrl', header: 'Access Token Url' },
      { field: 'scope', header: 'Scope' },
    ];
    this.acAuthList = [];
  }

  ngOnInit(): void {
    // ,ValidatorFns.websiteAddressValidator()
    this.form = this.fb.group({
      idpType: new FormControl('', [ValidatorFns.requiredValidator()]),
      clientId: new FormControl('', [ValidatorFns.requiredValidator()]),
      clientMima: new FormControl('', [ValidatorFns.requiredValidator()]),
      clientName: new FormControl('', [ValidatorFns.requiredValidator()]),
      idpWellKnownUrl: new FormControl('', [ValidatorFns.requiredValidator()]),
      callbackUrl: new FormControl('', [ValidatorFns.requiredValidator()]),
      clientStatus: new FormControl('', [ValidatorFns.requiredValidator()]),
      authUrl: new FormControl(''),
      accessTokenUrl: new FormControl(''),
      scope: new FormControl(''),
    });

    this.queryAcOauth();
  }

  queryAcOauth() {
    this.serverService.QueryDgrAcIdInfoAll().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        // console.log(res.RespBody)
        this.acAuthList = res.RespBody.dgrAcIdpInfo;
      } else this.acAuthList = [];
    });
  }

  updateAcOauth() {
    let reqBody = {
      idpType: this.idpType?.value,
      clientId: this.clientId?.value,
      clientMima: this.clientMima?.value,
      clientName: this.clientName?.value,
      idpWellKnownUrl: this.idpWellKnownUrl?.value,
      callbackUrl: this.callbackUrl?.value,
      clientStatus: this.clientStatus?.value,
      authUrl: this.authUrl?.value,
      accessTokenUrl: this.accessTokenUrl?.value,
      scope: this.scope?.value,
      id: this.updateId,
    } as DPB0151Req;

    this.serverService.updateDgrAcIdInfo(reqBody).subscribe(async (res) => {
      const code = ['message.update', 'message.success', 'message.user'];
      const dict = await this.toolService.getDict(code);
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.update']} ${dict['message.user']}`,
          detail: `${dict['message.update']} ${dict['message.success']}!`,
        });
        this.queryAcOauth();
        this.changePage('query');
      }
    });
  }

  onDeleteAcOauth() {
    this.messageService.clear();
    let reqBody = {
      id: this.updateId,
    } as DPB0152Req;

    this.serverService.deleteDgrAcIdInfo(reqBody).subscribe(async (res) => {
      const code = ['message.delete', 'message.success', 'message.user'];
      const dict = await this.toolService.getDict(code);
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.delete']} ${dict['message.user']}`,
          detail: `${dict['message.delete']} ${dict['message.success']}!`,
        });
        this.queryAcOauth();
        this.changePage('query');
      }
    });
  }

  onReject() {
    this.messageService.clear();
  }

  async deleteAcOauth() {
    const code = ['cfm_del', 'user_name', 'user_alias'];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: dict['cfm_del'],
      message: `${this.userDetail!.id}`,
      accept: () => {
        this.onDeleteAcOauth();
      },
    });
  }

  createAcOauth() {
    let reqBody = {
      idpType: this.idpType?.value,
      clientId: this.clientId?.value,
      clientMima: this.clientMima?.value,
      clientName: this.clientName?.value,
      idpWellKnownUrl: this.idpWellKnownUrl?.value,
      callbackUrl: this.callbackUrl?.value,
      clientStatus: this.clientStatus?.value,
      authUrl: this.authUrl?.value,
      accessTokenUrl: this.accessTokenUrl?.value,
      scope: this.scope?.value,
    } as DPB0150Req;

    this.serverService.AddDgrAcIdInfo(reqBody).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success', 'message.user'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.create']} ${dict['message.user']}`,
          detail: `${dict['message.create']} ${dict['message.success']}!`,
        });
        this.queryAcOauth();
        this.changePage('query');
      }
    });
  }

  submitForm() {}

  initRequiredForm() {
    this.idpType!.setValidators([ValidatorFns.requiredValidator()]);
    this.clientId!.setValidators([ValidatorFns.requiredValidator()]);
    this.clientMima!.setValidators([ValidatorFns.requiredValidator()]);
    this.clientName!.setValidators([ValidatorFns.requiredValidator()]);
    this.idpWellKnownUrl!.setValidators([ValidatorFns.requiredValidator()]);
    this.callbackUrl!.setValidators([ValidatorFns.requiredValidator()]);
    this.clientStatus!.setValidators([ValidatorFns.requiredValidator()]);
    this.form.updateValueAndValidity();
  }

  initData() {
    this.idpType?.setValue('');
    this.clientId?.setValue('');
    this.clientMima?.setValue('');
    this.clientName?.setValue('');
    this.idpWellKnownUrl?.setValue('');
    this.callbackUrl?.setValue('');
    this.clientStatus?.setValue('');
    this.authUrl?.setValue('');
    this.accessTokenUrl?.setValue('');
    this.scope?.setValue('');
  }

  async changePage(action: string, rowData?: DPB0149Item) {
    const code = [
      'button.detail',
      'button.delete',
      'button.create',
      'button.update',
    ];
    const dict = await this.toolService.getDict(code);
    this.currentAction = action;
    const _newData = rowData;
    this.resetFormValidator(this.form);
    this.initRequiredForm();
    this.showPassword = false;
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.initData();
        // console.log(this.form);
        // this.serverService.AddDgrAcIdInfo_before().subscribe(res=>{
        //   if(this.toolService.checkDpSuccess(res.ResHeader)){
        //     this.addFormValidator(this.form, res.RespBody.constraints)
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 4;
        //   }
        // })
        break;
      case 'update':
        this.updateId = rowData?.id;
        this.pageNum = 4;
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.idpType!.setValue(_newData?.idpType);
        this.clientName!.setValue(_newData?.clientName);
        this.clientId!.setValue(_newData?.clientId);
        this.clientMima!.setValue(_newData?.clientMima);
        this.idpWellKnownUrl!.setValue(_newData?.idpWellKnownUrl);
        this.callbackUrl!.setValue(_newData?.callbackUrl);
        this.clientStatus!.setValue(_newData?.clientStatus);
        this.authUrl?.setValue(_newData?.authUrl);
        this.accessTokenUrl?.setValue(_newData?.accessTokenUrl);
        this.scope?.setValue(_newData?.scope);
        break;
      case 'detail':
        this.pageNum = 2;
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.userDetail = rowData;
        break;
      case 'delete':
        this.updateId = rowData?.id;
        this.pageNum = 2;
        this.currentTitle = `${this.title} > ${dict['button.delete']}`;
        this.userDetail = rowData;
    }
  }

  headerReturn() {
    this.changePage('query');
  }

  formateDate(date: Date) {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'
      ? dayjs(date).format('YYYY-MM-DD HH:mm:ss')
      : '';
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

          // console.log('複製成功');
        })
        .catch((err) => {
          // console.error('複製失敗', err);

        });
    } else {
      // console.warn('無資料可複製');
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

   maskAllWithDots(input:string = '', maxlength?:number) {
    return input?.length? '●'.repeat(maxlength?maxlength:input.length) : '';
  }

  public get idpType() {
    return this.form.get('idpType');
  }
  public get clientId() {
    return this.form.get('clientId');
  }
  public get clientMima() {
    return this.form.get('clientMima');
  }
  public get clientName() {
    return this.form.get('clientName');
  }
  public get idpWellKnownUrl() {
    return this.form.get('idpWellKnownUrl');
  }
  public get callbackUrl() {
    return this.form.get('callbackUrl');
  }
  public get clientStatus() {
    return this.form.get('clientStatus');
  }
  public get authUrl() {
    return this.form.get('authUrl');
  }
  public get accessTokenUrl() {
    return this.form.get('accessTokenUrl');
  }
  public get scope() {
    return this.form.get('scope');
  }
}
