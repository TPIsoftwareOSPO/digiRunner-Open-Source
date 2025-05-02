import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { ToolService } from 'src/app/shared/services/tool.service';
import { EmApptJobStatus, FormOperate } from 'src/app/models/common.enum';
import { DPB0084Req } from 'src/app/models/api/CertificateAuthorityService/dpb0084.interface';
import { DPB0083Req, DPB0083RespItem } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { DPB0084certItem } from 'src/app/models/api/CertificateAuthorityService/dpb0084.interface';
import { ClientCAService } from 'src/app/shared/services/api-certificate-authority.service';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { interval, Observable, of } from 'rxjs';
import { delay, flatMap, switchMap, tap } from 'rxjs/operators';
import { CusService } from 'src/app/shared/services/api-cus.service';
import { CUS0001Req } from 'src/app/models/api/CertificateAuthorityService/cus0001.interface';
import { CUS0003CgRespBodyItem, CUS0003Req as ReqbodyInvoke, ReqCUS0003 as RequestInvoke, ResCUS0003 as ResponseInvoke } from 'src/app/models/api/CertificateAuthorityService/cus0003.invoke.interface';
import { CUS0003Req } from 'src/app/models/api/CertificateAuthorityService/cus0003.loopstatus.interface';
import { ResCUS0003 as ResponseResult } from 'src/app/models/api/CertificateAuthorityService/cus0003.result.interface';

@Component({
    selector: 'app-np0202',
    templateUrl: './np0202.component.html',
    styleUrls: ['./np0202.component.css'],
    providers: [ClientService]
})
export class Np0202Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;
    @ViewChild('error_messages') error_messages!: ElementRef<any>;

    form: FormGroup;
    formOperate = FormOperate;
    cols: { field: string; header: string; }[] = [];
    rowcount: number = 0;
    dialogTitle: string = '';
    dataList: Array<DPB0083RespItem> = new Array<DPB0083RespItem>();
    colETB: { field: string; header: string; }[] = [];
    activeIndex: EmTabName = EmTabName.CLIENT_SEARCH;
    intervalSubscription: any;
    statusName: any;
    emPageBlock = EmPageBlock;
    pageNum = EmPageBlock.QUERYPAGE;
    clientData?: FormParams;
    caData?: DPB0084certItem;
    cusTitle: string[] = [];
    isCusEnable: string = 'N';
    placeholder: string = '';
    apptJobId: number = 0;
    stackTrace: string = '';
    cgRespBody: string = '';
    cards?: CUS0003CgRespBodyItem[];
    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private clientCA: ClientCAService,
        private clientService: ClientService,
        private cusService: CusService
    ) {
        super(route, tr);

        this.form = this.fb.group({
          keyword: new FormControl(''),
          interface_keyword: new FormControl(''),
        });
    }

    async ngOnInit() {

        this.init();
        const codes = ['message.client', 'button.search', 'button.detail', 'cert_detail','client_cert_list'];
        const dicts = await this.tool.getDict(codes);
        this.cusTitle = [
            this.title,
            `${this.title} > ${dicts['client_cert_list']}`,
            `${this.title} > ${dicts['client_cert_list']} > ${dicts['cert_detail']}`
        ]
        this.otherSettings().then();
    }
    private async otherSettings() {
        this.clientCA.queryCusEnable().subscribe(async res1 => {
            this.isCusEnable = res1.RespBody.isCusEnable;
            if (this.isCusEnable === 'Y') {
                this.clientCA.queryCusUrl().subscribe(res2 => {
                    this.cusService.basePath = res2.RespBody.cusUrl;
                    const settingNo = this.tool.Base64Encoder(this.tool.BcryptEncoder('MSG'));
                    const subsettingNo = this.tool.Base64Encoder(this.tool.BcryptEncoder('SEARCH_WORD'));
                    this.cusService.querySettings({ settingNo: settingNo + ',2', subsettingNo: subsettingNo + ',0', isDefault: 'N' } as CUS0001Req).subscribe(res3 => this.placeholder = res3.RespBody.cus0001Items[0].param1)
                })
            }

        })

    }

    async init() {
        const codes = ['client_id', 'client_name', 'client_alias'];
        const dict = await this.tool.getDict(codes);
        this.cols = [
            { field: 'clientId', header: dict['client_id'] },
            { field: 'clientName', header: dict['client_name'] },
            { field: 'clientAlias', header: dict['client_alias'] }
        ];
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as DPB0083Req;
        this.clientService.queryClientListByLike_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
        this.colETB = [
            { field: 'tspId', header: 'TSP ID' },
            { field: 'dy', header: '動態展開...' },
        ]
    }

    submitForm() {
        switch (this.activeIndex) {
            case EmTabName.CLIENT_SEARCH:
                this.clientSubmitForm();
                break;
            case EmTabName.INTERFACE_SEARCH:
                this.interfaceSubmitForm();
                break;
        }

    }
    private clientSubmitForm() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as DPB0083Req;
        this.clientService.queryClientListByLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }
    private interfaceSubmitForm() {
        const keyword = this.interface_keyword!.value;
        const req = { tspid: keyword } as ReqbodyInvoke;
        this.cusService.queryQmInvokeCUS0003(req).subscribe(r => {
            //有apptJobId，則為Mock調用，否則為直接調用
            if (r.RespBody.apptJobId) {
                this.apptJobId = r.RespBody.apptJobId;
                // this.apptJobId = 41763;
                this.statusName = '處理中...';
                this.loopStatusCUS0003();
            } else {
                // (b). 因 按下 [介接資料搜尋], call CUS0003?invoke
                // 得到 CUS0003Resp.showUI.cgRespBody, 顯示在畫面
                if (r.RespBody.showUI && r.RespBody.showUI.cgRespBody) {
                    this.cards = r.RespBody.showUI.cgRespBody.data;
                }
            }
        })
    }
    private loopStatusCUS0003() {
        this.intervalSubscription = interval(1000)
            .pipe(
                switchMap(() => this.cusService.queryQmLoopStatusCUS0003({ apptJobId: this.apptJobId } as CUS0003Req))
            )
            .subscribe(async data => {
                // 每秒執行cus0003loopstats api一次
                // 取得 CUS0003Resp.commLoopStatus的 status(預約工作狀態), 判斷[狀態不是 "D" (完成) 或 "E" (失敗) 或 "C" (取消)], 則迴圈繼續
                // 若離開迴圈 且 狀態="D", call CUS0003?result
                if ([EmApptJobStatus.完成, EmApptJobStatus.失敗, EmApptJobStatus.取消].includes(data.RespBody.commLoopStatus.status)) {
                    this.stop();
                    if (data.RespBody.commLoopStatus.status === EmApptJobStatus.完成) {
                        await this.resultCUS0003().toPromise();
                    }
                }
                this.stackTrace = data.RespBody.commLoopStatus.stackTrace;
                this.statusName = data.RespBody.commLoopStatus.statusName;
            })
    }
    resultCUS0003(): Observable<ResponseResult> {
        return this.cusService.queryQmResultCUS0003({ apptJobId: this.apptJobId } as CUS0003Req).pipe(
            tap(r => {
                // 若離開迴圈 且 狀態="D", call CUS0003?result ,
                // 得到 CUS0003Resp.result.commLoopStatus 內容,顯示在畫面預約工作狀態
                // 得到 CUS0003Resp.result.showUI.cgRespBody 內容,顯示在畫面全景回覆資料
                this.statusName = r.RespBody.result.commLoopStatus.statusName;
                this.cgRespBody = JSON.stringify(r.RespBody.result.showUI.cgRespBody);
                this.stackTrace = r.RespBody.result.commLoopStatus.stackTrace;
                if (r.RespBody.result.showUI && r.RespBody.result.showUI.cgRespBody) {
                    this.cards = r.RespBody.result.showUI.cgRespBody.data;
                }
            })
        )
    }

    stop() {
        if (this.intervalSubscription) this.intervalSubscription.unsubscribe();
    }

    redirectPrev() {
        this.pageNum = this.emPageBlock.CLIENT_DETAIL;
        this.title = this.cusTitle[this.pageNum];
    }
    redirectList() {
        this.pageNum = this.emPageBlock.QUERYPAGE;
        this.title = this.cusTitle[this.pageNum];
    }
    async showDialog(rowData: DPB0083RespItem) {
        let ReqBody = {
            clientId: rowData.clientId,
            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
        } as DPB0084Req;
        // this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
        //     if (this.tool.checkDpSuccess(res.ResHeader)) {
        //         this.pageNum = this.emPageBlock.CLIENT_DETAIL;
        //         this.title = this.cusTitle[this.pageNum];
        //         // this.dialogTitle = dicts['dialog.detail_query'];
        //         let data: FormParams = {
        //             data: { type: 'JWE', clientId: rowData.clientId, detail: res.RespBody.certList },
        //             displayInDialog: true
        //         }
        //         this.clientData = data;
        //         // this._dialog.open(ClientCAComponent, data);

        //     }
        //     else {
        //         // this.dialogTitle = dicts['dialog.detail_query'];
        //         // let data: FormParams = {
        //         //     data: { type: 'JWE', clientId: rowData.clientId, detail: new Array<DPB0084certItem>() },
        //         //     displayInDialog: true
        //         // }
        //         // this.clientData = data;
        //         // this._dialog.open(ClientCAComponent, data);
        //     }
        // });
        this.pageNum = this.emPageBlock.CLIENT_DETAIL;
        this.title = this.cusTitle[this.pageNum];
        // this.dialogTitle = dicts['dialog.detail_query'];
        let data: FormParams = {
            data: { type: 'JWE', clientId: rowData.clientId, rowData: rowData,ReqBody:ReqBody },
            displayInDialog: true
        }
        this.clientData = data;
    }
    rowDataHandler(res: DPB0084certItem) {
        this.pageNum = this.emPageBlock.CA_DETAIL;
        this.title = this.cusTitle[this.pageNum];
        this.caData = res;
    }
    moreData() {
        let ReqBody = {
            clientId: this.dataList[this.dataList.length - 1].clientId,
            keyword: this.keyword!.value
        } as DPB0083Req;
        this.clientService.queryClientListByLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }
    restart(evt) {
        this.stackTrace = '';
        this.cgRespBody = '';
        this.resultCUS0003().subscribe();
    }
    switchErrorMessage(evt) {
        $(this.error_messages.nativeElement).toggleClass('hide');
    }

    headerReturn() {
      // console.log(this.pageNum === EmPageBlock.CLIENT_DETAIL)
      if(this.pageNum === EmPageBlock.CLIENT_DETAIL)
      {
        this.redirectList();
      }
      else if(this.pageNum === EmPageBlock.CA_DETAIL)
      {
        this.redirectPrev();
      }
    }

    public get keyword() { return this.form.get('keyword'); };
    public get interface_keyword() { return this.form.get('interface_keyword'); };

}
enum EmTabName {
    CLIENT_SEARCH = 0, INTERFACE_SEARCH = 1
}
enum EmPageBlock {
    QUERYPAGE, CLIENT_DETAIL, CA_DETAIL
}
