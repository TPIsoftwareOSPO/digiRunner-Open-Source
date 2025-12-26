import { EmInvokeCode } from './../../../../models/common.enum';
import { Component, OnInit, Input, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { DPB0084certItem, DPB0084Req } from 'src/app/models/api/CertificateAuthorityService/dpb0084.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { EmApptJobStatus, FormOperate, EmApptJobType } from 'src/app/models/common.enum';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { CADetailComponent } from '../ca-detail/ca-detail.component';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0086Req } from 'src/app/models/api/CertificateAuthorityService/dpb0086.interface';
import { ClientCAService } from 'src/app/shared/services/api-certificate-authority.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0085Req } from 'src/app/models/api/CertificateAuthorityService/dpb0085.interface';
import * as dayjs from 'dayjs';
import { CusService } from 'src/app/shared/services/api-cus.service';
import { interval, timer } from 'rxjs';
import { switchMap, takeUntil, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { CUS0003Req as ReqBodyCUS0003Invoke, CUS0003Resp as ResBodyCUS0003Invoke, CUS0003ShowUI, ReqCUS0003, ResCUS0003 as ResCUS0003Invoke, CUS0003CgRespBody, CUS0003CgRespBodyItem } from 'src/app/models/api/CertificateAuthorityService/cus0003.invoke.interface';
import { CUS0004CgRespBodyItem, CUS0004Req as ReqBodyCUS0004Invoke, ReqCUS0004, ResCUS0004 as ResCUS0004Invoke } from 'src/app/models/api/CertificateAuthorityService/cus0004.invoke.interface';
import { CUS0005Req as ReqBodyCUS0005Invoke, ReqCUS0005, ResCUS0005 as ResCUS0005Invoke } from 'src/app/models/api/CertificateAuthorityService/cus0005.invoke.interface';
import { CUS0006Req as ReqBodyCUS0006Invoke, ReqCUS0006, ResCUS0006 as ResCUS0006Invoke } from 'src/app/models/api/CertificateAuthorityService/cus0006.invoke.interface';
import { CUS0007Req as ReqBodyCUS0007Invoke, ReqCUS0007, ResCUS0007 as ResCUS0007Invoke } from 'src/app/models/api/CertificateAuthorityService/cus0007.invoke.interface';
import { CUS0003Req as ReqBodyCUS0003LoopStatus, CUS0003Resp as ResBodyCUS0003LoopStatus, ResCUS0003 as ResCUS0003LoopStatus, CusCommLoopStatus } from 'src/app/models/api/CertificateAuthorityService/cus0003.loopstatus.interface';
import { CUS0004Req as ReqBodyCUS0004LoopStatus, ResCUS0004 as ResCUS0004LoopStatus } from 'src/app/models/api/CertificateAuthorityService/cus0004.loopstatus.interface';
import { CUS0005Req as ReqBodyCUS0005LoopStatus, ResCUS0005 as ResCUS0005LoopStatus } from 'src/app/models/api/CertificateAuthorityService/cus0005.loopstatus.interface';
import { CUS0006Req as ReqBodyCUS0006LoopStatus, ResCUS0006 as ResCUS0006LoopStatus } from 'src/app/models/api/CertificateAuthorityService/cus0006.loopstatus.interface';
import { CUS0007Req as ReqBodyCUS0007LoopStatus, ResCUS0007 as ResCUS0007LoopStatus } from 'src/app/models/api/CertificateAuthorityService/cus0007.loopstatus.interface';
import { CUS0003Req as ReqBodyCUS0003Result, CUS0003Resp as ResBodyCUS0003Result, ResCUS0003 as ResCUS0003Result, CUS0003Result } from 'src/app/models/api/CertificateAuthorityService/cus0003.result.interface';
import { CUS0004Req as ReqBodyCUS0004Result, ResCUS0004 as ResCUS0004Result } from 'src/app/models/api/CertificateAuthorityService/cus0004.result.interface';
import { CUS0005Req as ReqBodyCUS0005Result, ResCUS0005 as ResCUS0005Result } from 'src/app/models/api/CertificateAuthorityService/cus0005.result.interface';
import { CUS0006Req as ReqBodyCUS0006Result, ResCUS0006 as ResCUS0006Result } from 'src/app/models/api/CertificateAuthorityService/cus0006.result.interface';
import { CUS0007Req as ReqBodyCUS0007Result, ResCUS0007 as ResCUS0007Result } from 'src/app/models/api/CertificateAuthorityService/cus0007.result.interface';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-client-ca',
    templateUrl: './client-ca.component.html',
    styleUrls: ['./client-ca.component.css'],
    providers: [ConfirmationService]
})
export class ClientCAComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;
    @ViewChild('upload_file') upload_file!: ElementRef;
    @Input() data?: FormParams;
    @Input() close?: Function;
    @Input() isCusEnable?: string;
    @Output() rowDataHandler: EventEmitter<any> = new EventEmitter();

    cols: { field: string; header: string; }[] = [];
    certList: Array<DPB0084certItem> = new Array<DPB0084certItem>();
    rowcount: number = 0;
    dialogTitle: string = '';
    currentCert?: DPB0084certItem;
    currentClientId: string = '';
    formOperate = FormOperate;
    fileName: string = '';
    _fileName: string = '';
    fileContent: string = '';
    today: Date = new Date();
    colETB: { field: string; header: string; }[] = [];
    selectedAction?: EmApptJobType;
    apptJobId: number = 0;
    intervalSubscription: any;
    statusName: string = '';
    stackTrace: string = '';
    cgRespBody: string = '';
    DPB0085ReqObject?: DPB0085Req;
    DPB0084certItem?: DPB0084certItem;
    cards?: CUS0004CgRespBodyItem[];

    constructor(
        private tool: ToolService,
        private message: MessageService,
        private clientCA: ClientCAService,
        private alert: AlertService,
        private cusService: CusService,
        private route: ActivatedRoute,
        private confirmationService:ConfirmationService
    ) { }

    async ngOnInit() {
        const codes = ['ca_file_name', 'ca_create_time', 'ca_expired_time'];
        const dict = await this.tool.getDict(codes);
        this.cols = [
            { field: 'clientFileName', header: dict['ca_file_name'] },
            { field: 'createAt', header: dict['ca_create_time'] },
            { field: 'expiredAt', header: dict['ca_expired_time'] }
        ];
        this.colETB = [
            { field: 'tspId', header: '憑證ID' },
            { field: 'dy', header: '動態展開...' },
        ]
        this.currentClientId = this.data?.data.clientId;
        this.rowcount = this.certList.length;

        this.clientCA.queryClientByCid(this.data?.data.ReqBody).subscribe(res => {
            if (res.RespBody && res.RespBody.certList) this.certList = res.RespBody.certList;
        });



    }

    highlight(value: string): string {
        if (this.today.getTime() - dayjs(value).toDate().getTime() > 2592000000) { // 超過30天
            return '#FF0000';
        }
        else if (this.today.getTime() - dayjs(value).toDate().getTime() <= 2592000000 && 0 <= this.today.getTime() - dayjs(value).toDate().getTime()) { // 30天內
            return '#FF8000';
        }
        else {
            return 'unset';
        }
    }

    async fileChange(files: FileList) {
        const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting', 'upload_fail', 'message.ca_file_limit', 'plz_del_one', 'limit_pem', 'message.upload', 'certificate_authority', 'message.success'];
        const dict = await this.tool.getDict(code);
        if (files && files[0]) {
        let _split = files.item(0)!.name.split('.');
        if (_split[_split.length - 1] != 'pem') {
            this.alert.ok(dict['upload_fail'], dict['limit_pem'], undefined);
            return;
        }
        if (this.certList.length == 2) {
            this.alert.ok(dict['upload_fail'], `${dict['message.ca_file_limit']}，${dict['plz_del_one']}`, undefined);
            return;
        }
        this.message.add({ severity: 'success', summary: dict['uploading'], detail: `${dict['waiting']}!` });
        let fileReader = new FileReader();
        fileReader.onloadend = () => {
            this._fileName = files.item(0)!.name;
            let ReqBody = {
                clientId: this.currentClientId,
                fileName: this._fileName,
                fileContent: fileReader.result!.toString().split('base64,')[1],
                encodeCertType: this.convertEncodeCertType(this.data?.data.type)
            } as DPB0085Req;
            switch (this.data?.data.type) {
                case 'JWE':
                    if (this.certList.length > 0) {
                        let _clientCertId = this.certList[0].clientCertId;
                        let _lv = this.certList[0].lv;
                        let _lockVersions = {};
                        _lockVersions[_clientCertId!] = _lv
                        ReqBody.lockVersions = _lockVersions;
                    }
                    break;
                case 'TLS':
                    if (this.certList.length > 0) {
                        let _clientCert2Id = this.certList[0].clientCert2Id;
                        let _lv = this.certList[0].lv;
                        let _lockVersions = {};
                        _lockVersions[_clientCert2Id!] = _lv
                        ReqBody.lockVersions = _lockVersions;
                    }
                    break;
            }
            if (this.isCusEnable === 'Y') {
                this.selectedAction = EmApptJobType.UPLOAD;
                this.DPB0085ReqObject = ReqBody;
                this.invokeCUS0005(ReqBody);
            } else {
                this.clientCA.uploadClientCA(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this._fileName = '';
                        this.fileName = '';
                        this.message.add({ severity: 'success', summary: `${dict['message.upload']} ${dict['certificate_authority']}`, detail: `${dict['message.upload']} ${dict['message.success']}!` });
                        this.loadClientCA();
                    }
                });
            }
        }
        fileReader.readAsDataURL(files.item(0)!);
      }
    }

    openFileBrowser() {
        $('#fileName').click();
    }
    bindData(evt) {
        this.selectedAction = EmApptJobType.SHOW;
        this.invokeCUS0004(this.currentClientId);

    }
    // 憑證資料-------------------------------------------------------------------------------------------------------------
    invokeCUS0004(tspid: string) {
        this.stop();
        const req = {
            tspid: tspid
        } as ReqBodyCUS0004Invoke;
        this.cusService.queryQmInvokeCUS0004(req).subscribe(r => {
            //有apptJobId，則為Mock調用，否則為直接調用
            if (r.RespBody.apptJobId) {
                const apptJobIdCus0004 = this.apptJobId = r.RespBody.apptJobId;
                // const apptJobIdCus0004 = this.apptJobId = 41513;
                this.statusName = '處理中...';
                this.loopStatusCUS004(apptJobIdCus0004);
            } else {
                // (b). 因 按下 [介接資料搜尋], call CUS0003?invoke
                // 得到 CUS0003Resp.showUI.cgRespBody, 顯示在畫面
                if (r.RespBody.showUI && r.RespBody.showUI.cgRespBody) {
                    this.cards = r.RespBody.showUI.cgRespBody.data!;
                }
            }
        })
    }
    private loopStatusCUS004(apptJobIdCus0004: number) {
        this.intervalSubscription = interval(1000)
            .pipe(
                switchMap(() => this.cusService.queryQmLoopStatusCUS0004({ apptJobId: apptJobIdCus0004 } as ReqBodyCUS0004LoopStatus)),
            )
            .subscribe(async data => {
                // 每秒執行cus0003loopstats api一次
                // 取得 CUS0003Resp.commLoopStatus的 status(預約工作狀態), 判斷[狀態不是 "D" (完成) 或 "E" (失敗) 或 "C" (取消)], 則迴圈繼續
                // 若離開迴圈 且 狀態="D", call CUS0003?result
                if ([EmApptJobStatus.完成, EmApptJobStatus.失敗, EmApptJobStatus.取消].includes(data.RespBody.commLoopStatus.status)) {
                    this.stop();
                    if (data.RespBody.commLoopStatus.status === EmApptJobStatus.完成) {
                        await this.resultCUS0004(apptJobIdCus0004).toPromise();
                    }
                }
                this.stackTrace = data.RespBody.commLoopStatus.stackTrace;
                this.statusName = data.RespBody.commLoopStatus.statusName;
            })
    }
    resultCUS0004(apptJobIdCus0004: number): Observable<ResCUS0004Result> {
        return this.cusService.queryQmResultCUS0004({ apptJobId: apptJobIdCus0004 } as ReqBodyCUS0004Result).pipe(
            tap(r => {
                // 若離開迴圈 且 狀態="D", call CUS0003?result ,
                // 得到 CUS0003Resp.result.commLoopStatus 內容,顯示在畫面預約工作狀態
                // 得到 CUS0003Resp.result.showUI.cgRespBody 內容,顯示在畫面全景回覆資料
                this.statusName = r.RespBody.result.commLoopStatus.statusName;
                this.cgRespBody = JSON.stringify(r.RespBody.result.showUI.cgRespBody);
                this.stackTrace = r.RespBody.result.commLoopStatus.stackTrace;
                // this.cards = r.RespBody.result.showUI.cgRespBody.data.concat(r.RespBody.result.showUI.cgRespBody.data).concat(r.RespBody.result.showUI.cgRespBody.data).concat(r.RespBody.result.showUI.cgRespBody.data);
                if (r.RespBody.result.showUI && r.RespBody.result.showUI.cgRespBody) {
                    this.cards = r.RespBody.result.showUI.cgRespBody.data!;
                }

            })
        )
    }

    // 上傳憑證-------------------------------------------------------------------------------------------------------------
    invokeCUS0005(reqBody: DPB0085Req) {
        this.stop();
        const req = {
            tspid: reqBody.clientId,
            fileContent: reqBody.fileContent
        } as ReqBodyCUS0005Invoke;
        this.cusService.queryQmInvokeCUS0005(req).subscribe(r => {
            //有apptJobId，則為Mock調用，否則為直接調用
            if (r.RespBody.apptJobId) {
                const apptJobIdCus0005 = this.apptJobId = r.RespBody.apptJobId;
                // const apptJobIdCus0005 = this.apptJobId = 42006;
                this.statusName = '處理中...';
                this.loopStatusCUS005(apptJobIdCus0005, reqBody);
            } else {
                // (b). 因 按下 [介接資料搜尋], call CUS0003?invoke
                // 得到 CUS0003Resp.showUI.cgRespBody, 顯示在畫面
                this.cgRespBody = JSON.stringify(r.RespBody.showUI?.cgRespBody);
                //實作上傳
                if (r.RespBody.showUI?.cgRespBody.code == EmInvokeCode.成功) {
                    this.actualUpload(r.RespBody.showUI.cgRespBody.data!, reqBody);
                }

            }
        })
    }
    private loopStatusCUS005(apptJobIdCus0005: number, reqBody: DPB0085Req) {
        this.intervalSubscription = interval(1000)
            .pipe(
                switchMap(() => this.cusService.queryQmLoopStatusCUS0005({ apptJobId: apptJobIdCus0005 } as ReqBodyCUS0005LoopStatus))
            )
            .subscribe(async data => {
                // 每秒執行cus0003loopstats api一次
                // 取得 CUS0003Resp.commLoopStatus的 status(預約工作狀態), 判斷[狀態不是 "D" (完成) 或 "E" (失敗) 或 "C" (取消)], 則迴圈繼續
                // 若離開迴圈 且 狀態="D", call CUS0003?result
                if ([EmApptJobStatus.完成, EmApptJobStatus.失敗, EmApptJobStatus.取消].includes(data.RespBody.commLoopStatus.status)) {
                    this.stop();
                    if (data.RespBody.commLoopStatus.status === EmApptJobStatus.完成) {
                        this.resultCUS0005(apptJobIdCus0005).subscribe(res => {
                            if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                                this.cusService.uploadClientCert({
                                    clientId: reqBody.clientId,
                                    fileContent: reqBody.fileContent,
                                    fileName: reqBody.fileName,
                                    cusCertId: res.RespBody.result.showUI.cgRespBody.data!
                                }).subscribe(res2 => {
                                    let ReqBody = {
                                        clientId: reqBody.clientId,
                                        encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
                                    } as DPB0084Req;
                                    this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
                                        if (res.RespBody && res.RespBody.certList) this.certList = res.RespBody.certList;
                                    });
                                });
                            }
                        })
                    }
                }
                this.stackTrace = data.RespBody.commLoopStatus.stackTrace;
                this.statusName = data.RespBody.commLoopStatus.statusName;
            })
    }
    resultCUS0005(apptJobIdCus0005: number): Observable<ResCUS0005Result> {
        return this.cusService.queryQmResultCUS0005({ apptJobId: apptJobIdCus0005 } as ReqBodyCUS0005Result).pipe(
            tap(r => {
                // 若離開迴圈 且 狀態="D", call CUS0003?result ,
                // 得到 CUS0003Resp.result.commLoopStatus 內容,顯示在畫面預約工作狀態
                // 得到 CUS0003Resp.result.showUI.cgRespBody 內容,顯示在畫面全景回覆資料 ==>未實作
                this.statusName = r.RespBody.result.commLoopStatus.statusName;
                this.cgRespBody = JSON.stringify(r.RespBody.result.showUI.cgRespBody);
            }),

        )
    }
    // 上傳實作:直接調用------------------------------------------------------------------------------------------------------
    actualUpload(cusCertId: string, reqBody: DPB0085Req) {
        this.cusService.uploadClientCert({
            clientId: reqBody.clientId,
            fileContent: reqBody.fileContent,
            fileName: reqBody.fileName,
            cusCertId: cusCertId
        }).subscribe(r => {
            if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                this.certList = new Array<DPB0084certItem>();
                let ReqBody = {
                    clientId: this.currentClientId,
                    encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
                } as DPB0084Req;
                this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
                    if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                        this.certList = res.RespBody.certList;
                    }
                });
            }
        });
    }
    // 刪除憑證-------------------------------------------------------------------------------------------------------------
    invokeCUS0006(clientCertId: string) {
        this.stop();
        const req = {
            clientCertId: clientCertId
        } as ReqBodyCUS0006Invoke;
        this.cusService.queryQmInvokeCUS0006(req).subscribe(r => {
            //有apptJobId，則為Mock調用，否則為直接調用
            if (r.RespBody.apptJobId) {
                const apptJobIdCus0006 = this.apptJobId = r.RespBody.apptJobId;
                this.statusName = '處理中...';
                this.loopStatusCUS006(apptJobIdCus0006);
            } else {
                // (b). 因 按下 [介接資料搜尋], call CUS0003?invoke
                // 得到 CUS0003Resp.showUI.cgRespBody, 顯示在畫面
                this.cgRespBody = JSON.stringify(r.RespBody.showUI?.cgRespBody);
                //刪除實作
                if (r.RespBody.showUI?.cgRespBody.code == EmInvokeCode.成功) {
                    this.actualDelete();
                }
            }
        })
    }
    private loopStatusCUS006(apptJobIdCus0006: number) {
        this.intervalSubscription = interval(1000)
            .pipe(
                switchMap(() => this.cusService.queryQmLoopStatusCUS0006({ apptJobId: apptJobIdCus0006 } as ReqBodyCUS0005LoopStatus))
            )
            .subscribe(async data => {
                // 每秒執行cus0003loopstats api一次
                // 取得 CUS0003Resp.commLoopStatus的 status(預約工作狀態), 判斷[狀態不是 "D" (完成) 或 "E" (失敗) 或 "C" (取消)], 則迴圈繼續
                // 若離開迴圈 且 狀態="D", call CUS0003?result
                if ([EmApptJobStatus.完成, EmApptJobStatus.失敗, EmApptJobStatus.取消].includes(data.RespBody.commLoopStatus.status)) {
                    this.stop();
                    if (data.RespBody.commLoopStatus.status === EmApptJobStatus.完成) {
                        const res = await this.resultCUS0006(apptJobIdCus0006).toPromise();
                        if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                            this.cusService.deleteClientCert({
                                clientId: this.currentClientId,
                                clientCertId: this.currentCert?.clientCertId as number
                            }).subscribe(r => {
                                let ReqBody = {
                                    clientId: this.currentClientId,
                                    encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
                                } as DPB0084Req;
                                this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
                                    if (res.RespBody && res.RespBody.certList) this.certList = res.RespBody.certList;
                                });
                            })
                        }
                    }
                }
                this.stackTrace = data.RespBody.commLoopStatus.stackTrace;
                this.statusName = data.RespBody.commLoopStatus.statusName;
            })
    }
    resultCUS0006(apptJobIdCus0006: number): Observable<ResCUS0006Result> {
        return this.cusService.queryQmResultCUS0006({ apptJobId: apptJobIdCus0006 } as ReqBodyCUS0006Result).pipe(
            tap(r => {
                // 若離開迴圈 且 狀態="D", call CUS0003?result ,
                // 得到 CUS0003Resp.result.commLoopStatus 內容,顯示在畫面預約工作狀態
                // 得到 CUS0003Resp.result.showUI.cgRespBody 內容,顯示在畫面全景回覆資料
                this.statusName = r.RespBody.result.commLoopStatus.statusName;
                this.stackTrace = r.RespBody.result.commLoopStatus.stackTrace;
                this.cgRespBody = JSON.stringify(r.RespBody.result.showUI.cgRespBody);
                //實作刪除
                if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                    this.actualDelete();
                }
            })
        )
    }
    // 刪除實作-------------------------------------------------------------------------------------------------------------
    actualDelete() {
        this.cusService.deleteClientCert({
            clientId: this.currentClientId,
            clientCertId: this.currentCert?.clientCertId as number
        }).subscribe(r => {
            if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                this.certList = new Array<DPB0084certItem>();
                let ReqBody = {
                    clientId: this.currentClientId,
                    encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
                } as DPB0084Req;
                this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
                    if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                        this.certList = res.RespBody.certList;
                    }
                });
            }
        });
    }
    // 下載憑證-------------------------------------------------------------------------------------------------------------
    invokeCUS0007(clientCertId: string, clientFileName: string) {
        this.stop();
        const req = {
            clientCertId: clientCertId
        } as ReqBodyCUS0007Invoke;
        this.cusService.queryQmInvokeCUS0007(req).subscribe(r => {
            //有apptJobId，則為Mock調用，否則為直接調用
            if (r.RespBody.apptJobId) {
                const apptJobIdCus0007 = this.apptJobId = r.RespBody.apptJobId;
                // const apptJobIdCus0007 = 42190;
                this.statusName = '處理中...';
                this.loopStatusCUS007(apptJobIdCus0007, clientFileName);
            } else {
                // (b). 因 按下 [介接資料搜尋], call CUS0003?invoke
                // 得到 CUS0003Resp.showUI.cgRespBody, 顯示在畫面
                this.cgRespBody = JSON.stringify(r.RespBody.showUI?.cgRespBody);
                //下載實作
                if (r.RespBody.showUI?.cgRespBody.code == EmInvokeCode.成功) {
                    this.downloadFile(r.RespBody.showUI.cgRespBody.data, clientFileName);
                }
            }
        })
    }
    private loopStatusCUS007(apptJobIdCus0007: number, clientFileName: string) {
        this.intervalSubscription = interval(1000)
            .pipe(
                switchMap(() => this.cusService.queryQmLoopStatusCUS0007({ apptJobId: apptJobIdCus0007 } as ReqBodyCUS0007LoopStatus)),
            )
            .subscribe(async data => {
                // 每秒執行cus0003loopstats api一次
                // 取得 CUS0003Resp.commLoopStatus的 status(預約工作狀態), 判斷[狀態不是 "D" (完成) 或 "E" (失敗) 或 "C" (取消)], 則迴圈繼續
                // 若離開迴圈 且 狀態="D", call CUS0003?result
                if ([EmApptJobStatus.完成, EmApptJobStatus.失敗, EmApptJobStatus.取消].includes(data.RespBody.cusCommLoopStatus.status)) {
                    this.stop();
                    if (data.RespBody.cusCommLoopStatus.status === EmApptJobStatus.完成) {
                        await this.resultCUS0007(apptJobIdCus0007, clientFileName).toPromise();
                    }
                }
                this.stackTrace = data.RespBody.cusCommLoopStatus.stackTrace;
                this.statusName = data.RespBody.cusCommLoopStatus.statusName;
            })
    }
    resultCUS0007(apptJobIdCus0007: number, clientFileName: string): Observable<ResCUS0007Result> {
        return this.cusService.queryQmResultCUS0007({ apptJobId: apptJobIdCus0007 } as ReqBodyCUS0007Result).pipe(
            tap(r => {
                // 若離開迴圈 且 狀態="D", call CUS0003?result ,
                // 得到 CUS0003Resp.result.commLoopStatus 內容,顯示在畫面預約工作狀態
                // 得到 CUS0003Resp.result.showUI.cgRespBody 內容,顯示在畫面全景回覆資料 ==>未實作
                this.statusName = r.RespBody.result.commLoopStatus.statusName;
                this.cgRespBody = JSON.stringify(r.RespBody.result.showUI.cgRespBody);
                if (r.RespBody.result.commLoopStatus.status === EmApptJobStatus.完成) {
                    this.downloadFile(r.RespBody.result.showUI.cgRespBody.data, clientFileName);
                }
            })
        )
    }
    // -------------------------------------------------------------------------------------------------------------
    stop() {
        if (this.intervalSubscription) this.intervalSubscription.unsubscribe();
    }
    async showDialog(rowData: DPB0084certItem, operation: FormOperate) {
        const codes = ['dialog.detail_query', 'cfm_del', 'ca_file_name','system_alert'];
        const dicts = await this.tool.getDict(codes);
        this.currentCert = rowData;
        switch (operation) {
            case FormOperate.detail:
                this.dialogTitle = dicts['dialog.detail_query'];
                //原本 np0204都是使用開窗的方式呈現 2022/12/30 註解 不以開窗呈現
                // if (this.route.snapshot.data['id'] && this.route.snapshot.data['id'] === 'np0204')
                //     // console.log(this.currentCert)
                //     this._dialog.open(CADetailComponent, this.currentCert);
                // else
                    this.rowDataHandler.emit(this.currentCert);
                break;
            case FormOperate.delete: {
                this.message.clear();
                // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: dicts['cfm_del'] });
                this.confirmationService.confirm({
                  header: dicts['system_alert'],
                  message: dicts['cfm_del'],
                  accept: () => {
                      this.onDeleteConfirm();
                  }
                });
            }
        }
    }

    loadClientCA() {
        this.certList = [];
        this.rowcount = this.certList.length;
        let ReqBody = {
            clientId: this.currentClientId,
            encodeCertType: this.convertEncodeCertType(this.data?.data.type)
        } as DPB0084Req;
        this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.certList = res.RespBody.certList;
                this.rowcount = this.certList.length;
            }
        });
    }

    onDeleteConfirm() {
        this.message.clear();
        if (this.isCusEnable === 'Y') {
            this.selectedAction = EmApptJobType.DELETE;
            this.invokeCUS0006(this.currentCert!.clientCertId!.toString());
        } else {
            let ReqBody = {
                clientId: this.currentCert?.clientId,
                encodeCertType: this.convertEncodeCertType(this.data?.data.type)
            } as DPB0086Req;
            switch (this.data?.data.type) {
                case 'JWE':
                    ReqBody.clientCertId = this.currentCert?.clientCertId
                    break;
                case 'TLS':
                    ReqBody.clientCert2Id = this.currentCert?.clientCert2Id
                    break;
            }
            this.clientCA.deleteClientCA(ReqBody).subscribe(async res => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                    const codes = ['message.delete', 'certificate_authority', 'message.success'];
                    const dicts = await this.tool.getDict(codes);
                    this.message.clear();
                    this.message.add({ severity: 'success', summary: `${dicts['message.delete']} ${dicts['certificate_authority']}`, detail: `${dicts['message.delete']} ${dicts['message.success']}!` });
                    this.loadClientCA();
                }
            });
        }

    }

    onReject() {
        this.message.clear();
    }

    convertEncodeCertType(type: string): string {
        switch (type) {
            case 'JWE':
                return this.tool.Base64Encoder(this.tool.BcryptEncoder(type)) + ',' + 0
            case 'TLS':
                return this.tool.Base64Encoder(this.tool.BcryptEncoder(type)) + ',' + 1
            default:
              return '';
        }

    }
    download(rowData: DPB0084certItem) {
        if (this.isCusEnable === 'Y') {
            this.selectedAction = EmApptJobType.DOWNLOAD;
            this.DPB0084certItem = rowData;
            this.invokeCUS0007(rowData!.clientCertId!.toString(), rowData.clientFileName);
        } else {
            this.downloadFile(rowData.clientFileContent, rowData.clientFileName);
        }
    }

    downloadFile(clientFileContent: string, clientFileName: string) {
        let blob = new Blob([this.tool.Base64Decoder(clientFileContent)]);
        const reader = new FileReader();
        reader.onloadend = function () {
            // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
            //     window.navigator.msSaveBlob(blob, clientFileName);
            // }
            // else {
                const file = new File([blob], clientFileName);
                const url = window.URL.createObjectURL(file);
                const a = document.createElement('a');
                document.body.appendChild(a);
                a.setAttribute('style', 'display: none');
                a.href = url;
                a.download = clientFileName;
                a.click();
                window.URL.revokeObjectURL(url);
                a.remove();
            // }
        }
        reader.readAsText(blob);
    }
    async restart(evt) {
        this.stackTrace = '';
        this.cgRespBody = '';
        switch (this.selectedAction) {
            case EmApptJobType.UPLOAD:
                const codes = ['upload_fail', 'message.ca_file_limit', 'plz_del_one'];
                const dict = await this.tool.getDict(codes);
                if (this.certList.length >= 2) {
                    this.alert.ok(dict['upload_fail'], `${dict['message.ca_file_limit']}，${dict['plz_del_one']}`, undefined);
                    return;
                } else {
                    if (this.DPB0085ReqObject) {
                        this.resultCUS0005(this.apptJobId).subscribe(res => {
                            if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                                if (res.RespBody.result.commLoopStatus.status === EmApptJobStatus.完成) {
                                    this.cusService.uploadClientCert({
                                        clientId: this.DPB0085ReqObject!.clientId,
                                        fileContent: this.DPB0085ReqObject!.fileContent,
                                        fileName: this.DPB0085ReqObject!.fileName,
                                        cusCertId: res.RespBody.result.showUI.cgRespBody.data!
                                    }).subscribe(res2 => {
                                        let ReqBody = {
                                            clientId: this.DPB0085ReqObject!.clientId,
                                            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
                                        } as DPB0084Req;
                                        this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
                                            if (res.RespBody && res.RespBody.certList) this.certList = res.RespBody.certList;
                                        });
                                    });
                                }

                            }
                        })
                    }
                }

                break;
            case EmApptJobType.DELETE:
                this.resultCUS0006(this.apptJobId).subscribe(res => {
                    if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                        if (res.RespBody.result.commLoopStatus.status === EmApptJobStatus.完成) {
                            this.cusService.deleteClientCert({
                                clientId: this.currentClientId,
                                clientCertId: this.currentCert!.clientCertId!
                            }).subscribe(r => {
                                let ReqBody = {
                                    clientId: this.currentClientId,
                                    encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('JWE')) + ',' + 0
                                } as DPB0084Req;
                                this.clientCA.queryClientByCid_ignore1298(ReqBody).subscribe(res => {
                                    if (res.ResHeader.rtnCode === '1298') {
                                        this.certList = [];
                                    } else {
                                        if (res.RespBody && res.RespBody.certList) this.certList = res.RespBody.certList;
                                    }
                                });
                            })
                        }

                    }
                })

                break;
            case EmApptJobType.DOWNLOAD:
                if (this.DPB0084certItem) {
                    await this.resultCUS0007(this.apptJobId, this.DPB0084certItem.clientFileName).toPromise();
                }
                break;
            case EmApptJobType.SHOW:
                this.resultCUS0004(this.apptJobId).subscribe();
                break;
        }
    }
}


