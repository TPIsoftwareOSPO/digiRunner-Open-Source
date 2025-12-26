import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { MessageService, ConfirmationService } from 'primeng/api';
import {
  DPB0097Item,
  DPB0097Req,
} from 'src/app/models/api/RtnCodeService/dpb0097.interface';
import { TranslateService } from '@ngx-translate/core';
import { switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { RtnCodeService } from 'src/app/shared/services/api-rtn-code.service';
import { DPB0098Req } from 'src/app/models/api/RtnCodeService/dpb0098.interface';
import { DPB0100Req } from 'src/app/models/api/RtnCodeService/dpb0100.interface';
import { DPB0096Req } from 'src/app/models/api/RtnCodeService/dpb0096.interface';
import { DPB0099Req } from 'src/app/models/api/RtnCodeService/dpb0099.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import {
  DPB0115Req,
  DPB0115Item,
} from 'src/app/models/api/RoleService/dpb0115.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { AlertService } from 'src/app/shared/services/alert.service';

@Component({
  selector: 'app-ac0103',
  templateUrl: './ac0103.component.html',
  styleUrls: ['./ac0103.component.css'],
  providers: [ConfirmationService],
})
export class Ac0103Component extends BaseComponent implements OnInit {
  @ViewChild('dialog') _dialog!: DialogComponent;

  form: FormGroup;
  localies: { label: string; value: string }[] = [];
  cols: { field: string; header: string; width: string }[] = [];
  dialogTitle: string = '';
  rowcount: number = 0;
  rtnCodeList: Array<DPB0097Item> = new Array<DPB0097Item>();
  delRtn?: DPB0097Item;
  pageNum: number = 1;
  btnName: string = '';
  currentAction: string = '';
  currentTitle: string = this.title;
  canCreate: boolean = false;
  canUpdate: boolean = false;
  canDelete: boolean = false;
  file: any = null;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private list: ListService,
    private translate: TranslateService,
    private rtnCodeService: RtnCodeService,
    private roleService: RoleService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private ngxService: NgxUiLoaderService,
    private api: ApiBaseService,
    private messageService: MessageService,
    private alertService: AlertService
  ) {
    super(route, tr);

    this.form = this.fb.group({
      keyword: new FormControl(''),
      tsmpRtnCode: new FormControl(''),
      locale: new FormControl(''),
      tsmpRtnMsg: new FormControl(''),
      oldMsg: new FormControl(''),
      tsmpRtnDesc: new FormControl(''),
    });
  }

  ngOnInit() {
    let ReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('RTN_CODE_LOCALE')
        ) +
        ',' +
        22,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        let _localies: { label: string; value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            _localies.push({ label: item.subitemName, value: item.subitemNo });
          }
        }
        this.localies = _localies;
      }
    });
    this.roleService
      .queryRTMapByUk({
        txIdList: ['DPB0096', 'DPB0099', 'DPB0100'],
      } as DPB0115Req)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.canCreate = res.RespBody.dataList.find(
            (item) => item.txId === 'DPB0096'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'DPB0096')!
                .available
            : false;
          this.canUpdate = res.RespBody.dataList.find(
            (item) => item.txId === 'DPB0099'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'DPB0099')!
                .available
            : false;
          this.canDelete = res.RespBody.dataList.find(
            (item) => item.txId === 'DPB0100'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'DPB0100')!
                .available
            : false;
        }
      });
    this.init();
  }

  async changePage(action: string, rowData?: DPB0097Item) {
    this.currentAction = action;
    const code = ['button.create', 'button.update'];
    const dict = await this.toolService.getDict(code);
    this.resetFormValidator(this.form);
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];
        this.tsmpRtnCode!.enable();
        this.locale!.enable();
        this.rtnCodeService.createApiRtnCode_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
          }
        });
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 2;
        this.btnName = dict['button.update'];
        let ReqBody = {
          tsmpRtnCode: rowData?.tsmpRtnCode,
          locale: rowData?.locale,
        } as DPB0098Req;
        this.rtnCodeService.qureyApiRtnCodeByPk(ReqBody).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.tsmpRtnCode!.setValue(res.RespBody.tsmpRtnCode);
            this.tsmpRtnCode!.disable();
            this.locale!.setValue(res.RespBody.locale);
            this.locale!.disable();
            this.tsmpRtnMsg!.setValue(res.RespBody.tsmpRtnMsg);
            this.oldMsg!.setValue(res.RespBody.tsmpRtnMsg);
            this.tsmpRtnDesc!.setValue(res.RespBody.tsmpRtnDesc);
            this.rtnCodeService.updateApiRtnCode_before().subscribe((res) => {
              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
              }
            });
          }
        });
        break;
    }
  }

  async init() {
    const code = ['rtn_code', 'fun_locale', 'rtn_msg', 'rtn_desc'];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'tsmpRtnCode', header: dict['rtn_code'], width: '10%' },
      { field: 'locale', header: dict['fun_locale'], width: '10%' },
      { field: 'tsmpRtnMsg', header: dict['rtn_msg'], width: '40%' },
      { field: 'tsmpRtnDesc', header: dict['rtn_desc'], width: '30%' },
    ];
    this.rtnCodeList = [];
    this.rowcount = this.rtnCodeList.length;
    let ReqBody = {
      keyword: this.form.get('keyword')!.value,
    } as DPB0097Req;
    this.rtnCodeService
      .queryApiRtnCodeList_ignore1298(ReqBody)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.rtnCodeList = res.RespBody.dataList;
          this.rowcount = this.rtnCodeList.length;
        }
      });
  }

  queryRtnCodeList() {
    this.rtnCodeList = [];
    this.rowcount = this.rtnCodeList.length;
    let ReqBody = {
      keyword: this.form.get('keyword')!.value,
    } as DPB0097Req;
    this.rtnCodeService.queryApiRtnCodeList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.rtnCodeList = res.RespBody.dataList;
        this.rowcount = this.rtnCodeList.length;
      }
    });
  }

  moreRtnCodeList() {
    let ReqBody = {
      tsmpRtnCode: this.rtnCodeList[this.rtnCodeList.length - 1].tsmpRtnCode,
      locale: this.rtnCodeList[this.rtnCodeList.length - 1].locale,
      keyword: this.form.get('keyword')!.value,
    } as DPB0097Req;
    this.rtnCodeService.queryApiRtnCodeList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.rtnCodeList = this.rtnCodeList.concat(res.RespBody.dataList);
        this.rowcount = this.rtnCodeList.length;
      }
    });
  }

  showDialog(rowData: DPB0097Item) {
    const codes = [
      'dialog.delete',
      'message.success',
      'message.update',
      'message.delete',
      'rtn_code',
      'cfm_del_rtnCode',
    ];
    this.translate
      .get(codes)
      .pipe(switchMap((dict) => this.openDialog$(rowData, dict)))
      .subscribe();
  }

  openDialog$(rowData: DPB0097Item, dict: any): Observable<boolean> {
    return Observable.create((obser) => {
      this.delRtn = rowData;
      this.messageService.clear();
      // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_rtnCode'], detail: `${rowData.tsmpRtnCode} - ${rowData.tsmpRtnMsg}` });
      this.confirmationService.confirm({
        header: dict['cfm_del_rtnCode'],
        message: `${rowData.tsmpRtnCode} - ${rowData.tsmpRtnMsg}`,
        accept: () => {
          this.onDeleteConfirm();
        },
      });

      obser.next(true);
    });
  }

  async createOrUpdate() {
    const code = [
      'message.create',
      'rtn_code',
      'message.success',
      'message.update',
    ];
    const dict = await this.toolService.getDict(code);
    switch (this.currentAction) {
      case 'create':
        let createReqBody = {
          tsmpRtnCode: this.tsmpRtnCode!.value,
          locale:
            this.toolService.Base64Encoder(
              this.toolService.BcryptEncoder(this.locale!.value)
            ) +
            ',' +
            this.convertLocaleIndex(this.locale!.value),
          tsmpRtnMsg: this.tsmpRtnMsg!.value,
          tsmpRtnDesc: this.tsmpRtnDesc!.value,
        } as DPB0096Req;
        this.rtnCodeService.createApiRtnCode(createReqBody).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']} ${dict['rtn_code']}`,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.queryRtnCodeList();
            this.changePage('query');
          }
        });
        break;
      case 'update':
        let updateReqBody = {
          tsmpRtnCode: this.tsmpRtnCode!.value,
          locale:
            this.toolService.Base64Encoder(
              this.toolService.BcryptEncoder(this.locale!.value)
            ) +
            ',' +
            this.convertLocaleIndex(this.locale!.value),
          tsmpRtnMsg: this.tsmpRtnMsg!.value,
          oldMsg: this.oldMsg!.value,
          tsmpRtnDesc: this.tsmpRtnDesc!.value,
        } as DPB0099Req;
        this.rtnCodeService.updateApiRtnCode(updateReqBody).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']} ${dict['rtn_code']}`,
              detail: `${dict['message.update']} ${dict['message.success']}!`,
            });
            this.queryRtnCodeList();
            this.changePage('query');
          }
        });
        break;
    }
  }

  convertLocaleIndex(locale: string): number {
    return this.localies.findIndex((item) => item.value === locale);
  }

  async onDeleteConfirm() {
    this.messageService.clear();
    let ReqBody = {
      tsmpRtnCode: this.delRtn?.tsmpRtnCode,
      locale: this.delRtn?.locale,
    } as DPB0100Req;
    this.rtnCodeService.deleteApiRtnCode(ReqBody).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const codes = ['message.delete', 'rtn_code', 'message.success'];
        const dicts = await this.toolService.getDict(codes);
        this.messageService.clear();
        this.messageService.add({
          severity: 'success',
          summary: `${dicts['message.delete']} ${dicts['rtn_code']}`,
          detail: `${dicts['message.delete']} ${dicts['message.success']}!`,
        });
        this.queryRtnCodeList();
      }
    });
  }

  onReject() {
    this.messageService.clear();
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

  switchOri(rowData: any) {
    if (rowData.t) {
      rowData.t = !rowData.t;
    } else rowData.t = true;
  }

  switchDescOri(rowData: any) {
    if (rowData.desc) {
      rowData.desc = !rowData.desc;
    } else rowData.desc = true;
  }

  headerReturn() {
    this.changePage('query');
  }

  exportTsmpRtnCode() {
    this.ngxService.start();
    this.serverService.exportTsmpRtnCode().subscribe((res) => {
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
        FileSaver.saveAs(data, `Rtn_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  importTsmpRtnCode() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importTsmpRtnCode),
      ReqBody: {},
    };
    this.ngxService.start();
    this.serverService
      .importTsmpRtnCode(req, this.file)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['uploading', 'message.success', 'upload_result'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: dict['upload_result'],
            detail: `${dict['message.success']}!`,
          });
          this.file = null;
          this.queryRtnCodeList();
        }
        this.ngxService.stop();
      });
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
      this.file = file.item(0);
      event.target.value = '';
    } else {
      // this.fileData!.setValue(null);
      this.file = null;
      event.target.value = '';
    }
  }

  openFileBrowser() {
    $('#file').click();
  }

  public get keyword() {
    return this.form.get('keyword');
  }
  public get tsmpRtnCode() {
    return this.form.get('tsmpRtnCode');
  }
  public get locale() {
    return this.form.get('locale');
  }
  public get tsmpRtnMsg() {
    return this.form.get('tsmpRtnMsg');
  }
  public get oldMsg() {
    return this.form.get('oldMsg');
  }
  public get tsmpRtnDesc() {
    return this.form.get('tsmpRtnDesc');
  }
}
