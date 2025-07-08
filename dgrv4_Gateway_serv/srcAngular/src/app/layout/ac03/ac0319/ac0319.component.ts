import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute, Router } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AA0423RespItem } from 'src/app/models/api/ApiService/aa0423.interface';
import { LabelListComponent } from 'src/app/shared/label-list/label-list.component';
import { DialogService } from 'primeng/dynamicdialog';
import { TargetSiteListComponent } from 'src/app/shared/target-site-list/target-site-list.component';
import { AA0430Req } from 'src/app/models/api/ApiService/aa0430.interfcae';
import { LabelResetComponent } from 'src/app/shared/label-reset/label-reset.component';
import { AA0431Req } from 'src/app/models/api/ApiService/aa0431.interfcae';
import { AA0424Req } from 'src/app/models/api/ApiService/aa0424.interface';
import {
  AA0425Req,
  AA0425RespItem,
} from 'src/app/models/api/ApiService/aa0425.interface';
import {
  AA0426Req,
  AA0426RespItem,
} from 'src/app/models/api/ApiService/aa0426.interface';
import { AA0303Req } from 'src/app/models/api/ApiService/aa0303.interface';
import { TranslateService } from '@ngx-translate/core';
import { FailHandlePolicyComponent } from './fail-handle-policy/fail-handle-policy.component';
import { TOrgService } from 'src/app/shared/services/org.service';


@Component({
  selector: 'app-ac0319',
  templateUrl: './ac0319.component.html',
  styleUrls: ['./ac0319.component.css'],
  providers: [MessageService, ConfirmationService, ApiService],
})
export class Ac0319Component extends BaseComponent implements OnInit {
  pageNum: number = 1;
  currentTitle: string = this.title;

  labelList: Array<String> = [];
  targetSiteList: Array<String> = [];
  tableData: Array<AA0423RespItem> = [];
  selected: Array<AA0423RespItem> = [];

  apiListReq: { labelList?: Array<string>; targetSite?: Array<string> } = {};

  showPreview: boolean = false;
  previewDataReq?: AA0425Req;
  previewData: Array<AA0425RespItem> = [];

  showErrMsg: boolean = false;
  errMsg: string = '';
  errList: Array<AA0426RespItem> = [];

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private messageService: MessageService,
    private toolService: ToolService,
    private ngxService: NgxUiLoaderService,
    private confirmationService: ConfirmationService,
    private apiServer: ApiService,
    private dialogService: DialogService,
    private apiService: ApiService,
    private translate: TranslateService,

  ) {
    super(route, tr);
  }

  ngOnInit(): void {
    this.apiServer
      .queryAPIListBySrcUrlOrLabel(this.apiListReq)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.dataList;
        }
      });

    // this.confirmationService.confirm({
    //   key: 'err',
    //   header: '  ',
    //   message: '123456',
    // });
  }

  headerReturn() {
    this.changePage('query');
  }

  queryAPIListBySrcUrlOrLabel() {
    this.selected = [];
    this.ngxService.start();
    this.apiServer
      .queryAPIListBySrcUrlOrLabel(this.apiListReq)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          // console.log(res.RespBody);
          this.tableData = res.RespBody.dataList;
        }
        this.ngxService.stop();
      });
  }

  async queryLabelList() {
    const code = ['label_list'];
    const dict = await this.toolService.getDict(code);
    const ref = this.dialogService.open(LabelListComponent, {
      header: dict['label_list'],
      width: '700px',
    });

    ref.onClose.subscribe((res) => {
      if (res) {
        this.apiListReq.labelList = res.length > 0 ? res : undefined;
        delete this.apiListReq.targetSite;
        this.queryAPIListBySrcUrlOrLabel();
      }
    });
  }

  async queryAllTargetSitList() {
    const code = ['target_site_search'];
    const dict = await this.toolService.getDict(code);
    const ref = this.dialogService.open(TargetSiteListComponent, {
      header: dict['target_site_search'],
      width: '700px',
    });

    ref.onClose.subscribe((res) => {
      if (res) {
        this.apiListReq.targetSite = res.length > 0 ? res : undefined;
        delete this.apiListReq.labelList;
        this.queryAPIListBySrcUrlOrLabel();
      }
    });
  }

  async labelReset() {
    const code = ['button.label_reset'];
    const dict = await this.toolService.getDict(code);
    const ref = this.dialogService.open(LabelResetComponent, {
      header: dict['button.label_reset'],
      width: '700px',
      data: {
        data: this.selected,
      },
    });

    ref.onClose.subscribe((res) => {
      if (res) {
        let req = {
          apiList: this.selected.map((data) => {
            return {
              apiKey: data.apiKey,
              moduleName: data.moduleName,
            };
          }),
          labelList: res,
        } as AA0431Req;

        this.apiServer.batchLabelReset(req).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.success', 'button.label_reset'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['button.label_reset']}`,
              detail: `${dict['button.label_reset']} ${dict['message.success']}`,
            });
            this.queryAPIListBySrcUrlOrLabel();
          }
        });
      }
    });
  }

  async changePage(action: string = 'query', rowData?: any) {
    const code = ['api_url_setting', 'button.preview'];
    const dict = await this.toolService.getDict(code);

    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'api_url_setting':
        this.currentTitle = `${this.title} > ${dict['api_url_setting']}`;
        this.pageNum = 2;
        break;
      case 'preview':
        this.currentTitle = `${this.title} > ${dict['button.preview']}`;
        this.pageNum = 3;
        break;
      default:
        break;
    }
  }

  async batchNoOauthModify(noAuth: boolean) {
    const code = [
      'button.enable',
      'button.disable',
      'noauth.active',
      'noauth.inactive',
    ];
    const dict = await this.toolService.getDict(code);

    this.confirmationService.confirm({
      key: 'cd',
      header: noAuth ? dict['button.enable'] : dict['button.disable'],
      message: (noAuth ? dict['noauth.active'] : dict['noauth.inactive']) + '?',
      accept: () => {
        let req = {
          apiList: this.selected.map((data) => {
            return {
              apiKey: data.apiKey,
              moduleName: data.moduleName,
            };
          }),
          noOauth: noAuth,
        } as AA0430Req;
        this.apiServer.batchNoOauthModify(req).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.success', 'message.update'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']} API No Auth Suatus`,
              detail: `${dict['message.update']} ${dict['message.success']}`,
            });
          }
          this.queryAPIListBySrcUrlOrLabel();
        });
      },
    });
  }

  procPreviewPage(req: AA0424Req) {
    this.apiServer.temporaryByModifyBatch(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        let req_0425 = {
          tempFileName: res.RespBody.tempFileName,
          refId: res.RespBody.refId,
        } as AA0425Req;
        this.previewByModifyBatch(req_0425);
      }
      // else {
      //   this.changePage();
      // }
    });
  }

  previewByModifyBatch(req: AA0425Req, isMore: boolean = false) {
    this.previewDataReq = req;
    this.apiServer.previewByModifyBatch(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (isMore) {
          this.previewData = this.previewData.concat(res.RespBody.apiList);
        } else {
          this.previewData = res.RespBody.apiList;
        }
        this.changePage('preview');
      }
    });
  }

  moreApiListData() {
    let req = {
      tempFileName: this.previewDataReq?.tempFileName,
      sort: this.previewData[this.previewData.length - 1].sort,
      refId: this.previewDataReq?.refId,
    } as AA0425Req;

    this.previewByModifyBatch(req, true);
  }

  // 儲存批量修改的內容
  batchModify() {
    let req = {
      tempFileName: this.previewDataReq?.tempFileName,
      refId: this.previewDataReq?.refId,
    } as AA0426Req;
    this.apiServer.batchModify(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (res.RespBody.errMsg) {
          // 呈現錯誤訊息
          this.errList = res.RespBody.errList;
          this.errMsg = res.RespBody.errMsg;
          this.setErrListStatus(true);
        } else {
          this.queryAPIListBySrcUrlOrLabel();
          this.changePage();
        }
      }
    });
  }

  async updateAPIStatus(action: string = 'enable') {
    const code = ['button.enable', 'button.disable'];
    const dict = await this.toolService.getDict(code);

    this.translate
      .get('alert.text.actions', {
        statusText:
          action == 'enable' ? dict['button.enable'] : dict['button.disable'],
      })
      .subscribe((msg) => {
        this.confirmationService.confirm({
          key: 'cd',
          header:
            action == 'enable' ? dict['button.enable'] : dict['button.disable'],
          message: msg,
          accept: () => {
            let req = {
              ignoreAlert: 'Y', //啟用/停用直接帶入Y，若是刪除才要再確認
              apiList: this.selected.map((data) => {
                return {
                  moduleName: data.moduleName,
                  apiKey: data.apiKey,
                };
              }),
              apiStatus: action == 'enable' ? '1' : '2',
            } as AA0303Req;
            this.apiService.updateAPIStatus_1(req).subscribe(async (res) => {
              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                const code = [
                  'message.success',
                  'message.update',
                  'api_status',
                ];
                const dict = await this.toolService.getDict(code);
                this.messageService.add({
                  severity: 'success',
                  summary: `${dict['message.update']} ${dict['api_status']}`,
                  detail: `${dict['message.update']} ${dict['message.success']}`,
                });
                this.queryAPIListBySrcUrlOrLabel();
                this.changePage();
              }
            });
          },
        });
      });
  }

  // 批量存檔有錯誤時呈現錯誤窗內容，關窗後導回query頁
  setErrListStatus(status: boolean = false) {
    this.showErrMsg = status;
    if (!status) {
      this.queryAPIListBySrcUrlOrLabel();
      this.changePage();
    }
  }

  async resetHandlePolicyProc() {
    const code = [
      'fail_handle_policy.reset',
      'message.success',
      'fail_handle_policy.cfm_reset',
    ];
    const dict = await this.toolService.getDict(code);
    const ref = this.dialogService.open(FailHandlePolicyComponent, {
      header: dict['fail_handle_policy.reset'],
      width: '700px',
      data: {
        data: this.selected,
      },
    });

    ref.onClose.subscribe((res) => {
      if (res) {
        // console.log(res);

        this.apiService.batchFailHandlePolicy(res).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['fail_handle_policy.reset']}`,
              detail: `${dict['fail_handle_policy.reset']} ${dict['message.success']}`,
            });
            this.queryAPIListBySrcUrlOrLabel();
          }
        });
      }
    });
  }


}
