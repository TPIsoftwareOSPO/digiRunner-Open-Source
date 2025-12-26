import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { DPB0095Item, DPB0095Req } from 'src/app/models/api/OpenApiService/dpb0095.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import * as dayjs from 'dayjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DialogService } from 'primeng/dynamicdialog';
import { DPB0240Req, DPB0240RespItem } from 'src/app/models/api/ServerService/dpb0240.interface';
import { DPB0241Req, DPB0241Resp } from 'src/app/models/api/ServerService/dpb0241.interface';
import { DPB0242Req } from 'src/app/models/api/ServerService/dpb0242.interface';
import { DPB0243Req } from 'src/app/models/api/ServerService/dpb0243.interface';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-ac0231',
  templateUrl: './ac0231.component.html',
  styleUrls: ['./ac0231.component.scss'],
  providers: [MessageService, ConfirmationService]
})
export class Ac0231Component extends BaseComponent implements OnInit {

  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  formEdit!: FormGroup;
  clientListCols: { field: string; header: string; }[] = [];
  clientList: Array<DPB0095Item> = [];
  currentClient?: DPB0095Item;
  currentAction: string = '';
  gtwIdpList: Array<DPB0240RespItem> = [];
  gtwIdPInfo?: DPB0241Resp;

  _fileSrc: any = null;

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
      keyword_client: new FormControl(''),
      status_client: new FormControl('A'),
    });
    this.formEdit = this.fb.group({
      id: new FormControl({ value: '', disabled: true }),
      clientId: new FormControl({ value: '', disabled: true }),
      status: new FormControl(''),
      cusLoginUrl: new FormControl(''),
      cusUserDataUrl: new FormControl(''),

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
  defaultColor() {
    const svgPanel: any = document.querySelector('.svg-panel');
    svgPanel.classList.remove('step02','step04')
  }

  focusStep(step) {
    const svgPanel: any = document.querySelector('.svg-panel');
    svgPanel.classList.add(step)
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
      this.currentTitle = `${this.title} > GTW CUS IdP Client List`
    }
    else
      this.changePage('queryClientList')
  }

  viewGtwIdPinfoList(rowData: DPB0095Item) {
    this.currentClient = rowData;
    // console.log(rowData)
    this.status_client?.setValue('A');
    this.queryGtwIdPInfoByClientId_cus();
  }

  queryGtwIdPInfoByClientId_cus() {
    this.pageNum = 2;
    this.currentTitle = `${this.title} > GTW CUS IdP Client List`
    let reqBody = {
      clientId: this.currentClient?.clientId,
      keyword: this.keyword_client?.value,
      status: this.status_client?.value == 'A'? null: this.status_client?.value,
    } as DPB0240Req
    this.gtwIdpList = [];
    this.serverService.queryGtwIdPInfoByClientId_cus(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.gtwIdpList = res.RespBody.infoList;
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

  moreClientDetail (){
    let reqBody = {
      clientId: this.currentClient?.clientId,
      keyword: this.keyword_client?.value,
      status: this.status_client?.value == 'A'? null: this.status_client?.value,
      gtwIdpInfoCusId: this.gtwIdpList[this.gtwIdpList.length-1].gtwIdpInfoCusId
    } as DPB0240Req

    this.serverService.queryGtwIdPInfoByClientId_cus(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.gtwIdpList = this.gtwIdpList.concat(res.RespBody.infoList);
      }
    })
  }

  async changePage(action: string, rowData?: DPB0240RespItem) {
    const codes = ['button.detail', 'button.create', 'button.update', 'cfm_del', 'message.delete', 'message.success'];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);

    // this.resetFormValidator(this.form);
    this.currentAction = action;
    this.formEdit.enable();
    this.clearFile();

    switch (action) {
      case 'queryClientList':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'clientList':
        this.currentTitle = `${this.title} > GTW CUS IdP Client List`
        this.pageNum = 2;
        break;
      case 'detail':
        let reqDetail = {
          gtwIdpInfoCusId: rowData?.gtwIdpInfoCusId,
          clientId: rowData?.clientId
        } as DPB0241Req;
        this.serverService.queryGtwIdPInfoDetail_cus(reqDetail).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.detail']}`
            this.pageNum = 4;
            this.gtwIdPInfo = res.RespBody;
            this.formEdit.disable();
            this.id.setValue(res.RespBody.gtwIdpInfoCusId);
            this.clientId.setValue(res.RespBody.clientId);
            this.status.setValue(res.RespBody.status);


            this.cusLoginUrl.setValue(res.RespBody.cusLoginUrl);
            this.cusUserDataUrl.setValue(res.RespBody.cusUserDataUrl);

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
        this.serverService.createGtwIdPInfo_cus_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.create']}`
            this.pageNum = 4;

            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.clientId?.setValue(this.currentClient?.clientId);
            this.clientId?.disable();
            this.status?.markAsTouched();
          }
        })
        break;
      case 'update':
        let reqD = {
          gtwIdpInfoCusId: rowData?.gtwIdpInfoCusId,
          clientId: rowData?.clientId
        } as DPB0241Req;
        this.serverService.queryGtwIdPInfoDetail_cus(reqD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {

            this.gtwIdPInfo = res.RespBody;
            // console.log(this.gtwIdPInfo)

            this.serverService.updateGtwIdPInfo_cus_before().subscribe(resValid => {
              if (this.toolService.checkDpSuccess(resValid.ResHeader)) {
                this.currentTitle += `> ${dict['button.update']}`
                this.pageNum = 4;
                this.addFormValidator(this.formEdit, resValid.RespBody.constraints);

                this.id.setValue(res.RespBody.gtwIdpInfoCusId);
                this.id.disable();
                this.clientId.setValue(res.RespBody.clientId);
                this.clientId.disable();
                this.status.setValue(res.RespBody.status);


                this.cusLoginUrl.setValue(res.RespBody.cusLoginUrl);
                this.cusUserDataUrl.setValue(res.RespBody.cusUserDataUrl);

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
          message: `${rowData?.gtwIdpInfoCusId}`,
          accept: () => {

            this.serverService.deleteGtwIdPInfo_cus({ gtwIdpInfoCusId: rowData!.gtwIdpInfoCusId, clientId:rowData!.clientId}).subscribe(async res => {

              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({
                  severity: 'success', summary: `${dict['message.delete']} GTW CUS IdP Client List`,
                  detail: `${dict['message.delete']} ${dict['message.success']}!`
                });
                this.queryGtwIdPInfoByClientId_cus();
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
      cusLoginUrl: this.cusLoginUrl.value,
      cusUserDataUrl: this.cusUserDataUrl.value,
      pageTitle: this.pageTitle.value
    } as DPB0242Req;


    if (this._fileSrc) reqCreate.iconFile = this._fileSrc

    this.serverService.createGtwIdPInfo_cus(reqCreate).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} GTW CUS IdP Client List`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.queryGtwIdPInfoByClientId_cus();
      }
    })
  }

  async update() {

    let reqbody = {
      gtwIdpInfoCusId: this.id.value,
      clientId: this.clientId.value,
      status: this.status.value,
      cusLoginUrl: this.cusLoginUrl.value,
      cusUserDataUrl: this.cusUserDataUrl.value,
      pageTitle: this.pageTitle.value
    } as DPB0243Req;

    if (this._fileSrc) reqbody.iconFile = this._fileSrc

    this.serverService.updateGtwIdPInfo_cus(reqbody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} GTW CUS IdP Client List`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.queryGtwIdPInfoByClientId_cus();
      }
    })

  }


  public get keyword() { return this.form.get('keyword'); };
  public get keyword_client() { return this.form.get('keyword_client'); };
  public get status_client() { return this.form.get('status_client'); };

  public get id() { return this.formEdit.get('id')!; };

  public get clientId() { return this.formEdit.get('clientId')!; };
  public get status() { return this.formEdit.get('status')!; };

  public get cusLoginUrl() { return this.formEdit.get('cusLoginUrl')!; };
  public get cusUserDataUrl() { return this.formEdit.get('cusUserDataUrl')!; };

  public get iconFile() { return this.formEdit.get('iconFile')!; };
  public get pageTitle() { return this.formEdit.get('pageTitle')!; };

  public get createUser() { return this.formEdit.get('createUser')!; };
  public get createDateTime() { return this.formEdit.get('createDateTime')!; };
  public get updateUser() { return this.formEdit.get('updateUser')!; };
  public get updateDateTime() { return this.formEdit.get('updateDateTime')!; };

}
