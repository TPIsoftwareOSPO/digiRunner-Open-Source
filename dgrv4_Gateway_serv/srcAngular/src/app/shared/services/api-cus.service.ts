import { ToolService } from 'src/app/shared/services/tool.service';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { EmApptJobStatus, TxID } from 'src/app/models/common.enum';
import { CUS0003Req as ReqBodyCUS0003Invoke, CUS0003Resp as ResBodyCUS0003Invoke, CUS0003ShowUI, ReqCUS0003, ResCUS0003 as ResCUS0003Invoke, CUS0003CgRespBody, CUS0003CgRespBodyItem } from 'src/app/models/api/CertificateAuthorityService/cus0003.invoke.interface';
import { CUS0004Req as ReqBodyCUS0004Invoke, ReqCUS0004, ResCUS0004 as ResCUS0004Invoke } from 'src/app/models/api/CertificateAuthorityService/cus0004.invoke.interface';
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
import { CUS0001Req, CUS0001Resp, ReqCUS0001, ResCUS0001 } from 'src/app/models/api/CertificateAuthorityService/cus0001.interface';
import { CUS0008Req, ReqCUS0008, ResCUS0008 } from 'src/app/models/api/CertificateAuthorityService/cus0008.interface';
import { Observable, of } from 'rxjs';
import { BaseRes } from 'src/app/models/api/base.interface';
import { CUS0002Req, ReqCUS0002, ResCUS0002 } from 'src/app/models/api/CertificateAuthorityService/cus0002.interface';

@Injectable({ providedIn: 'root' })
export class CusService {

    private _basePath
    // public get basePath(): string {
    //     return 'dgr-cus-ETB_CG/11';
    // }
    public set basePath(value: string) {
        this._basePath = value;
    }
    constructor(
        private api: ApiBaseService,
        private toolService: ToolService
    ) {

    }
    /**
     * 查詢設定資料
     * @param ReqBody CUS0001Req
     * @returns
     */
    querySettings(ReqBody: CUS0001Req): Observable<ResCUS0001> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.CUS0001),
            ReqBody: ReqBody
        } as ReqCUS0001;
        const path = `${this._basePath}/CUS0001`;
        return this.api.excuteNpPost<ResCUS0001>(path, body);
        // return of({
        //     ResHeader: {},
        //     RespBody: {
        //         dataItem: {
        //             cusSettingId: '8',
        //             settingNo: 'MSG',
        //             settingName: '訊息',
        //             subsettingNo: 'SEARCH_WORD',
        //             subsettingName: '提示訊息',
        //             param1: 'TSP ID(例如：統一編號)，可以用*做萬用字元查詢'
        //         }
        //     } as CUS0001Resp
        // } as ResCUS0001)
    }
    /**
     * PEM檔-上傳/解析/save用戶憑證
     * @param ReqBody CUS0002Req
     * @returns
     */
    uploadClientCert(ReqBody: CUS0002Req): Observable<ResCUS0002> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.CUS0002),
            ReqBody: ReqBody
        } as ReqCUS0002;
        const path = `${this._basePath}/CUS0002`;
        return this.api.excuteNpPost<ResCUS0002>(path, body);
    }
    /**
     * 使用Mock調用或直接調用
     * @param ReqBody ResponseCUS0003Invoke
     * @returns Observable<ResponseInvoke>
     */
    queryQmInvokeCUS0003(ReqBody: ReqBodyCUS0003Invoke): Observable<ResCUS0003Invoke> {
        console.log('call cus0003 invoke');
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryQmInvokeCUS0003),
            ReqBody: ReqBody
        } as ReqCUS0003;
        const path = `${this._basePath}/CUS0003?invoke`;
        return this.api.excuteNpPost<ResCUS0003Invoke>(path, body);
        // return of({
        //     ResHeader: { "txSN": "1210317182949UHD5uh", "txDate": "20210317T182949+0800", "txID": "DPB0083", "rtnCode": "1100", "rtnMsg": null },
        //     RespBody: {
        //         apptJobId: 1001,
        //         showUI: {
        //             cgRespBody: {
        //                 code: '0',
        //                 message: '成功',
        //                 data: [
        //                     { tspid: '1000' } as CUS0003CgRespBodyItem
        //                 ]
        //             } as CUS0003CgRespBody
        //         } as CUS0003ShowUI
        //     } as ResBodyCUS0003Invoke
        // } as ResCUS0003Invoke)
    }
    /**
     * 查詢預約工作狀態
     * @param reqbodyLoopStatus ReqbodyLoopStatus
     * @returns Observable<ResponseLoopStatus>
     */
    count = 2
    queryQmLoopStatusCUS0003(reqbodyLoopStatus: ReqBodyCUS0003LoopStatus): Observable<ResCUS0003LoopStatus> {
        console.log('call cus0003 loopstatus');
        const path = `${this._basePath}/CUS0003?loopStatus&apptJobId=${reqbodyLoopStatus.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0003LoopStatus>(path,TxID.queryQmLoopStatusCUS0003);
        // this.count++;
        // if (this.count % 3 == 0) {
        //     return of({
        //         ResHeader: {},
        //         RespBody: {
        //             commLoopStatus: {
        //                 apptJobId: 1001,
        //                 status: EmApptJobStatus.等待,
        //                 statusName: '等待',
        //                 stackTrace: ''
        //             }
        //         } as ResBodyCUS0003LoopStatus
        //     } as ResCUS0003LoopStatus)
        // } else if (this.count % 3 == 1) {
        //     return of({
        //         ResHeader: {},
        //         RespBody: {
        //             commLoopStatus: {
        //                 apptJobId: 1001,
        //                 status: EmApptJobStatus.執行中,
        //                 statusName: '執行中',
        //                 stackTrace: ''
        //             }
        //         } as ResBodyCUS0003LoopStatus
        //     } as ResCUS0003LoopStatus)
        // } else {
        //     return of({
        //         ResHeader: {},
        //         RespBody: {
        //             commLoopStatus: {
        //                 apptJobId: 1001,
        //                 status: EmApptJobStatus.完成,
        //                 statusName: '完成',
        //                 stackTrace: ''
        //             }
        //         } as ResBodyCUS0003LoopStatus
        //     } as ResCUS0003LoopStatus)
        // }
    }
    /**
     * 查詢調用全景API回覆結果
     * @param reqbodyResult ReqbodyResult
     * @returns Observable<ResponseResult>
     */
    queryQmResultCUS0003(reqbodyResult: ReqBodyCUS0003Result): Observable<ResCUS0003Result> {
        console.log('call cus0003 result');
        const path = `${this._basePath}/CUS0003?result&apptJobId=${reqbodyResult.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0003Result>(path,TxID.queryQmResultCUS0003);
        // return of({
        //     ResHeader: {},
        //     RespBody: {
        //         result: {
        //             commLoopStatus: {
        //                 apptJobId: 1001,
        //                 status: EmApptJobStatus.完成,
        //                 statusName: '完成',
        //                 stackTrace: ''
        //             } as CusCommLoopStatus,
        //             showUI: {
        //                 cgRespBody: {
        //                     code: '0',
        //                     message: '成功',
        //                     data: [
        //                         { tspid: '1000' } as CUS0003CgRespBodyItem
        //                     ]
        //                 } as CUS0003CgRespBody
        //             } as CUS0003ShowUI
        //         } as CUS0003Result
        //     } as ResBodyCUS0003Result
        // } as ResCUS0003Result)
    }

    queryQmInvokeCUS0004(ReqBody: ReqBodyCUS0004Invoke): Observable<ResCUS0004Invoke> {
        console.log('call cus0004 invoke');
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryQmInvokeCUS0004),
            ReqBody: ReqBody
        } as ReqCUS0004;
        const path = `${this._basePath}/CUS0004?invoke`;
        return this.api.excuteNpPost<ResCUS0004Invoke>(path, body);
    }

    queryQmLoopStatusCUS0004(reqbodyLoopStatus: ReqBodyCUS0004LoopStatus): Observable<ResCUS0004LoopStatus> {
        console.log('call cus0004 loopstatus');
        const path = `${this._basePath}/CUS0004?loopStatus&apptJobId=${reqbodyLoopStatus.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0004LoopStatus>(path,TxID.queryQmLoopStatusCUS0004);
    }

    queryQmResultCUS0004(reqbodyResult: ReqBodyCUS0004Result): Observable<ResCUS0004Result> {
        console.log('call cus0004 result');
        const path = `${this._basePath}/CUS0004?result&apptJobId=${reqbodyResult.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0004Result>(path,TxID.queryQmResultCUS0004);
    }

    queryQmInvokeCUS0005(ReqBody: ReqBodyCUS0005Invoke): Observable<ResCUS0005Invoke> {
        console.log('call cus0005 invoke');
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryQmInvokeCUS0005),
            ReqBody: ReqBody
        } as ReqCUS0005;
        const path = `${this._basePath}/CUS0005?invoke`;
        return this.api.excuteNpPost<ResCUS0005Invoke>(path, body);
    }
    queryQmLoopStatusCUS0005(reqbodyLoopStatus: ReqBodyCUS0005LoopStatus): Observable<ResCUS0005LoopStatus> {
        console.log('call cus0005 loopstatus');
        const path = `${this._basePath}/CUS0005?loopStatus&apptJobId=${reqbodyLoopStatus.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0005LoopStatus>(path,TxID.queryQmLoopStatusCUS0005);
    }
    queryQmResultCUS0005(reqbodyResult: ReqBodyCUS0005Result): Observable<ResCUS0005Result> {
        console.log('call cus0005 result');
        const path = `${this._basePath}/CUS0005?result&apptJobId=${reqbodyResult.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0005Result>(path,TxID.queryQmResultCUS0005);
    }

    queryQmInvokeCUS0006(ReqBody: ReqBodyCUS0006Invoke): Observable<ResCUS0006Invoke> {
        console.log('call cus0006 invoke');
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryQmInvokeCUS0006),
            ReqBody: ReqBody
        } as ReqCUS0006;
        const path = `${this._basePath}/CUS0006?invoke`;
        return this.api.excuteNpPost<ResCUS0006Invoke>(path, body);
    }
    queryQmLoopStatusCUS0006(reqbodyLoopStatus: ReqBodyCUS0006LoopStatus): Observable<ResCUS0006LoopStatus> {
        console.log('call cus0006 loopstatus');
        const path = `${this._basePath}/CUS0006?loopStatus&apptJobId=${reqbodyLoopStatus.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0006LoopStatus>(path,TxID.queryQmLoopStatusCUS0006);
    }
    queryQmResultCUS0006(reqbodyResult: ReqBodyCUS0006Result): Observable<ResCUS0006Result> {
        console.log('call cus0006 result');
        const path = `${this._basePath}/CUS0006?result&apptJobId=${reqbodyResult.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0006Result>(path,TxID.queryQmResultCUS0006);
    }

    queryQmInvokeCUS0007(ReqBody: ReqBodyCUS0007Invoke): Observable<ResCUS0007Invoke> {
        console.log('call cus0007 invoke');
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryQmInvokeCUS0007),
            ReqBody: ReqBody
        } as ReqCUS0007;
        const path = `${this._basePath}/CUS0007?invoke`;
        return this.api.excuteNpPost<ResCUS0007Invoke>(path, body);
    }
    queryQmLoopStatusCUS0007(reqbodyLoopStatus: ReqBodyCUS0007LoopStatus): Observable<ResCUS0007LoopStatus> {
        console.log('call cus0007 loopstatus');
        const path = `${this._basePath}/CUS0007?loopStatus&apptJobId=${reqbodyLoopStatus.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0007LoopStatus>(path,TxID.queryQmLoopStatusCUS0007);
    }
    queryQmResultCUS0007(reqbodyResult: ReqBodyCUS0007Result): Observable<ResCUS0007Result> {
        console.log('call cus0007 result');
        const path = `${this._basePath}/CUS0007?result&apptJobId=${reqbodyResult.apptJobId}`;
        return this.api.excuteDpGet<ResCUS0007Result>(path,TxID.queryQmResultCUS0007);
    }
    deleteClientCert(ReqBody: CUS0008Req): Observable<ResCUS0008> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteClientCert),
            ReqBody: ReqBody
        } as ReqCUS0008;
        const path = `${this._basePath}/CUS0008`;
        return this.api.excuteNpPost<ResCUS0008>(path, body);
    }


    excuteCusJob<T,K,Z>(invokeFn:Function,loopstatusFn:Observable<K>,resultFn:Observable<Z>){
        invokeFn()
    }
}
