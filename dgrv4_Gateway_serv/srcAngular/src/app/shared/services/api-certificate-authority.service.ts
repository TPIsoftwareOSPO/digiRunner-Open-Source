
import { Observable, of } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0083Req, ResDPB0083, ReqDPB0083 } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { DPB0084Req, ResDPB0084, ReqDPB0084 } from 'src/app/models/api/CertificateAuthorityService/dpb0084.interface';
import { DPB0086Req, ResDPB0086, ReqDPB0086 } from 'src/app/models/api/CertificateAuthorityService/dpb0086.interface';
import { DPB0085Req, ResDPB0085, ReqDPB0085 } from 'src/app/models/api/CertificateAuthorityService/dpb0085.interface';
import { DPB0087Req, ReqDPB0087 } from 'src/app/models/api/CertificateAuthorityService/dpb0087.interface';
import { ResDPB0088, DPB0088Req, ReqDPB0088 } from 'src/app/models/api/CertificateAuthorityService/dpb0088.interface';
import { DPB0089Req, ResDPB0089, ReqDPB0089 } from 'src/app/models/api/CertificateAuthorityService/dpb0089.interface';
import { ResDPB0119 } from 'src/app/models/api/CertificateAuthorityService/dpb0119.interface';
import { ResDPB0120 } from 'src/app/models/api/CertificateAuthorityService/dpb0120.interface';
import { RespDPB0229 } from 'src/app/models/api/ServerService/dpb0229.interface';
import { DPB0225Req, DPB0225RespBefore, RespDPB0225, RespDPB0225RespBefore } from 'src/app/models/api/ServerService/dpb0225.interface';
import { DPB0226Req, DPB0226RespBefore, RespDPB0226, RespDPB0226RespBefore } from 'src/app/models/api/ServerService/dpb0226.interface';
import { DPB0228Req, RespDPB0228 } from 'src/app/models/api/ServerService/dpb0228.interface';
import { DPB0227Req, RespDPB0227 } from 'src/app/models/api/ServerService/dpb0227.interface';
import { DPB0230Req, RespDPB0230 } from 'src/app/models/api/ServerService/dpb0230.interface';
import { DPB0231Req, RespDPB0231 } from 'src/app/models/api/ServerService/dpb0231.interface';
import { RespSSLDecoder, SSLDecoderReq } from 'src/app/models/api/ServerService/ssl-decoder.interface';

@Injectable({
    providedIn: 'root'
})
export class ClientCAService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0084: 查找Client憑證
     * 1. 以CLIENT _ID 作為搜尋條件查找 TSMP_CLIENT_CERT, TSMP_DP_FILE, TSMP_DP_FILE_CATEGORY
     * 2. 列出CLIENT持有憑證
     */
    queryClientByCid(ReqBody: DPB0084Req): Observable<ResDPB0084> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientByCid),
            ReqBody: ReqBody
        } as ReqDPB0084;
        const path = `${this.basePath}/DPB0084`;
        return this.api.npPost<ResDPB0084>(path, body);
    }
    queryClientByCid_ignore1298(ReqBody: DPB0084Req): Observable<ResDPB0084> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientByCid),
            ReqBody: ReqBody
        } as ReqDPB0084;
        const path = `${this.basePath}/DPB0084`;
        return this.api.excuteNpPost_ignore1298<ResDPB0084>(path, body);
    }


    /**
     * DPB0085: 上傳Client憑證
     * 1. 上傳憑證PEM檔
     * 2. 解析PEM檔內容
     * 3. 比對該憑證效期 (憑證最多保留2張創建日期最新的) , 同一個 Client Id 最多只有2張憑證, 若已有2張則把最舊的刪除, 若上傳相同的憑證則不處理
     * 4. 將對應之內容與PEM檔原始資料Insert至TSMP_CLIENT_CERT
     * 5. 若是異動2筆資料, 則需要依 "規範" 加註 @Transcational
     */
    uploadClientCA(ReqBody: DPB0085Req): Observable<ResDPB0085> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.uploadClientCA),
            ReqBody: ReqBody
        } as ReqDPB0085;
        const path = `${this.basePath}/DPB0085`;
        return this.api.npPost<ResDPB0085>(path, body);
    }

    /**
     * DPB0086: 刪除Client憑證
     * 1. 依指定的 clientId 移除檔案
     */
    deleteClientCA(ReqBody: DPB0086Req): Observable<ResDPB0086> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteClientCA),
            ReqBody: ReqBody
        } as ReqDPB0086;
        const path = `${this.basePath}/DPB0086`;
        return this.api.npPost<ResDPB0086>(path, body);
    }

    /**
     * DPB0087: 下載Client憑證
     * 1. 指定 id 下載檔案
     * 2. 可傳入多筆 id, 程式會壓為 zip 下載
     */
    downLoadPEMFile(ReqBody: DPB0087Req): Observable<Blob> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.downLoadPEMFile),
            ReqBody: ReqBody
        } as ReqDPB0087;
        const path = `${this.basePath}/DPB0087`;
        return this.api.excuteDpGetPEMFile(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0088: 依據日期範圍查找憑證列表
     */
    queryCaListByDate_ignore1298(ReqBody: DPB0088Req): Observable<ResDPB0088> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryCaListByDate),
            ReqBody: ReqBody
        } as ReqDPB0088;
        const path = `${this.basePath}/DPB0088`;
        return this.api.excuteNpPost_ignore1298<ResDPB0088>(path, body);
    }

    /**
     * DPB0088: 依據日期範圍查找憑證列表
     */
    queryCaListByDate(ReqBody: DPB0088Req): Observable<ResDPB0088> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryCaListByDate),
            ReqBody: ReqBody
        } as ReqDPB0088;
        const path = `${this.basePath}/DPB0088`;
        return this.api.npPost<ResDPB0088>(path, body);
    }

    /**
     * DPB0089: 將多個PEM檔合併成txt文字檔
     * 1. 指定 id 下載 PEM.txt 檔案, 內容為 PEM 的文字內容合併
     * 2. 可傳入多筆 id, 每一筆文字內容以 \n 取隔
     * 3. UI 直接把String 做成檔案(PEM.txt)下載
     */
    returnTextFIle(ReqBody: DPB0089Req): Observable<ResDPB0089> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.returnTextFIle),
            ReqBody: ReqBody
        } as ReqDPB0089;
        const path = `${this.basePath}/DPB0089`;
        return this.api.npPost<ResDPB0089>(path, body);
    }
    /**
     * 取得客製包URL
     */
    queryCusUrl(): Observable<ResDPB0119> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.returnTextFIle)
        };
        const path = `${this.basePath}/DPB0119?1`;
        return this.api.excuteDpGet<ResDPB0119>(path,TxID.queryCusEnabl);
        // return of({
        //     ResHeader : {},
        //     RespBody:{cusUrl : 'dgr-cus-etb_cg/11'}
        // } as ResDPB0119)
    }
    /**是否啟用客製功能 */
    queryCusEnable(): Observable<ResDPB0120> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.returnTextFIle)
        };
        const path = `${this.basePath}/DPB0120?1`;
        return this.api.excuteDpGet<ResDPB0120>(path,TxID.queryCusEnable);
        // return of({
        //     ResHeader:{"txSN":"1210317141343ZiMvGr","txDate":"20210317T141343+0800","txID":"DPB0083","rtnCode":"1100","rtnMsg":null},
        //     RespBody:{isCusEnable : 'Y'}
        // } as ResDPB0120)
    }

    //查詢站台
    querySiteList(): Observable<RespDPB0229> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.querySiteList),
          ReqBody: {}
      };
      const path = `${this.basePath}/DPB0229`;
      return this.api.excuteNpPost<RespDPB0229>(path, body);
    }
    querySiteList_ignore1298(): Observable<RespDPB0229> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.querySiteList),
          ReqBody: {}
      };
      const path = `${this.basePath}/DPB0229`;
      return this.api.excuteNpPost_ignore1298<RespDPB0229>(path, body);
    }

    // 新增站台
    createClientCert(ReqBody:DPB0225Req): Observable<RespDPB0225> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.createClientCert),
          ReqBody: ReqBody,
      };
      const path = `${this.basePath}/DPB0225`;
      return this.api.excuteNpPost<RespDPB0225>(path, body);
    }

    createClientCert_before(): Observable<DPB0225RespBefore> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.createClientCert),
          ReqBody: {},
      };
      const path = `${this.basePath}/DPB0225?before`;
      return this.api.excuteNpPost<DPB0225RespBefore>(path, body);
    }

    // 修改站台
    updateClientCert(ReqBody:DPB0226Req): Observable<RespDPB0226> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.updateClientCert),
          ReqBody: ReqBody,
      };
      const path = `${this.basePath}/DPB0226`;
      return this.api.excuteNpPost<RespDPB0226>(path, body);
    }

    updateClientCert_before(): Observable<DPB0226RespBefore> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.updateClientCert),
          ReqBody: {},
      };
      const path = `${this.basePath}/DPB0226?before`;
      return this.api.excuteNpPost<DPB0226RespBefore>(path, body);
    }

    // 刪除站台
    deleteClientCert(ReqBody:DPB0227Req): Observable<RespDPB0227> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.deleteClientCert),
          ReqBody: ReqBody,
      };
      const path = `${this.basePath}/DPB0227`;
      return this.api.excuteNpPost<RespDPB0227>(path, body);
    }

    // 查詢站台明細
    queryClientCertDetail(ReqBody:DPB0228Req): Observable<RespDPB0228> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.queryClientCertDetail),
          ReqBody: ReqBody,
      };
      const path = `${this.basePath}/DPB0228`;
      return this.api.excuteNpPost<RespDPB0228>(path, body);
    }

    // 啟動 / 停用站台
    enableClientCert(ReqBody:DPB0230Req): Observable<RespDPB0230> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.enableSite),
          ReqBody: ReqBody,
      };
      const path = `${this.basePath}/DPB0230`;
      return this.api.excuteNpPost<RespDPB0230>(path, body);
    }

    // mtls測試連線
    checkMtlsConnection(ReqBody:DPB0231Req): Observable<RespDPB0231> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.checkMtlsConnection),
          ReqBody: ReqBody,
      };
      const path = `${this.basePath}/DPB0231`;
      return this.api.excuteNpPost<RespDPB0231>(path, body);
    }

    // SSLDecoder
    SSLDecoder(ReqBody:SSLDecoderReq): Observable<RespSSLDecoder> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.SSLDecoder),
          ReqBody: ReqBody,
      };
      const path = `dgrv4/SSLDecoder`;
      return this.api.excuteNpPost<RespSSLDecoder>(path, body);
    }
}
