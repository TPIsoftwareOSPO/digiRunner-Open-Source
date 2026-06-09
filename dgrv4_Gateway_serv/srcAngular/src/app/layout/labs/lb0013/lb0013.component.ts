import { map } from 'rxjs';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import {
  DPB0174Req,
  DPB0174RespItem,
} from 'src/app/models/api/ServerService/dpb0174.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0176Req } from 'src/app/models/api/ServerService/dpb0176.interface';
import { DPB0175Req } from 'src/app/models/api/ServerService/dpb0175.interface';
import { DPB0177Req } from 'src/app/models/api/ServerService/dpb0177.interface';
import { DPB0175Resp } from 'src/app/models/api/ServerService/dpb0175.interface';
import { DPB0178Req } from 'src/app/models/api/ServerService/dpb0178.interface';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertType, TxID } from 'src/app/models/common.enum';
import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';
import {
  SmartOnFhirProxyDto,
  SmartOnFhirProxySearchReq,
} from 'src/app/models/api/ServerService/dpb0310.interface';
import { TranslateService } from '@ngx-translate/core';
import { SmartOnFhirProxyDiversionDto } from 'src/app/models/api/ServerService/dpb0311.interface';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { Dialog } from 'primeng/dialog';
import { DialogService } from 'primeng/dynamicdialog';
import { ClientInfoListComponent } from './client-info-list/client-info-list.component';
import { SmartOnFhirProxyUpdateDto } from 'src/app/models/api/ServerService/dpb0312.interface';
import { DPB0313ReqItem } from 'src/app/models/api/ServerService/dpb0313.interface';
import {
  StreamApiService,
  StreamResponse,
} from '../../ac03/ac0316/api-test/stream-api.service';
import { environment } from 'src/environments/environment';
import { ReqDPB0314 } from 'src/app/models/api/ServerService/dpb0314.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { StickyListComponent } from './sticky-list/sticky-list.component';

interface _SmartOnFhirProxyDto extends SmartOnFhirProxyDto {
  result?: string;
}

@Component({
  selector: 'app-lb0013',
  templateUrl: './lb0013.component.html',
  styleUrls: ['./lb0013.component.css'],
  providers: [ConfirmationService],
  standalone: false,
})
export class Lb0013Component extends BaseComponent implements OnInit {
  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  formE!: FormGroup;
  cols: { field: string; header: string }[] = [];
  tableData: Array<SmartOnFhirProxyDto> = [];
  currentAction: string = '';
  totalElements: number = 0;
  currentPage: number = 0;
  btnName: string = '';
  selectedItem?: SmartOnFhirProxyDto;

  _formValid: boolean = false;
  // fileName: string = '';
  // file: any = null;

  exportProcessPercent: string = '';
  exportStatus: string = '';

  importFile?: any;
  importFileName: string = '';
  importData: _SmartOnFhirProxyDto[] = [];
  importSelected: _SmartOnFhirProxyDto[] = [];
  // formFile!: FormGroup;
  reload: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private serverService: ServerService,
    private toolService: ToolService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private alertService: AlertService,
    private ngxService: NgxUiLoaderService,
    private translate: TranslateService,
    private dialogService: DialogService,
    private streamApi: StreamApiService,
    private api: ApiBaseService,
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.form = this.fb.group({
      keyword: new FormControl(''),
    });
    this.formE = this.fb.group({
      sofProxyId: new FormControl(''),
      sofProxyName: new FormControl('', ValidatorFns.requiredValidator()),
      sofProxyStatus: new FormControl(''),
      sofProxyStatusName: new FormControl(''),
      sofProxyRemark: new FormControl(''),
      sofProxyAccessToken: new FormControl(''),
      sofProxySqlInjection: new FormControl(''),
      sofProxyTraffic: new FormControl(''),
      sofProxyXss: new FormControl(''),
      sofProxyXxe: new FormControl(''),
      sofProxyTps: new FormControl(''),
      sofProxyIgnoreApi: new FormControl(''),
      sofProxyClientId: new FormControl([]),
      sofProxyShowLog: new FormControl(''),
      sofProxySticky: new FormControl(''),
      diversionList: new FormControl(''),
      createDateTime: new FormControl(''),
      createUser: new FormControl(''),
      updateDateTime: new FormControl(''),
      updateUser: new FormControl(''),
      version: new FormControl(''),
    });

    // this.formFile = this.fb.group({
    //   file: new FormControl(),
    //   fileName: new FormControl({ value: '', disabled: true }),
    // });

    this.cols = [
      { field: 'sofProxyStatus', header: this.translate.instant('sof.status') },
      { field: 'sofProxyId', header: 'ID' },
      { field: 'sofProxyName', header: this.translate.instant('sof.name') },
      { field: 'sofProxyRemark', header: this.translate.instant('memo') },
      {
        field: 'sofProxyAccessToken',
        header: this.translate.instant('sof.access_token'),
      },
      {
        field: 'sofProxySqlInjection',
        header: this.translate.instant('sof.sql_injection'),
      },
      {
        field: 'sofProxyTraffic',
        header: this.translate.instant('sof.traffic'),
      },
      { field: 'sofProxyXss', header: this.translate.instant('sof.xss') },
      { field: 'sofProxyXxe', header: this.translate.instant('sof.xxe') },
      {
        field: 'sofProxyShowLog',
        header: this.translate.instant('sof.show_log'),
      },
      {
        field: 'sofProxySticky',
        header: this.translate.instant('sof.sticky'),
      },
      { field: 'sofProxyTps', header: 'TPS' },
      {
        field: 'sofProxyIgnoreApi',
        header: this.translate.instant('sof.ignore_api'),
      },
      {
        field: 'sofProxyClientId',
        header: this.translate.instant('sof.client_id'),
      },
      {
        field: 'diversionList',
        header: this.translate.instant('sof.diversion_list'),
      },
      {
        field: 'updateDateTime',
        header: this.translate.instant('update_time'),
      },
      { field: 'updateUser', header: this.translate.instant('update_user') },
    ];

    this.serverService
      .querySmartOnFhirProxyList_ignore1298({})
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.content;
          this.totalElements = res.RespBody.totalElements;
          this.currentPage = res.RespBody.number + 1;
        } else {
          this.tableData = [];
        }
      });

    this.sofProxyTps.valueChanges.subscribe((val) => {
      if (val == null) this.sofProxyTps.setValue('');
    });
  }

  querySmartOnFhirProxyList() {
    this.currentPage = 0;
    let ReqBody = {
      keywords: this.keyword.value
        ? this.keyword.value.toString().split(' ')
        : [],
    } as SmartOnFhirProxySearchReq;
    this.ngxService.start();
    this.serverService.querySmartOnFhirProxyList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
        this.totalElements = res.RespBody.totalElements;
        this.currentPage = res.RespBody.number + 1;
      } else {
        this.tableData = [];
      }
      this.ngxService.stop();
    });
  }

  getMoreData() {
    this.ngxService.start();
    this.serverService
      .querySmartOnFhirProxyList({
        pageNum: this.currentPage,
        pageSize: 20,
        sortOrder: 'desc',
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = this.tableData.concat(res.RespBody.content);
          this.totalElements = res.RespBody.totalElements;
          this.currentPage = res.RespBody.number + 1;
        } else {
          this.tableData = [];
        }
        this.ngxService.stop();
      });
  }

  headerReturn() {
    if (this.reload) {
      this.reload = false;
      this.querySmartOnFhirProxyList();
    }
    this.changePage('query');
  }

  async changePage(action: string, rowData?: SmartOnFhirProxyDto) {
    this.currentAction = action;
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
      'button.import',
    ];
    const dict = await this.toolService.getDict(code);
    this.formE.enable();
    // this.resetFormValidator(this.formE);
    this.formE.reset('');
    this.formE.clearValidators();
    this.formE.updateValueAndValidity();
    this.diversionList.setValue([]);
    this.sofProxyClientId.setValue([]);
    this.sofProxyClientId.disable();
    this._formValid = false;
    this.selectedItem = undefined;
    this.importFile = undefined;
    this.importFileName = '';
    this.importData = [];
    this.exportProcessPercent = '';
    this.exportStatus = '';

    switch (action) {
      case 'import':
        this.currentTitle = `${this.title} > ${dict['button.import']}`;
        this.pageNum = 3;
        break;
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];
        this.sofProxyStatus.setValue('N');
        this.sofProxyTps.setValue(0);

        break;

      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 2;
        this.btnName = dict['button.update'];
        this.selectedItem = rowData;

        this.sofProxyId.setValue(this.selectedItem!.sofProxyId);
        this.sofProxyName.setValue(this.selectedItem!.sofProxyName);
        this.sofProxyStatus.setValue(this.selectedItem!.sofProxyStatus);
        this.sofProxyRemark.setValue(this.selectedItem!.sofProxyRemark);
        this.sofProxyAccessToken.setValue(
          this.selectedItem!.sofProxyAccessToken,
        );
        this.sofProxySqlInjection.setValue(
          this.selectedItem!.sofProxySqlInjection,
        );
        this.sofProxyTraffic.setValue(this.selectedItem!.sofProxyTraffic);
        this.sofProxyXss.setValue(this.selectedItem!.sofProxyXss);
        this.sofProxyXxe.setValue(this.selectedItem!.sofProxyXxe);
        this.sofProxyShowLog.setValue(this.selectedItem!.sofProxyShowLog);
        this.sofProxySticky.setValue(this.selectedItem!.sofProxySticky);
        this.sofProxyTps.setValue(this.selectedItem!.sofProxyTps);
        this.sofProxyIgnoreApi.setValue(this.selectedItem!.sofProxyIgnoreApi);
        this.sofProxyClientId.setValue(this.selectedItem!.sofProxyClientId);

        this.diversionList.setValue(this.selectedItem!.diversionList);

        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.pageNum = 2;
        this.btnName = dict['button.detail'];
        this.selectedItem = rowData;

        this.formE.disable();
        this.sofProxyId.setValue(this.selectedItem!.sofProxyId);
        this.sofProxyName.setValue(this.selectedItem!.sofProxyName);
        this.sofProxyStatus.setValue(this.selectedItem!.sofProxyStatus);
        this.sofProxyRemark.setValue(this.selectedItem!.sofProxyRemark);
        this.sofProxyAccessToken.setValue(
          this.selectedItem!.sofProxyAccessToken,
        );
        this.sofProxySqlInjection.setValue(
          this.selectedItem!.sofProxySqlInjection,
        );
        this.sofProxyTraffic.setValue(this.selectedItem!.sofProxyTraffic);
        this.sofProxyXss.setValue(this.selectedItem!.sofProxyXss);
        this.sofProxyXxe.setValue(this.selectedItem!.sofProxyXxe);
        this.sofProxyShowLog.setValue(this.selectedItem!.sofProxyShowLog);
        this.sofProxySticky.setValue(this.selectedItem!.sofProxySticky);
        this.sofProxyTps.setValue(this.selectedItem!.sofProxyTps);
        this.sofProxyIgnoreApi.setValue(this.selectedItem!.sofProxyIgnoreApi);
        this.sofProxyClientId.setValue(this.selectedItem!.sofProxyClientId);

        this.diversionList.setValue(this.selectedItem!.diversionList);

        break;
      case 'delete':
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.deleteConfirm(rowData!);
          },
        });
        break;
      case 'sticky':
        const ref = this.dialogService.open(StickyListComponent, {
          data: {
            data: rowData ,
            showMessage: (summary:string = '', detail:string = '')=> this.showMessage(summary,detail)
          },
          header: 'Sticky List',
          styleClass: 'cHeader cContent cIcon',
          width: '700px',
          // height: '100vh'
        });

        ref?.onClose.subscribe((res) => {
          if (res) {
            this.sofProxyClientId!.setValue(
              res.map((row: any) => row.clientId),
            );
          } else {
            this.sofProxyClientId!.setValue([]);
          }
        });
        break;
    }
  }

  openStickyList(rowData: SmartOnFhirProxyDto) {
    const ref = this.dialogService.open(StickyListComponent, {
      data: { data: rowData },
      header: 'Sticky List',
      styleClass: 'cHeader cContent cIcon',
      width: '700px',
      // height: '100vh'
    });

        ref?.onClose.subscribe((res) => {
          if (res) {
            this.sofProxyClientId!.setValue(
              res.map((row: any) => row.clientId),
            );
          } else {
            this.sofProxyClientId!.setValue([]);
          }
        });
  }

  async procData() {
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);

    switch (this.currentAction) {
      case 'create':
        let reqBodyC = {
          sofProxyName: this.sofProxyName.value,
          sofProxyStatus: this.sofProxyStatus.value,
          sofProxyRemark: this.sofProxyRemark.value,
          sofProxyAccessToken:
            this.sofProxyAccessToken.value == 'Y' ? 'Y' : 'N',
          sofProxySqlInjection:
            this.sofProxySqlInjection.value == 'Y' ? 'Y' : 'N',
          sofProxyTraffic: this.sofProxyTraffic.value == 'Y' ? 'Y' : 'N',
          sofProxyXss: this.sofProxyXss.value == 'Y' ? 'Y' : 'N',
          sofProxyXxe: this.sofProxyXxe.value == 'Y' ? 'Y' : 'N',
          sofProxyShowLog: this.sofProxyShowLog.value == 'Y' ? 'Y' : 'N',
          sofProxySticky: this.sofProxySticky.value == 'Y' ? 'Y' : 'N',
          sofProxyTps: this.sofProxyTps.value,
          sofProxyIgnoreApi: this.sofProxyIgnoreApi.value,
          sofProxyClientId: this.sofProxyClientId.value,
          diversionList:
            this.diversionList.value?.map((row: any) => {
              return {
                sofProxyDiversionProbability: row.sofProxyDiversionProbability,
                sofProxyDiversionUrl: row.sofProxyDiversionUrl,
              } as SmartOnFhirProxyDiversionDto;
            }) ?? [],
        };

        this.serverService.addSmartOnFhirProxy(reqBodyC).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']}`,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.querySmartOnFhirProxyList();
          }
        });

        break;
      case 'update':
        let reqBodyU = {
          sofProxyId: this.sofProxyId.value,
          version: this.selectedItem?.version,
          sofProxyName: this.sofProxyName.value,
          sofProxyStatus: this.sofProxyStatus.value,
          sofProxyRemark: this.sofProxyRemark.value,

          sofProxyAccessToken:
            this.sofProxyAccessToken.value == 'Y' ? 'Y' : 'N',
          sofProxySqlInjection:
            this.sofProxySqlInjection.value == 'Y' ? 'Y' : 'N',
          sofProxyTraffic: this.sofProxyTraffic.value == 'Y' ? 'Y' : 'N',
          sofProxyXss: this.sofProxyXss.value == 'Y' ? 'Y' : 'N',
          sofProxyXxe: this.sofProxyXxe.value == 'Y' ? 'Y' : 'N',
          sofProxyShowLog: this.sofProxyShowLog.value == 'Y' ? 'Y' : 'N',
          sofProxySticky: this.sofProxySticky.value == 'Y' ? 'Y' : 'N',
          sofProxyTps: this.sofProxyTps.value,
          sofProxyIgnoreApi: this.sofProxyIgnoreApi.value,
          sofProxyClientId: this.sofProxyClientId.value,
          diversionList: this.diversionList.value?.map((row: any) => {
            return {
              sofProxyDiversionId: row.sofProxyDiversionId ?? '',
              version: row.version ?? '',
              sofProxyDiversionUrl: row.sofProxyDiversionUrl,
              sofProxyDiversionProbability: row.sofProxyDiversionProbability,
            } as SmartOnFhirProxyDiversionDto;
          }),
        } as SmartOnFhirProxyUpdateDto;

        this.serverService
          .batchUpdateSmartOnFhirProxy({ proxyList: [reqBodyU] })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']}`,
                detail: `${dict['message.update']} ${dict['message.success']}!`,
              });
              this.changePage('query');
              this.querySmartOnFhirProxyList();
            }
          });

        break;
    }
  }

  deleteConfirm(rowData: SmartOnFhirProxyDto) {
    let reqBodyD = {
      sofProxyId: rowData.sofProxyId,
      version: rowData.version,
    } as DPB0313ReqItem;

    this.serverService
      .deleteSmartOnFhirProxy({ proxyList: [reqBodyD] })
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['message.delete', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['message.delete']}`,
            detail: `${dict['message.delete']} ${dict['message.success']}!`,
          });
          this.querySmartOnFhirProxyList();
        }
      });
  }

  // async fileChange(event: any) {
  //   let file: FileList = event.target.files;

  //   if (file.length != 0) {
  //     this.file = file.item(0);
  //     this.fileName = file[0].name;
  //     event.target.value = '';
  //   } else {
  //     this.file = null;
  //     event.target.value = '';
  //   }
  // }

  openFileBrowser() {
    $('#file').click();
  }

  exportWebsocketProxy() {
    this.ngxService.start();
    this.serverService.exportWebsocketProxy().subscribe((res) => {
      if (res.type === 'application/json') {
        const reader = new FileReader();
        reader.onload = () => {
          const jsonData = JSON.parse(reader.result as string);
          this.alertService.ok(
            jsonData.ResHeader.rtnMsg,
            '',
            AlertType.warning,
            jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID,
          );
        };
        reader.readAsText(res);
      } else {
        const data: Blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
        });

        const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
        const ver = sessionStorage.getItem('majorVersionNo') ?? '';
        FileSaver.saveAs(data, `WebSocket_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  formatDiversionList(diversionDto: Array<SmartOnFhirProxyDiversionDto>) {
    if (diversionDto) {
      return diversionDto.map((row) => row.sofProxyDiversionId).join(', ');
    } else return '';
  }

  formValid(evt) {
    this._formValid = evt;
  }

  queryRoleList() {
    const ref = this.dialogService.open(ClientInfoListComponent, {
      data: { selectionMode: 'single' },
      header: this.translate.instant('role_list'),
      styleClass: 'cHeader cContent cIcon',
      width: '700px',
    });

    ref?.onClose.subscribe((res) => {
      if (res) {
        this.sofProxyClientId!.setValue(res.map((row: any) => row.clientId));
      } else {
        this.sofProxyClientId!.setValue([]);
      }
    });
  }

  exportFile() {
    let tarUrl = '';
    if (!environment.production) {
      tarUrl = environment.apiUrl;
    } else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    const _filePath = `${tarUrl}/dgrv4/11/DPB0314`;
    let token = this.toolService.getToken();

    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportSmartOnFhirProxy),
      ReqBody: {},
    } as ReqDPB0314;

    let _exportData = [];
    // this.ngxService.start();
    this.streamApi
      .getStreamMessages('POST', _filePath, JSON.stringify(body), {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      })
      .subscribe({
        next: (res) => {
          this.exportProcessPercent = res.percentage;
          this.exportStatus = res.type;
          if (res.type === 'progress') {
            _exportData = _exportData.concat(res.data);
          } else if (res.type === 'complete') {
            this.exportFileProcess(_exportData);
          }
        },

        complete: () => {
          // console.log('export complete');
          // this.ngxService.stop();
        },

        error: (err) => {
          console.error(err);
          this.ngxService.stop();
        },
      });
  }

  importFileProcess() {
    // console.log(this.importSelected);
    let fileReader = new FileReader();
    try {
      fileReader.onloadend = () => {
        let tarUrl = '';
        if (!environment.production) {
          tarUrl = environment.apiUrl;
        } else {
          tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
        }
        const _filePath = `${tarUrl}/dgrv4/11/DPB0316`;
        let token = this.toolService.getToken();

        let formData = new FormData();
        formData.append('jsonData', JSON.stringify(this.importSelected));

        this.streamApi
          .getStreamMessages('POST', _filePath, formData, {
            Authorization: `Bearer ${token}`,
          })
          .subscribe({
            next: (res) => {
              // console.log(res);
              if (res.type === 'progress') {
                //   this.importData = this.importData.concat(res.data);
                this.importData.find((row) => {
                  if (row.sofProxyId === res.data.sofProxyId)
                    row.result = res.data.validationResult.success;
                });
              }
              // else if (res.type === 'complete') {
              //   this.exportFileProcess(_exportData);
              // }
            },

            complete: () => {
              this.ngxService.stop();
            },

            error: (err) => {
              // console.error(err);
              this.ngxService.stop();
            },
          });
      };
    } catch (error) {
      // console.log(error);
      this.ngxService.stop();
    }
    this.ngxService.start();
    fileReader.readAsText(this.importFile!);
  }

  showMessage(summary:string = '', detail:string = '') {
    this.messageService.add({
      severity: 'success',
      summary: summary,
      detail: detail,
    });
  }

  exportFileProcess(data: any) {
    if (!data || (Array.isArray(data) && data.length === 0)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'No Data',
        detail: 'There is no data to export.',
      });
      return;
    }

    try {
      const jsonString = JSON.stringify(data, null, 2);
      const blob = new Blob([jsonString], {
        type: 'application/json;charset=utf-8',
      });
      const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
      FileSaver.saveAs(blob, `exported_data_${date}.json`);
    } catch (error) {
      console.error('Error during file export:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Export Failed',
        detail: 'Could not export data to JSON file.',
      });
    }
  }

  changeFile(event: any) {
    let file: FileList = event.target.files;

    if (file.length != 0) {
      this.importFile = file.item(0);
      this.importFileName = file[0].name;
      event.target.value = '';
    } else {
      this.importFile = undefined;
      this.importFileName = '';
      event.target.value = '';
    }
  }

  uploadFile() {
    this.importData = [];
    this.importSelected = [];
    let fileReader = new FileReader();
    try {
      fileReader.onloadend = () => {
        let tarUrl = '';
        if (!environment.production) {
          tarUrl = environment.apiUrl;
        } else {
          tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
        }
        const _filePath = `${tarUrl}/dgrv4/11/DPB0315`;
        let token = this.toolService.getToken();

        let formData = new FormData();
        formData.append('file', this.importFile!);

        this.streamApi
          .getStreamMessages('POST', _filePath, formData, {
            Authorization: `Bearer ${token}`,
          })
          .subscribe({
            next: (res) => {
              // console.log(res);
              if (res.type === 'progress') {
                this.importData = this.importData.concat(res.data);
              }
              // else if (res.type === 'complete') {
              //   this.exportFileProcess(_exportData);
              // }
            },

            complete: () => {
              this.reload = true;
              this.ngxService.stop();
            },

            error: (err) => {
              console.error(err);
              this.ngxService.stop();
            },
          });
      };
    } catch (error) {
      console.log(error);
      this.ngxService.stop();
    }
    this.ngxService.start();
    fileReader.readAsText(this.importFile!);
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get sofProxyId() {
    return this.formE.get('sofProxyId')!;
  }

  public get sofProxyName() {
    return this.formE.get('sofProxyName')!;
  }

  public get sofProxyStatus() {
    return this.formE.get('sofProxyStatus')!;
  }

  public get sofProxyStatusName() {
    return this.formE.get('sofProxyStatusName')!;
  }

  public get sofProxyRemark() {
    return this.formE.get('sofProxyRemark')!;
  }

  public get sofProxyAccessToken() {
    return this.formE.get('sofProxyAccessToken')!;
  }

  public get sofProxySqlInjection() {
    return this.formE.get('sofProxySqlInjection')!;
  }

  public get sofProxyTraffic() {
    return this.formE.get('sofProxyTraffic')!;
  }

  public get sofProxyXss() {
    return this.formE.get('sofProxyXss')!;
  }

  public get sofProxyXxe() {
    return this.formE.get('sofProxyXxe')!;
  }

  public get sofProxyTps() {
    return this.formE.get('sofProxyTps')!;
  }

  public get sofProxyIgnoreApi() {
    return this.formE.get('sofProxyIgnoreApi')!;
  }

  public get sofProxyClientId() {
    return this.formE.get('sofProxyClientId')!;
  }

  public get sofProxyShowLog() {
    return this.formE.get('sofProxyShowLog')!;
  }

  public get sofProxySticky() {
    return this.formE.get('sofProxySticky')!;
  }

  public get diversionList() {
    return this.formE.get('diversionList')!;
  }

  public get createDateTime() {
    return this.formE.get('createDateTime')!;
  }

  public get createUser() {
    return this.formE.get('createUser')!;
  }

  public get updateDateTime() {
    return this.formE.get('updateDateTime')!;
  }

  public get updateUser() {
    return this.formE.get('updateUser')!;
  }

  public get version() {
    return this.formE.get('version')!;
  }

  //   public get file() {
  //   return this.formFile.get('file')!;
  // }
  // public get fileName() {
  //   return this.formFile.get('fileName')!;
  // }
}
