import { DPB9922Req } from './../../../models/api/ServerService/dpb9922.interface';
import { ExportService } from 'src/app/shared/services/export.service';
import { DPB9904Req } from './../../../models/api/ServerService/dpb9904.interface';
import { DPB9902Req } from './../../../models/api/ServerService/dpb9902.interface';
import { ServerService } from './../../../shared/services/api-server.service';
import { ApiBaseService } from './../../../shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import {
  DPB9900Item,
  DPB9900Req,
} from './../../../models/api/ServerService/dpb9900.interface';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { FileService } from 'src/app/shared/services/api-file.service';

import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { Observable } from 'rxjs';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
// import { ModuleService } from 'srcAngular/app/shared/services/api-module.service';
import { concatMap } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { generate } from 'generate-password';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { DPB9901Req } from 'src/app/models/api/ServerService/dpb9901.interface';
import { DPB9903Req } from 'src/app/models/api/ServerService/dpb9903.interface';
import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DialogService } from 'primeng/dynamicdialog';
import { ReportTestConnectionComponent } from './report-test-connection/report-test-connection.component';

@Component({
  selector: 'app-tsmpsetting',
  templateUrl: './tsmpsetting.component.html',
  styleUrls: ['./tsmpsetting.component.css'],
  providers: [FileService, ApiService, ConfirmationService],
})
export class TsmpsettingComponent extends BaseComponent implements OnInit {
  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  toastValue: any;
  cols: { field: string; header: string }[] = [];
  tableData: Array<DPB9900Item> = [];
  tableDataRowcount: number = 0;
  delData?: DPB9900Item;
  currentAction: string = '';
  btnName: string = '';

  fileName: string = '';
  _keyword: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private translateService: TranslateService,
    private messageService: MessageService,
    private alertService: AlertService,
    // private moduleService: ModuleService,
    private fileService: FileService,
    private api: ApiBaseService,
    private apiService: ApiService,
    private serverService: ServerService,
    private router: Router,
    private confirmationService: ConfirmationService,
    private message: MessageService,
    private ngxService: NgxUiLoaderService,
    private dialogService: DialogService
  ) {
    super(route, tr);
  }

  ngOnInit() {
    // this.checkOrgId();

    this.form = this.fb.group({
      keyword: new FormControl(''),
      id: new FormControl(''),
      value: new FormControl(''),
      memo: new FormControl(),
      newVal: new FormControl(''),
      fileData: new FormControl(),
      encrptionType: new FormControl(),
    });

    this.init();
  }

  checkOrgId() {
    const tokenPool = this.toolService.getToken().split('.');
    const token = this.toolService.Base64Decoder(tokenPool[1]);
    const tokenParse = JSON.parse(token);

    if (tokenParse.org_id !== '100000') {
      this.router.navigateByUrl('/about');
    }
  }

  queryTsmpSettingList() {
    let ReqBody = {
      id: '',
      keyword: this.keyword.value,
    } as DPB9900Req;
    this.serverService.queryTsmpSettingList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
        this.tableDataRowcount = this.tableData.length;
      } else {
        this.tableData = [];
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  async init() {
    const code = ['key', 'value', 'memo'];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'id', header: dict['key'] },
      { field: 'value', header: dict['value'] },
      { field: 'memo', header: dict['memo'] },
    ];
    this.tableData = [];
    this.tableDataRowcount = this.tableData.length;
    let ReqBody = {
      id: '',
      keyword: this.keyword.value,
    } as DPB9900Req;
    this.serverService
      .queryTsmpSettingList_ignore1298(ReqBody)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.dataList;
          this.tableDataRowcount = this.tableData.length;
        }
      });
  }

  async copyOriData(identifData: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.toolService.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = identifData;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.messageService.add({
      severity: 'success',
      summary: `${dict['copy']} ${dict['data']}`,
      detail: `${dict['copy']} ${dict['message.success']}`,
    });
  }

  async changePage(action: string, rowData?: DPB9900Item) {
    this.currentAction = action;
    const code = ['button.create', 'button.update', 'button.delete'];
    const dict = await this.toolService.getDict(code);
    this._keyword = this.keyword.value;
    this.resetFormValidator(this.form);

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        this.keyword.setValue(this._keyword);
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];
        this.id.enable();
        this.value.enable();
        this.serverService.addTsmpSetting_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
            this.encrptionType.setValue('NONE');
          }
        });
        break;
      case 'update':
        let ReqBodyU = {
          id: rowData!.id,
        } as DPB9901Req;
        this.serverService.queryTsmpSettingDetail(ReqBodyU).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.serverService.updateTsmpSetting_before().subscribe((_res) => {
              if (this.toolService.checkDpSuccess(_res.ResHeader)) {
                this.addFormValidator(this.form, _res.RespBody.constraints);

                this.currentTitle = `${this.title} > ${dict['button.update']}`;
                this.pageNum = 3;
                this.btnName = dict['button.update'];

                this.id.setValue(res.RespBody.id);
                this.id.disable();
                this.value.setValue(res.RespBody.value);
                this.newVal.setValue(res.RespBody.value);
                this.memo.setValue(res.RespBody.memo);
              }
            });
          }
        });
        break;
      case 'delete':
        let ReqBodyD = {
          id: rowData!.id,
        } as DPB9901Req;
        this.serverService.queryTsmpSettingDetail(ReqBodyD).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle = `${this.title} > ${dict['button.delete']}`;
            this.pageNum = 4;
            this.btnName = dict['button.delete'];

            this.id.setValue(res.RespBody.id);
            this.value.setValue(res.RespBody.value);
            this.newVal.setValue(res.RespBody.value);
            this.memo.setValue(res.RespBody.memo);
          }
        });
        break;
      case 'export':
        this.ngxService.start();
        this.serverService.exportTsmpSetting().subscribe((res) => {
          if (res.type === 'application/json') {
            const reader = new FileReader();
            reader.onload = () => {
              const jsonData = JSON.parse(reader.result as string);
              this.alertService.ok(
                jsonData.ResHeader.rtnMsg,
                '',
                AlertType.warning,
                jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID
              );
            };
            reader.readAsText(res);
          } else {
            const data: Blob = new Blob([res], {
              type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
            });

            const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
            const ver = sessionStorage.getItem('majorVersionNo') ?? '';
            FileSaver.saveAs(data, `Setting_${date}_${ver}.xlsx`);
          }
          this.ngxService.stop();
        });
        break;
    }
  }

  importTsmpSetting() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importTsmpSetting),
      ReqBody: {},
    };

    this.serverService
      .importTsmpSetting(req, this.fileData.value)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['uploading', 'message.success', 'upload_result'];
          const dict = await this.toolService.getDict(code);
          this.message.add({
            severity: 'success',
            summary: dict['upload_result'],
            detail: `${dict['message.success']}!`,
          });
          this.fileData.reset();
          this.fileName = '';
          this.queryTsmpSettingList();
        }
      });
  }

  getMoreData() {
    let ReqBody = {
      id: this.tableData[this.tableData.length - 1].id,
      keyword: this.keyword.value,
    } as DPB9900Req;
    this.serverService.queryTsmpSettingList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.dataList);
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  onToastClose(event) {
    this.toastValue = '';
  }

  async procData(encodeType?: string) {
    console.log(this.currentAction);
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);
    switch (this.currentAction) {
      case 'create':
        let reqBodyC = {
          id: this.id.value,
          value: this.value.value,
          memo: this.memo.value,
          encrptionType: encodeType ? encodeType : 'NONE',
        } as DPB9902Req;
        this.serverService.addTsmpSetting(reqBodyC).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']} ${dict['key']}`,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.keyword.setValue(this._keyword);
            this.queryTsmpSettingList();
            this.changePage('query');
          }
        });
        break;
      case 'update':
        let reqBodyU = {
          id: this.id.value,
          oldVal: this.value.value,
          newVal: this.newVal.value,
          memo: this.memo.value,
          encrptionType: encodeType ? encodeType : 'NONE',
        } as DPB9903Req;
        this.serverService.updateTsmpSetting(reqBodyU).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']} ${dict['key']}`,
              detail: `${dict['message.update']} ${dict['message.success']}!`,
            });
            this.keyword.setValue(this._keyword);
            this.queryTsmpSettingList();
            this.changePage('query');
          }
        });
        break;
      case 'delete':
        this.deleteUser();
        break;
    }
  }

  async deleteUser() {
    const code = ['cfm_del', 'system_alert'];
    const dict = await this.toolService.getDict(code);
    // this.messageService.add({ key: 'deleteMsg', sticky: true, severity: 'error', summary: dict['cfm_del'] });
    this.confirmationService.confirm({
      header: dict['system_alert'],
      message: dict['cfm_del'],
      accept: () => {
        this.deleteConfirm();
      },
    });
  }

  deleteConfirm() {
    this.messageService.clear();
    let ReqBody = {
      id: this.id.value,
    } as DPB9904Req;

    this.serverService.deleteTsmpSetting(ReqBody).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'key', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.delete']} ${dict['key']}`,
          detail: `${dict['message.delete']} ${dict['message.success']}!`,
        });
        this.keyword.setValue(this._keyword);
        this.queryTsmpSettingList();
        this.changePage('query');
      }
    });
  }

  onReject() {
    this.messageService.clear();
  }

  originStringTable(item: any) {
    return !item.ori ? item.val : item.t ? item.val : item.ori;
  }

  switchOri(item: any) {
    item.t = !item.t;
  }

  headerReturn() {
    this.changePage('query');
  }

  async testConnection(type: string) {
    const code = ['button.connect_test'];
    const dict = await this.toolService.getDict(code);

    if (type == 'ES') {
      this.ngxService.start();
      this.serverService.testEsConnection().subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.dialogService.open(ReportTestConnectionComponent, {
            data: {
              type: type,
              esResp: res.RespBody.esRespMap,
            },
            header: `${type} ${dict['button.connect_test']}`,
            width: '700px',
          });
          this.ngxService.stop();
        }
      });
    } else {
      this.ngxService.start();
      this.serverService.testKibanaConnection().subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.dialogService.open(ReportTestConnectionComponent, {
            data: {
              type: type,
              kibanaResp: res.RespBody,
            },
            header: `${type} ${dict['button.connect_test']}`,
            width: '800px',
          });
          this.ngxService.stop();
        }
      });
    }
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;
    const code = [
      'uploading',
      'cfm_img_format',
      'cfm_size',
      'message.success',
      'upload_result',
      'waiting',
    ];
    const dict = await this.toolService.getDict(code);
    if (file.length != 0) {
      let fileReader = new FileReader();
      fileReader.onloadend = () => {
        // this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
        this.fileData!.setValue(file.item(0));
        // console.log(this.fileData.value)
        event.target.value = '';
      };
      fileReader.readAsBinaryString(file.item(0)!);
      this.fileName = file[0].name;
    } else {
      this.fileData!.setValue(null);
      event.target.value = '';
    }
  }

  openFileBrowser() {
    $('#file').click();
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get id() {
    return this.form.get('id')!;
  }
  public get value() {
    return this.form.get('value')!;
  }
  public get newVal() {
    return this.form.get('newVal')!;
  }
  public get memo() {
    return this.form.get('memo')!;
  }
  public get fileData() {
    return this.form.get('fileData')!;
  }
  public get encrptionType() {
    return this.form.get('encrptionType')!;
  }
}
