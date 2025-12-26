import { combineLatest } from 'rxjs';
import { generate } from 'generate-password';
import { Component, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import {
  DPB0153Req,
  DPB0153WebsiteItem,
} from 'src/app/models/api/ServerService/dpb0153.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0157Req } from 'src/app/models/api/ServerService/dpb0157.interface';
import { ConfirmationService, MessageService } from 'primeng/api';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0154Req } from 'src/app/models/api/ServerService/dpb0154.interface';
import {
  DPB0158ItemReq,
  DPB0158Req,
  DPB0158Resp,
} from 'src/app/models/api/ServerService/dpb0158.interface';
import { DPB0155Req } from 'src/app/models/api/ServerService/dpb0155.interface';
import { DPB0156Req } from 'src/app/models/api/ServerService/dpb0156.interface';

import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { AlertService } from 'src/app/shared/services/alert.service';

@Component({
  selector: 'app-website-proxy',
  templateUrl: './website-proxy.component.html',
  styleUrls: ['./website-proxy.component.css'],
  providers: [ConfirmationService],
})
export class WebsiteProxyComponent extends BaseComponent implements OnInit {
  currentTitle: string = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  cols: { field: string; header: string }[] = [];
  tableData: Array<DPB0153WebsiteItem> = [];
  currentAction: string = '';
  btnName: string = '';
  selectedItem?: DPB0153WebsiteItem;
  statusList: { label: string; value: string }[] = [];
  statusListIgnoreAll: { label: string; value: string }[] = [];
  websiteInfo?: DPB0158Resp;

  formC!: FormGroup;
  _formValid: boolean = false;

  timeOut: any;

  fileName: string = '';
  file: any = null;

  // @ViewChild('websiterow', { read: ViewContainerRef, static: true }) websiterowRef!: ViewContainerRef;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private serverService: ServerService,
    private toolService: ToolService,
    private list: ListService,
    private messageService: MessageService,
    private ngxService: NgxUiLoaderService,
    private confirmationService: ConfirmationService,
    private api: ApiBaseService,
    private alert: AlertService
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.form = this.fb.group({
      id: new FormControl(''),
      keyword: new FormControl(''),
      websiteStatus: new FormControl('null'),
    });

    this.formC = this.fb.group({
      websiteStatus: new FormControl(''),
      websiteName: new FormControl(''),
      webSiteList: new FormControl(),
      remark: new FormControl(),
      auth: new FormControl(),
      sqlInjection: new FormControl(),
      traffic: new FormControl(),
      xss: new FormControl(),
      xxe: new FormControl(),
      tps: new FormControl(),
      ignoreApi: new FormControl(),
      showLog: new FormControl(),
    });

    const code = ['website_name', 'status', 'remark'];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'name', header: dict['website_name'] },
      { field: 'statusName', header: dict['status'] },
      { field: 'remark', header: dict['remark'] },
    ];
    this.getStatusList();

    this.serverService.queryWebsite_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.websiteList;
      }
    });

    this.tps.valueChanges.subscribe((val) => {
      if (val == null) this.tps.setValue('');
    });
  }

  ngOnDestroy(): void {
    //Called once, before the instance is destroyed.
    //Add 'implements OnDestroy' to the class.
    // clearInterval(this.timeInterval);
    clearTimeout(this.timeOut);
  }

  getWebsiteStatusEncodeString(status: string) {
    if (status == 'null') {
      return '-1';
    } else {
      return (
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder(status)) +
        ',' +
        this.statusList.findIndex((item) => item.value == status)
      );
    }
  }

  getStatusList() {
    let apiStatusReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('ENABLE_FLAG')
        ) +
        ',' +
        9,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(apiStatusReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        let statusOpt: { label: string; value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map((item) => {
            if (item.subitemNo != '2') {
              // 不要鎖定
              statusOpt.push({
                label: item.subitemName,
                value: item.param2 ? item.param2 : 'null',
              });
            }
          });
        }
        this.statusList = statusOpt;
        this.statusListIgnoreAll = this.statusList.filter(
          (x) => x.value != 'null'
        );
      }
    });
  }

  // convertStatusIndex(status: string): number {
  //   switch (status) {
  //     case '1':
  //       return 0;
  //     case '2':
  //       return 1;
  //     case '3':
  //       return 3;
  //     default:
  //       return -1;
  //   }
  // }

  queryData() {
    // console.log(typeof this.websiteStatus!.value)

    // let statusIdx =  this.statusList.findIndex(
    //   (item) => item.value == this.websiteStatus!.value
    // )
    // console.log(statusIdx)
    // return;
    let reqBody = {
      keyword: this.keyword.value,
      websiteStatus: this.getWebsiteStatusEncodeString(
        this.websiteStatus.value
      ),
    } as DPB0153Req;
    this.ngxService.start();
    this.serverService.queryWebsite(reqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.websiteList;
      } else {
        this.tableData = [];
      }
      this.ngxService.stop();
    });
  }

  getMoreData() {
    let reqBody = {
      id: this.tableData[this.tableData.length - 1].id,
      keyword: this.keyword.value,
      websiteStatus: this.getWebsiteStatusEncodeString(
        this.websiteStatus.value
      ),
    } as DPB0153Req;
    this.ngxService.start();
    this.serverService.queryWebsite(reqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.websiteList);
      }
      this.ngxService.stop();
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  async changePage(action: string, rowData?: DPB0153WebsiteItem) {
    this.currentAction = action;
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
    ];
    const dict = await this.toolService.getDict(code);
    // this.resetFormValidator(this.form);
    this.resetFormValidator(this.formC);
    this.selectedItem = undefined;
    // this.websiterowRef.clear();
    this.formC.enable();
    // clearInterval(this.timeInterval);
    clearTimeout(this.timeOut);

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;

        this.btnName = dict['button.create'];
        this.serverService.createWebsite_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.formC, res.RespBody.constraints);
            this.tps.setValue(0);
            this.auth.setValue('N');
            this.sqlInjection.setValue('N');
            this.traffic.setValue('N');
            this.xss.setValue('N');
            this.xxe.setValue('N');
            this.showLog.setValue('N');

            this.pageNum = 2;
          }
        });

        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;

        this.btnName = dict['button.update'];
        let reqDetail = {
          id: rowData?.id,
        } as DPB0158Req;
        this.selectedItem = rowData;

        this.serverService.getWebsiteInfo(reqDetail).subscribe((resInfo) => {
          if (this.toolService.checkDpSuccess(resInfo.ResHeader)) {
            this.serverService.updateWebsite_before().subscribe((res) => {
              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.formC, res.RespBody.constraints);

                this.websiteStatusC.setValue(resInfo.RespBody.websiteStatus);
                this.webSiteListC.setValue(resInfo.RespBody.webSiteList);
                this.websiteNameC.setValue(resInfo.RespBody.websiteName);
                if (resInfo.RespBody.remark)
                  this.remarkC.setValue(resInfo.RespBody.remark);

                this.auth.setValue(resInfo.RespBody?.auth);
                this.sqlInjection.setValue(resInfo.RespBody?.sqlInjection);
                this.traffic.setValue(resInfo.RespBody?.traffic);
                this.xss.setValue(resInfo.RespBody?.xss);
                this.xxe.setValue(resInfo.RespBody?.xxe);
                this.tps.setValue(resInfo.RespBody?.tps);
                this.ignoreApi.setValue(resInfo.RespBody?.ignoreApi);
                this.showLog.setValue(resInfo.RespBody.showLog);
                this.pageNum = 2;
              }
            });
          }
        });
        break;
      case 'delete':
        this.selectedItem = rowData;
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.deleteConfirm();
          },
        });
        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        // this.pageNum = 3;
        let reqD = {
          id: rowData?.id,
        } as DPB0158Req;
        this.selectedItem = rowData;

        this.serverService.getWebsiteInfo(reqD).subscribe((resInfo) => {
          if (this.toolService.checkDpSuccess(resInfo.ResHeader)) {
            // this.websiteInfo = resInfo.RespBody;
            // this.websiteInfo.webSiteList.forEach((rowData, index) => {
            //   this.generateletWebSettingRow({
            //     probability: Number(rowData.probability),
            //     url: rowData.url,
            //     no: index
            //   });
            // })

            this.formC.disable();

            this.websiteStatusC.setValue(resInfo.RespBody.websiteStatus);

            this.websiteNameC.setValue(resInfo.RespBody.websiteName);
            if (resInfo.RespBody.remark)
              this.remarkC.setValue(resInfo.RespBody.remark);

            this.auth.setValue(resInfo.RespBody?.auth);
            this.sqlInjection.setValue(resInfo.RespBody?.sqlInjection);
            this.traffic.setValue(resInfo.RespBody?.traffic);
            this.xss.setValue(resInfo.RespBody?.xss);
            this.xxe.setValue(resInfo.RespBody?.xxe);
            this.tps.setValue(resInfo.RespBody?.tps);
            this.ignoreApi.setValue(resInfo.RespBody?.ignoreApi);
            this.showLog.setValue(resInfo.RespBody.showLog);

            this.webSiteListC.setValue(resInfo.RespBody.webSiteList);

            this.queryTargetThroughputInterval(rowData?.name);
            this.pageNum = 2;
          }
        });

        // const getWebsiteInfoObs = this.serverService.getWebsiteInfo(reqD);
        // const queryTargetThroughputObs = this.serverService.queryTargetThroughput({ websiteName: rowData!.name });

        // combineLatest(getWebsiteInfoObs, queryTargetThroughputObs).subscribe((data) => {
        //   const [respInfo, respTThr] = data;
        //   if (this.toolService.checkDpSuccess(respInfo.ResHeader) && this.toolService.checkDpSuccess(respTThr.ResHeader)) {
        //     // console.log(respInfo,respTThr)
        //     this.formC.disable();

        //     this.websiteStatusC.setValue(respInfo.RespBody.websiteStatus);

        //     this.websiteNameC.setValue(respInfo.RespBody.websiteName);
        //     if (respInfo.RespBody.remark) this.remarkC.setValue(respInfo.RespBody.remark);

        //     this.auth.setValue(respInfo.RespBody?.auth);
        //     this.sqlInjection.setValue(respInfo.RespBody?.sqlInjection);
        //     this.traffic.setValue(respInfo.RespBody?.traffic);
        //     this.xss.setValue(respInfo.RespBody?.xss);
        //     this.xxe.setValue(respInfo.RespBody?.xxe);
        //     this.tps.setValue(respInfo.RespBody?.tps)
        //     this.ignoreApi.setValue(respInfo.RespBody?.ignoreApi)
        //     this.showLog.setValue(respInfo.RespBody.showLog)

        //     let _webSiteList = respInfo.RespBody.webSiteList.map(row => {
        //       let itemData = respTThr.RespBody.itemList?.find(item => item.targetUrl == row.url)
        //       return {
        //         ...row,
        //         targetThroughPut: itemData
        //       }
        //     })
        //     this.webSiteListC.setValue(_webSiteList);
        //     this.pageNum = 2;

        //   }
        // })
        break;
    }
  }

  // generateletWebSettingRow(rowData: {
  //   probability: number,
  //   url: string,
  //   no: number
  // }) {
  //   let componentRef = this.websiterowRef.createComponent(WebsiteSettingRowComponent);
  //   componentRef.instance._ref = componentRef;
  //   componentRef.instance.no = rowData.no;
  //   componentRef.instance.data = rowData;
  //   componentRef.instance.readonly = true;
  // }

  queryTargetThroughputInterval(websiteName: any) {
    this.timeOut = setTimeout(() => {
      this.serverService
        .queryTargetThroughput({ websiteName: websiteName })
        .subscribe((resp0201) => {
          if (this.toolService.checkDpSuccess(resp0201.ResHeader)) {
            if (this.webSiteListC) {
              let _webSiteList = this.webSiteListC.value.map((row) => {
                let itemData = resp0201.RespBody.itemList?.find(
                  (item) => item.targetUrl == row.url
                );
                return {
                  ...row,
                  targetThroughPut: itemData,
                };
              });
              this.webSiteListC.setValue(_webSiteList);
            }
          }
          this.queryTargetThroughputInterval(websiteName);
        });
    }, 1000);
  }

  async procData() {
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);

    switch (this.currentAction) {
      case 'create':
        let reqBodyC = {
          websiteStatus: this.getWebsiteStatusEncodeString(
            this.websiteStatusC.value
          ),
          websiteName: this.websiteNameC.value,
          webSiteList: this.webSiteListC.value.map((rowData) => {
            return {
              probability: rowData.probability,
              url: rowData.url,
            };
          }),
          remark: this.remarkC.value,
          auth: this.auth.value,
          sqlInjection: this.sqlInjection.value,
          traffic: this.traffic.value,
          xss: this.xss.value,
          xxe: this.xxe.value,
          tps: this.tps.value,
          ignoreApi: this.ignoreApi.value,
          showLog: this.showLog.value,
        } as DPB0154Req;

        this.serverService.createWebsite(reqBodyC).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']}`,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });

            this.changePage('query');
            // this.websiteStatus.setValue("-1");
            this.queryData();
          }
        });
        break;
      case 'update':
        let reqBodyU = {
          dgrWebsiteId: this.selectedItem?.id,
          websiteStatus: this.getWebsiteStatusEncodeString(
            this.websiteStatusC.value
          ),
          websiteName: this.websiteNameC.value,
          webSiteList: this.webSiteListC.value.map((rowData) => {
            return {
              probability: rowData.probability,
              url: rowData.url,
            };
          }),
          remark: this.remarkC.value,
          auth: this.auth.value,
          sqlInjection: this.sqlInjection.value,
          traffic: this.traffic.value,
          xss: this.xss.value,
          xxe: this.xxe.value,
          tps: this.tps.value,
          ignoreApi: this.ignoreApi.value,
          showLog: this.showLog.value,
        } as DPB0155Req;

        this.serverService.updateWebsite(reqBodyU).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']}`,
              detail: `${dict['message.update']} ${dict['message.success']}!`,
            });

            this.changePage('query');
            this.queryData();
          }
        });

        break;
    }
  }

  formValid(evt) {
    this._formValid = evt;
  }

  originStringTable(item: any) {
    return !item.ori ? item.val : item.t ? item.val : item.ori;
  }

  switchOri(item: any) {
    item.t = !item.t;
  }

  deleteConfirm() {
    this.messageService.clear();
    let ReqBody = {
      dgrWebsiteId: this.selectedItem?.id,
    } as DPB0156Req;

    this.serverService.deleteWebsite(ReqBody).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.delete']} `,
          detail: `${dict['message.delete']} ${dict['message.success']}!`,
        });

        this.changePage('query');
        this.queryData();
      }
    });
  }

  exportWebsiteProxy() {
    this.ngxService.start();
    this.serverService.exportWebsiteProxy().subscribe((res) => {
      if (res.type === 'application/json') {
        const reader = new FileReader();
        reader.onload = () => {
          const jsonData = JSON.parse(reader.result as string);
          this.alert.ok(
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
        FileSaver.saveAs(data, `Website_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  importWebsiteProxy() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importWebsiteProxy),
      ReqBody: {},
    };
    this.ngxService.start();
    this.serverService
      .importWebsiteProxy(req, this.file)
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
          this.fileName = '';
          this.queryData();
        }
        this.ngxService.stop();
      });
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;
    if (file.length != 0) {
      this.file = file.item(0);
      this.fileName = file[0].name;
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
    return this.form.get('keyword')!;
  }
  public get websiteStatus() {
    return this.form.get('websiteStatus')!;
  }
  public get websiteStatusC() {
    return this.formC.get('websiteStatus')!;
  }
  public get websiteNameC() {
    return this.formC.get('websiteName')!;
  }
  public get webSiteListC() {
    return this.formC.get('webSiteList')!;
  }
  public get remarkC() {
    return this.formC.get('remark')!;
  }
  public get auth() {
    return this.formC.get('auth')!;
  }
  public get sqlInjection() {
    return this.formC.get('sqlInjection')!;
  }
  public get traffic() {
    return this.formC.get('traffic')!;
  }
  public get xss() {
    return this.formC.get('xss')!;
  }
  public get xxe() {
    return this.formC.get('xxe')!;
  }
  public get tps() {
    return this.formC.get('tps')!;
  }
  public get ignoreApi() {
    return this.formC.get('ignoreApi')!;
  }
  public get showLog() {
    return this.formC.get('showLog')!;
  }
}
