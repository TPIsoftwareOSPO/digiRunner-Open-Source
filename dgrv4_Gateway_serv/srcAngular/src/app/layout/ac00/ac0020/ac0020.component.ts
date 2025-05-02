import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0195RespItem } from 'src/app/models/api/ServerService/dpb0195.interface';
import * as dayjs from 'dayjs';
import { DPB0197Req } from 'src/app/models/api/ServerService/dpb0197.interface';
import { DPB0196Req, DPB0196Resp } from 'src/app/models/api/ServerService/dpb0196.interface';
import { DPB0198Req } from 'src/app/models/api/ServerService/dpb0198.interface';
import { KeyValueFormComponent } from '../../ac02/ac0228/key-value-form/key-value-form.component';

@Component({
  selector: 'app-ac0020',
  templateUrl: './ac0020.component.html',
  styleUrls: ['./ac0020.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class Ac0020Component extends BaseComponent implements OnInit {

  @ViewChild("keyValueComp") keyValueComp!: KeyValueFormComponent;

  currentTitle = this.title;
  pageNum: number = 1;
  formEdit!: FormGroup;
  dataList: Array<DPB0195RespItem> = [];
  currentAction: string = '';
  apiInfo?: DPB0196Resp;

  methodsList: { label: string; value: string }[] = [
    { value: 'POST', label: 'POST' },
    { value: 'GET', label: 'GET' }
  ];
  _fileSrc: any = null;

  bodyType: { label: string; value: string }[] = [
    { value: 'N', label: 'none' },
    { value: 'F', label: 'form-data' },
    { value: 'X', label: 'x-www-form-urlencoded' },
    { value: 'R', label: 'raw' },
  ];

  respType: { label: string; value: string }[] = [
    { value: 'H', label: 'HTTP status' },
    { value: 'R', label: 'HTTP status + return code' }
  ];

  idTokenExp: string = `{
    "at_hash": "FOiDD5zzh0sLqMMYGkovbw",
    "aud": "aud",
    "sub": "user_sub",
    "iss": "https://domain/dgrv4/ssotoken/API",
    "name": "{{$user.name_th%}} {{$user.name_en%}}",
    "exp": 1690611646,
    "iat": 1685427646,
    "email": "{{$user.email%}}",
    "picture": "{{$user.picture%}}"
  }`;

  rawExp: string = `{
    "username": "{{$username%}}",
    "password": "{{$password%}}",
    "uid": "{{$ip%}}",
    "check": "Y"
}`;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private openApiService: OpenApiKeyService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
  ) {
    super(route, tr);
  }

  ngOnInit(): void {

    this.formEdit = this.fb.group({
      id: new FormControl({ value: '', disabled: true }),
      // clientId: new FormControl({ value: '', disabled: true }),
      status: new FormControl(''),
      approvalResultMail: new FormControl(''),
      apiMethod: new FormControl(''),
      apiUrl: new FormControl(''),
      reqHeader: new FormControl(''),
      reqBodyType: new FormControl(''),
      reqBody: new FormControl(''),
      sucByType: new FormControl(''),
      sucByField: new FormControl(''),
      sucByValue: new FormControl(''),
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

    this.axios_queryIdPInfoList_api();

  }


  axios_queryIdPInfoList_api() {
    if (this.pageNum !== 1) this.pageNum = 1;
    this.currentTitle = this.title;

    this.serverService.queryIdPInfoList_api({}).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
      }
    })
  }

  headerReturn() {
    this.changePage('clientList')
  }

  async changePage(action: string, rowData?: DPB0195RespItem) {
    const codes = ['button.detail', 'button.create', 'button.update', 'cfm_del', 'message.delete', 'message.success'];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);
    this.currentAction = action;
    this.formEdit.enable();
    this.clearFile();

    switch (action) {
      case 'clientList':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.serverService.createIdPInfo_api_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.create']}`
            this.pageNum = 2;
            this.addFormValidator(this.formEdit, res.RespBody.constraints);

            this.status?.markAsTouched();
            this.sucByType.markAsTouched();
            this.reqBodyType.markAsTouched();

            this.sucByType.valueChanges.subscribe(res => {
              if (res === 'R') {
                const validStr = [
                  { field: "sucByField", type: "string", isRequired: { msg: "必填", value: true } },
                  { field: "sucByValue", type: "string", isRequired: { msg: "必填", value: true } },
                ];
                this.addFormValidator(this.formEdit, validStr)
              }
              else {
                this.sucByField.clearValidators();
                this.sucByField.updateValueAndValidity();
                this.sucByValue.clearValidators();
                this.sucByValue.updateValueAndValidity();
              }
              this.formEdit.updateValueAndValidity();
            })

            this.reqBodyType.valueChanges.subscribe((res) => {
              this.reqBody.setValue('');
              if(res ==='F' || res === 'X'){
                this.keyValueComp.addKeyValue();
              }
            })

          }
        })
        break;
      case 'detail':
        let reqDetail = {
          id: rowData?.id
        } as DPB0196Req;
        this.serverService.queryIdPInfoDetail_api(reqDetail).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.detail']}`
            this.pageNum = 2;
            this.apiInfo = res.RespBody;
            this.formEdit.disable();
            this.id.setValue(res.RespBody.id);
            // this.clientId.setValue(res.RespBody.clientId);
            this.status.setValue(res.RespBody.status);
            this.approvalResultMail.setValue(res.RespBody.approvalResultMail);
            this.apiMethod.setValue(res.RespBody.apiMethod);
            this.apiUrl.setValue(res.RespBody.apiUrl);
            this.reqHeader.setValue(res.RespBody.reqHeader);
            this.reqBodyType.setValue(res.RespBody.reqBodyType);
            if (this.reqBodyType.value === 'F' || this.reqBodyType.value === 'X') {
              this.reqBody.disable();
            }
            this.reqBody.setValue(res.RespBody.reqBody);
            this.sucByType.setValue(res.RespBody.sucByType);
            this.sucByField.setValue(res.RespBody.sucByField);
            this.sucByValue.setValue(res.RespBody.sucByValue);
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
      case 'update':
        let reqD = {
          id: rowData?.id
        } as DPB0196Req;
        this.serverService.queryIdPInfoDetail_api(reqD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {

            this.apiInfo = res.RespBody;
            // console.log(this.gtwIdPInfo)

            this.serverService.updateIdPInfo_api_before().subscribe(resValid => {
              if (this.toolService.checkDpSuccess(resValid.ResHeader)) {
                this.currentTitle += `> ${dict['button.update']}`
                this.pageNum = 2;
                this.addFormValidator(this.formEdit, resValid.RespBody.constraints);

                this.id.setValue(res.RespBody.id);
                this.id.disable();

                this.status.setValue(res.RespBody.status);
                this.approvalResultMail.setValue(res.RespBody.approvalResultMail);
                this.apiMethod.setValue(res.RespBody.apiMethod);
                this.apiUrl.setValue(res.RespBody.apiUrl);
                this.reqHeader.setValue(res.RespBody.reqHeader);
                this.reqBodyType.setValue(res.RespBody.reqBodyType);
                this.reqBody.setValue(res.RespBody.reqBody);
                this.sucByType.setValue(res.RespBody.sucByType);
                this.sucByField.setValue(res.RespBody.sucByField);
                this.sucByValue.setValue(res.RespBody.sucByValue);
                this.idtName.setValue(res.RespBody.idtName);
                this.idtEmail.setValue(res.RespBody.idtEmail);
                this.idtPicture.setValue(res.RespBody.idtPicture);

                // console.log(res.RespBody.iconFile)
                if (res.RespBody.iconFile) this._fileSrc = res.RespBody.iconFile;
                this.pageTitle.setValue(res.RespBody.pageTitle);

                this.sucByType.valueChanges.subscribe(res_sucByType => {
                  if (res_sucByType === 'R') {
                    const validStr = [
                      { field: "sucByField", type: "string", isRequired: { msg: "必填", value: true } },
                      { field: "sucByValue", type: "string", isRequired: { msg: "必填", value: true } },
                    ];
                    this.addFormValidator(this.formEdit, validStr)
                    this.sucByField.setValue(res.RespBody.sucByField ? res.RespBody.sucByField : '');
                    this.sucByField.markAsTouched();
                    this.sucByValue.setValue(res.RespBody.sucByField ? res.RespBody.sucByField : '');
                    this.sucByValue.markAsTouched();
                    // console.log(this.sucByField)

                  }
                  else {
                    this.sucByField.clearValidators();
                    this.sucByField.updateValueAndValidity();
                    this.sucByValue.clearValidators();
                    this.sucByValue.updateValueAndValidity();
                  }
                  this.formEdit.updateValueAndValidity();
                })

                this.reqBodyType.valueChanges.subscribe((res) => {
                  this.reqBody.setValue('');
                  if(res ==='F' || res === 'X'){
                    this.keyValueComp.addKeyValue();
                  }
                })

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

            this.serverService.deleteIdPInfo_api({ id: rowData!.id }).subscribe(async res => {

              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({
                  severity: 'success', summary: `${dict['message.delete']} AC API IdP List`,
                  detail: `${dict['message.delete']} ${dict['message.success']}!`
                });
                this.axios_queryIdPInfoList_api();
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

        this._fileSrc = fileReader.result!;


      }
      fileReader.readAsDataURL(files.item(0)!);
    }
    else {
      this._fileSrc = null;
    }
  }

  async create() {

    const code = ['header_required', 'body_keyvalue_required', 'body_raw_required'];
    const dict = await this.toolService.getDict(code);

    // 檢查request header 欄位有無空白

    if (this.reqHeader.value) {
      // console.log('header', this.reqHeader.value)
      const checkReqHeaderData = JSON.parse(this.reqHeader.value)
      const checkReqHeader = checkReqHeaderData.every(headerData => {
        return (Object.keys(headerData).map(key => {
          return key != '' && headerData[key] != ''
        }))[0];
      });

      if (!checkReqHeader) {
        this.alertService.ok(dict['header_required'], '');
        return;
      }
    }

    // 檢查request body 欄位有無空白
    if (this.reqBodyType.value !== 'N') {
      if (!this.reqBody.value || this.reqBody.value === '') {
        this.reqBodyType.value === 'R' ? this.alertService.ok(dict['body_raw_required'], '') : this.alertService.ok(dict['body_keyvalue_required'], '')
        return;
      }

      if (this.reqBodyType.value == 'F' || this.reqBodyType.value == 'X') {
        const checkReqBodyData = JSON.parse(this.reqBody.value)
        const checkReqBody = checkReqBodyData.every(headerData => {
          return (Object.keys(headerData).map(key => {
            return key != '' && headerData[key] != ''
          }))[0];
        });

        if (!checkReqBody) {
          this.alertService.ok(dict['body_keyvalue_required'], '');
          return;
        }
      }
    }

    let reqCreate = {
      status: this.status.value,
      approvalResultMail: this.approvalResultMail.value,
      apiMethod: this.apiMethod.value,
      apiUrl: this.apiUrl.value,
      reqBodyType: this.reqBodyType.value,
      sucByType: this.sucByType.value,
      idtName: this.idtName.value,
      idtEmail: this.idtEmail.value,
      idtPicture: this.idtPicture.value,
      pageTitle: this.pageTitle.value
    } as DPB0197Req;

    // console.log(this.reqHeader.value)
    if (this.reqHeader.value && this.reqHeader.value !== '') reqCreate.reqHeader = this.reqHeader.value;
    if (this.reqBodyType.value !== 'N') reqCreate.reqBody = this.reqBody.value;
    if (this.sucByType.value === 'R') {
      reqCreate.sucByField = this.sucByField.value;
      reqCreate.sucByValue = this.sucByValue.value;
    }

    if (this._fileSrc) reqCreate.iconFile = this._fileSrc

    this.serverService.createIdPInfo_api(reqCreate).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} AC API Idp List`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.axios_queryIdPInfoList_api();
      }
    })
  }


  async update() {

    const code = ['header_required', 'body_keyvalue_required', 'body_raw_required'];
    const dict = await this.toolService.getDict(code);

    // 檢查request header 欄位有無空白

    if (this.reqHeader.value) {
      const checkReqHeaderData = JSON.parse(this.reqHeader.value)
      const checkReqHeader = checkReqHeaderData.every(headerData => {
        return (Object.keys(headerData).map(key => {
          return key != '' && headerData[key] != ''
        }))[0];
      });

      if (!checkReqHeader) {
        this.alertService.ok(dict['header_required'], '');
        return;
      }
    }

    // 檢查request body 欄位有無空白
    if (this.reqBodyType.value !== 'N') {
      if (!this.reqBody.value || this.reqBody.value === '') {
        this.reqBodyType.value === 'R' ? this.alertService.ok(dict['body_raw_required'], '') : this.alertService.ok(dict['body_keyvalue_required'], '')
        return;
      }

      if (this.reqBodyType.value == 'F' || this.reqBodyType.value == 'X') {
        const checkReqBodyData = JSON.parse(this.reqBody.value)
        const checkReqBody = checkReqBodyData.every(headerData => {
          return (Object.keys(headerData).map(key => {
            return key != '' && headerData[key] != ''
          }))[0];
        });

        if (!checkReqBody) {
          this.alertService.ok(dict['body_keyvalue_required'], '');
          return;
        }
      }
    }

    let reqbody = {
      id: this.id.value,

      status: this.status.value,
      approvalResultMail: this.approvalResultMail.value,
      apiMethod: this.apiMethod.value,
      apiUrl: this.apiUrl.value,
      reqBodyType: this.reqBodyType.value,
      sucByType: this.sucByType.value,
      idtName: this.idtName.value,
      idtEmail: this.idtEmail.value,
      idtPicture: this.idtPicture.value,
      pageTitle: this.pageTitle.value
    } as DPB0198Req;

    if (this.reqHeader.value && this.reqHeader.value !== '') reqbody.reqHeader = this.reqHeader.value;
    if (this.reqBodyType.value !== 'N') reqbody.reqBody = this.reqBody.value;
    if (this.sucByType.value === 'R') {
      reqbody.sucByField = this.sucByField.value;
      reqbody.sucByValue = this.sucByValue.value;
    }

    if (this._fileSrc) reqbody.iconFile = this._fileSrc

    this.serverService.updateIdPInfo_api(reqbody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} AC API IdP List`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.axios_queryIdPInfoList_api();
      }
    })

  }

  public get id() { return this.formEdit.get('id')!; };
  // public get clientId() { return this.formEdit.get('clientId')!; };
  public get status() { return this.formEdit.get('status')!; };
  public get approvalResultMail() { return this.formEdit.get('approvalResultMail')!; };
  public get apiMethod() { return this.formEdit.get('apiMethod')!; };
  public get apiUrl() { return this.formEdit.get('apiUrl')!; };
  public get reqHeader() { return this.formEdit.get('reqHeader')!; };
  public get reqBodyType() { return this.formEdit.get('reqBodyType')!; };
  public get reqBody() { return this.formEdit.get('reqBody')!; };
  public get sucByType() { return this.formEdit.get('sucByType')!; };
  public get sucByField() { return this.formEdit.get('sucByField')!; };
  public get sucByValue() { return this.formEdit.get('sucByValue')!; };
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
