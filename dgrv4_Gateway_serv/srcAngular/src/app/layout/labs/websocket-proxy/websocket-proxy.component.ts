import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB0174Req, DPB0174RespItem } from 'src/app/models/api/ServerService/dpb0174.interface';
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

@Component({
  selector: 'app-websocket-proxy',
  templateUrl: './websocket-proxy.component.html',
  styleUrls: ['./websocket-proxy.component.css'],
  providers: [ConfirmationService]
})
export class WebsocketProxyComponent extends BaseComponent implements OnInit {

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  cols: { field: string; header: string }[] = [];
  tableData: Array<DPB0174RespItem> = [];
  currentAction: string = '';
  btnName: string = '';
  selectedItem?: DPB0175Resp;

  file: any = null;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private serverService: ServerService,
    private toolService: ToolService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private api: ApiBaseService,
    private alertService: AlertService,
    private ngxService: NgxUiLoaderService
  ) {
    super(route, tr);
  }

  async ngOnInit() {

    this.form = this.fb.group({
      keyword: new FormControl(''),
      siteName: new FormControl(''),
      targetWs: new FormControl(''),
      memo: new FormControl(''),
      auth: new FormControl('N'),
    })

    const code = ['ID', 'site_name', 'target_ws', 'last_update_user', 'last_update_datetime'];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'hexId', header: dict['ID'] }, // ID
      { field: 'siteName', header: dict['site_name'] }, //站點名稱
      { field: 'targetWs', header: dict['target_ws'] }, //目標websocket
      { field: 'changeUser', header: dict['last_update_user'] }, //最近異動人員
      { field: 'changeDateTime', header: dict['last_update_datetime'] }, //最近一動時間
    ]

    let ReqBody = {
      keyword: this.keyword.value,
    } as DPB0174Req;
    this.serverService.queryWsList_ignore1298(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
      }
    })
  }

  queryWsList() {
    let ReqBody = {
      keyword: this.keyword.value,
    } as DPB0174Req;
    this.serverService.queryWsList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
      } else {
        this.tableData = [];
      }
    })
  }

  getMoreData() {
    let ReqBody = {
      longId: this.tableData[this.tableData.length - 1].longId,
      keyword: this.keyword.value,
    } as DPB0174Req;
    this.serverService.queryWsList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.dataList);
      }
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  async changePage(action: string, rowData?: DPB0174RespItem) {
    this.currentAction = action;
    const code = ['button.create', 'button.update', 'button.delete', 'button.detail', 'cfm_del'];
    const dict = await this.toolService.getDict(code);
    this.resetFormValidator(this.form);
    this.selectedItem = undefined;

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];


        this.serverService.createWs_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
            this.auth.setValue('N');
          }
        });
        break;

      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 2;
        this.btnName = dict['button.update'];


        let reqBodyU = {
          longId: rowData?.longId,
        } as DPB0175Req;

        this.serverService.queryWsDetail(reqBodyU).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.selectedItem = res.RespBody;
            this.siteName.setValue(res.RespBody.siteName);
            this.targetWs.setValue(res.RespBody.targetWs);
            this.memo.setValue(res.RespBody.memo);
            this.auth.setValue(res.RespBody.auth)

            this.serverService.updateWs_before().subscribe(res => {
              this.addFormValidator(this.form, res.RespBody.constraints);
            })
          }
        });
        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.pageNum = 3;
        this.btnName = dict['button.detail'];

        let reqBodyD = {
          longId: rowData?.longId,
        } as DPB0175Req;
        this.serverService.queryWsDetail(reqBodyD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.selectedItem = res.RespBody;
          }
        });
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.deleteConfirm(rowData);
          }
        });
        break;
    }
  }

  async procData() {
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);

    switch (this.currentAction) {
      case 'create':
        let reqBodyC = {
          siteName: this.siteName.value,
          targetWs: this.targetWs.value,
          memo: this.memo.value == '' ? null : this.memo.value,
          auth: this.auth.value
        } as DPB0176Req;
        // console.log(reqBodyC)

        this.serverService.createWs(reqBodyC).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({ severity: 'success', summary: `${dict['message.create']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
            this.queryWsList();
            this.changePage('query');
          }
        });
        break;
      case 'update':
        let reqBodyU = {
          longId: this.selectedItem?.longId,
          targetWs: this.targetWs.value,
          memo: this.memo.value == '' ? null : this.memo.value,
          auth: this.auth.value
        } as DPB0177Req;
        this.serverService.updateWs(reqBodyU).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success', summary: `${dict['message.update']}`, detail: `${dict['message.update']} ${dict['message.success']}!`
            });
            this.queryWsList();
            this.changePage('query');
          }
        });
        break;
      case 'delete':
        // this.deleteUser();
        break;
    }
  }

   deleteConfirm(rowData?:DPB0174RespItem){
    let reqBodyD = {
      longId: rowData?.longId,
    } as DPB0178Req;
    this.serverService.deleteWs(reqBodyD).subscribe( async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete',  'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.delete']} `,
          detail: `${dict['message.delete']} ${dict['message.success']}!`
        });
        this.queryWsList();
        this.changePage('query');
      }
    });
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;

    if (file.length != 0) {
      this.file = file.item(0);
      event.target.value = '';
    } else {
      this.file = null;
      event.target.value = '';
    }
  }

  openFileBrowser() {
    $('#file').click();
  }

  exportWebsocketProxy(){
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
            FileSaver.saveAs(data, `WebSocket_${date}.xlsx`);
          }
          this.ngxService.stop();
        });
  }

  importWebsocketProxy() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importWebsocketProxy),
      ReqBody: {},
    };

    this.serverService
      .importWebsocketProxy(req, this.file)
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
          this.queryWsList();
        }
        this.ngxService.stop();
      });
  }

  public get keyword() { return this.form.get('keyword')!; }
  public get siteName() { return this.form.get('siteName')!; }
  public get targetWs() { return this.form.get('targetWs')!; }
  public get memo() { return this.form.get('memo')!; }
  public get auth() { return this.form.get('auth')!; }
}
