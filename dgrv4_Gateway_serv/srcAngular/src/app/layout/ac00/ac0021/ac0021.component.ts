import { generate } from 'generate-password';
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
import { DPB0220RespItem } from 'src/app/models/api/ServerService/dpb0220.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0222Req } from 'src/app/models/api/ServerService/dpb0222.interface';
import { DPB0221Req, DPB0221Resp } from 'src/app/models/api/ServerService/dpb0221.interface';
import { DPB0223Req } from 'src/app/models/api/ServerService/dpb0223.interface';

@Component({
  selector: 'app-ac0021',
  templateUrl: './ac0021.component.html',
  styleUrls: ['./ac0021.component.scss'],
  providers: [MessageService, ConfirmationService],
})
export class Ac0021Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  formEdit!: FormGroup;

  currentAction: string = '';
  cusInfo?: DPB0221Resp;

  tableData: Array<DPB0220RespItem> = [];
  cusStatusData: { label: string; value: string }[] = [];
  urlExp: String = '';
  isShowBtnSVG: boolean=false;

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
    private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);

    this.form = this.fb.group({
      cusId: new FormControl(''),
      keyword: new FormControl(''),
      cusStatus: new FormControl(''),
    });

    this.formEdit = this.fb.group({
      cusId: new FormControl(''),
      cusName: new FormControl(''),
      cusStatus: new FormControl(''),
      cusLoginUrl: new FormControl(''),
      cusBackendLoginUrl: new FormControl(''),
      cusUserDataUrl: new FormControl(''),
      createDateTime: new FormControl(''),
      createUser: new FormControl(''),
      updateDateTime: new FormControl(''),
      updateUser: new FormControl(''),
    });
  }

  async ngOnInit() {
    const codes = ['button.enable', 'button.disable', 'all', 'cusUrlExp'];
    const dict = await this.toolService.getDict(codes);
    this.urlExp = dict['cusUrlExp'];
    this.cusStatusData = [
      { label: dict['button.enable'], value: 'Y' },
      { label: dict['button.disable'], value: 'N' },
      { label: dict['all'], value: '' },
    ];

    this.axios_queryIdPInfoList_cus();
  }

  axios_queryIdPInfoList_cus() {
    if (this.pageNum !== 1) this.pageNum = 1;
    this.currentTitle = this.title;

    this.ngxSrvice.start();
    this.serverService
      .queryIdPInfoList_cus({
        cusId: this.q_cusId.value,
        keyword: this.keyword.value,
        cusStatus: this.q_cusStatus.value == '' ? null : this.q_cusStatus.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.infoList;
        }
        this.ngxSrvice.stop();
      });
  }

  headerReturn() {
    this.changePage('default');
  }
  defaultColor() {
    const svgPanel: any = document.querySelector('.svg-panel');
    svgPanel.classList.remove('step02','step04','step08')
  }

  focusStep(step) {
    const svgPanel: any = document.querySelector('.svg-panel');
    svgPanel.classList.add(step)
  }
  async changePage(action: string, rowData?: DPB0220RespItem) {
    const codes = [
      'button.detail',
      'button.create',
      'button.update',
      'cfm_del',
      'message.delete',
      'message.success',
      'cusName'
    ];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);
    this.currentAction = action;
    this.formEdit.enable();

    switch (action) {
      case 'default':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.serverService.createIdPInfo_cus_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.currentTitle += `> ${dict['button.create']}`;
            this.pageNum = 2;
          }
        });
        break;
      case 'detail':
        let req = {
          cusId: rowData?.cusId
        } as DPB0221Req;
        this.serverService.queryIdPInfoDetail_cus(req).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.detail']}`
            this.pageNum = 2;
            this.cusInfo = res.RespBody;
            this.formEdit.disable();
            this.cusId.setValue(res.RespBody.cusId);
            this.cusName.setValue(res.RespBody.cusName);
            this.cusStatus.setValue(res.RespBody.cusStatus);
            this.cusLoginUrl.setValue(res.RespBody.cusLoginUrl);
            this.cusBackendLoginUrl.setValue(res.RespBody.cusBackendLoginUrl);
            this.cusUserDataUrl.setValue(res.RespBody.cusUserDataUrl);
            this.createDateTime.setValue(res.RespBody.createDateTime? this.toolService.setformate(new Date(res.RespBody.createDateTime), 'YYYY-MM-DD HH:mm:ss') : '');
            this.createUser.setValue(res.RespBody.createUser);
            this.updateDateTime.setValue(res.RespBody.updateDateTime? this.toolService.setformate(new Date(res.RespBody.updateDateTime), 'YYYY-MM-DD HH:mm:ss') : '');
            this.updateUser.setValue(res.RespBody.updateUser);
          }
        })
        break;
      case 'update':
        let reqD = {
          cusId: rowData?.cusId
        } as DPB0221Req;
        this.serverService.queryIdPInfoDetail_cus(reqD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.cusInfo = res.RespBody;
            this.serverService.updateIdPInfo_cus_before().subscribe(resValid=> {
              if (this.toolService.checkDpSuccess(resValid.ResHeader)) {
                this.addFormValidator(this.formEdit, resValid.RespBody.constraints);
                this.currentTitle += `> ${dict['button.update']}`
                this.pageNum = 2;
                this.cusId.setValue(res.RespBody.cusId);
                this.cusName.setValue(res.RespBody.cusName);
                this.cusStatus.setValue(res.RespBody.cusStatus);
                this.cusLoginUrl.setValue(res.RespBody.cusLoginUrl);
                this.cusBackendLoginUrl.setValue(res.RespBody.cusBackendLoginUrl);
                this.cusUserDataUrl.setValue(res.RespBody.cusUserDataUrl);
                this.createDateTime.setValue(res.RespBody.createDateTime? this.toolService.setformate(new Date(res.RespBody.createDateTime), 'YYYY-MM-DD HH:mm:ss') : '');
                this.createUser.setValue(res.RespBody.createUser);
                this.updateDateTime.setValue(res.RespBody.updateDateTime? this.toolService.setformate(new Date(res.RespBody.updateDateTime), 'YYYY-MM-DD HH:mm:ss') : '');
                this.updateUser.setValue(res.RespBody.updateUser);
              }
            })

          }
        })

        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${dict['cusName']}: ${rowData?.cusName}`,
          accept: () => {
            this.serverService
              .deleteIdPInfo_cus({ cusId: rowData!.cusId })
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']} AC API IdP`,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.axios_queryIdPInfoList_cus();
                }
              });
          },
        });
        break;
    }
  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'
      ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss')
      : '';
  }

  async create() {
    let req = {
      cusName: this.cusName.value,
      cusStatus: this.cusStatus.value,
      cusLoginUrl: this.cusLoginUrl.value,
      cusBackendLoginUrl: this.cusBackendLoginUrl.value,
      cusUserDataUrl: this.cusUserDataUrl.value,
    } as DPB0222Req;

    this.serverService.createIdPInfo_cus(req).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.create']} AC CUS Idp`,
          detail: `${dict['message.create']} ${dict['message.success']}!`,
        });
        this.axios_queryIdPInfoList_cus();
      }
    });
  }

  async update() {
    let req = {
      cusId: this.cusInfo?.cusId,
      cusName: this.cusName.value,
      cusStatus: this.cusStatus.value,
      cusLoginUrl: this.cusLoginUrl.value,
      cusBackendLoginUrl: this.cusBackendLoginUrl.value,
      cusUserDataUrl: this.cusUserDataUrl.value,
    } as DPB0223Req;

    this.serverService.updateIdPInfo_cus(req).subscribe(async (res)=>{
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.update', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success', summary: `${dict['message.update']} AC CUS IdP`,
              detail: `${dict['message.update']} ${dict['message.success']}!`
            });

            this.axios_queryIdPInfoList_cus();
          }
    })

  }

  moreData() {
    this.ngxSrvice.start();
    this.serverService
      .queryIdPInfoList_cus({
        cusId: this.tableData[this.tableData.length-1].cusId,
        keyword: this.keyword.value,
        cusStatus: this.q_cusStatus.value == '' ? null : this.q_cusStatus.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = this.tableData.concat( res.RespBody.infoList);
        }
        this.ngxSrvice.stop();
      });
  }

  public get q_cusId() {
    return this.form.get('cusId')!;
  }
  public get q_cusStatus() {
    return this.form.get('cusStatus')!;
  }
  public get keyword() {
    return this.form.get('keyword')!;
  }

  public get cusId() {
    return this.formEdit.get('cusId')!;
  }
  public get cusName() {
    return this.formEdit.get('cusName')!;
  }
  public get cusStatus() {
    return this.formEdit.get('cusStatus')!;
  }
  public get cusLoginUrl() {
    return this.formEdit.get('cusLoginUrl')!;
  }
  public get cusBackendLoginUrl() {
    return this.formEdit.get('cusBackendLoginUrl')!;
  }
  public get cusUserDataUrl() {
    return this.formEdit.get('cusUserDataUrl')!;
  }
  public get createDateTime() {
    return this.formEdit.get('createDateTime')!;
  }
  public get createUser() {
    return this.formEdit.get('createUser')!;
  }
  public get updateDateTime() {
    return this.formEdit.get('updateDateTime')!;
  }
  public get updateUser() {
    return this.formEdit.get('updateUser')!;
  }
}
