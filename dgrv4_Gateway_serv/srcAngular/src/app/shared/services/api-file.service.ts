import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { DPB0078Req, ReqDPB0078 } from 'src/app/models/api/FileService/dpb0078.interface';
import { ResDPB0082 } from 'src/app/models/api/FileService/dpb0082.interface';
import { TxID } from 'src/app/models/common.enum';
import { ReqAA1120, RespAA1120 } from 'src/app/models/api/FileService/aa1120.interface';
import { ReqAA1121, RequestParam, RespAA1121 } from 'src/app/models/api/FileService/aa1121.interface';
import { AA1128Req, ReqAA1128, RespAA1128 } from 'src/app/models/api/FileService/aa1128.interface';
import { AA1129Req, ReqAA1129, RespAA1129 } from 'src/app/models/api/FileService/aa1129.interface';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  public get basePath(): string {
    return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
  }

  constructor(
    private api: ApiBaseService
  ) {
    this.api.baseUrl = environment.dpPath;
  }

  /**
   * DP0078: 下載檔案
   * 依據指定的路徑下載檔案
   */
  downloadFile(ReqBody: DPB0078Req): Observable<Blob> {
    const path = `${this.basePath}/DPB0078?filePath=${ReqBody.filePath}`;
    return this.api.excuteDpGetFile(path);
  }

  /**
   * DPB0082: For MultiPart-上傳檔案
   */
  uploadFile2(file: File): Observable<ResDPB0082> {
    const path = `${this.basePath}/DPB0082`;
    return this.api.excuteFileUpload(path, file);
    // return Observable.create(obser => {
    //     let self = this;
    //     let formData = new FormData();
    //     let serverno = '1';
    //     let d = new Date();
    //     let date = dayjs(d).format('YYMMDDHHmmss');
    //     let alphaNumber = generate({ length: 6, numbers: true });
    //     let txDate = this.tool.formateDate(d);
    //     formData.append('txSN', `${serverno}${date}${alphaNumber}`);
    //     formData.append('txDate', txDate);
    //     formData.append('txID', 'DPB0082');
    //     formData.append('cID', sessionStorage.getItem('decode_token') ? JSON.parse(sessionStorage.getItem('decode_token'))['client_id'] : '');
    //     formData.append('file', file);
    //     let xhr = new XMLHttpRequest();
    //     let token = this.tool.getToken();
    //     let url = `${this.api.baseUrl}/${this.basePath}/DPB0082`;
    //     xhr.open('POST', url, true);
    //     xhr.setRequestHeader("Authorization", `Bearer ${token}`);
    //     xhr.onreadystatechange = () => {
    //         if (xhr.readyState == XMLHttpRequest.DONE) {
    //             // console.log(xhr.responseText);
    //         }
    //     };
    //     xhr.onload = function (e) {
    //         let res = JSON.parse(xhr.responseText);
    //         if (this.status == 200) {
    //             if (self.tool.checkDpSuccess(res.ResHeader)) {
    //                 obser.next(JSON.parse(xhr.responseText));
    //             }
    //             else {
    //                 self.alert.ok(`Return code : ${res.ResHeader.rtnCode}`, res.ResHeader.rtnMsg, null); // 判斷後兩碼如不為00 , show rtnMsg
    //                 self.ngxService.stopAll();
    //             }
    //         }
    //         else {
    //             self.alert.ok(`HTTP Status Code：${res.status}`, '', AlertType.error, `Error：${res.error}<br>Message：${res.message}`);
    //         }
    //     };
    //     xhr.send(formData);  // multipart/form-data
    // });
  }

  exportClientRelated(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportClientRelated),
      ReqBody: {}
    } as ReqAA1120;
    const path = `${this.basePath}/AA1120`;
    return this.api.excutePostGetFile(path, body);
  }

  // importClientRelated(ReqBody:RequestParam): Observable<RespAA1121> {
  //   let body = {
  //     ReqHeader: this.api.getReqHeader(TxID.importClientRelated),
  //     ReqBody: ReqBody
  //   } as ReqAA1121;
  //   const path = `${this.basePath}/AA1121`;
  //   return this.api.npPost<RespAA1121>(path, body);
  // }

  importClientRelated(req: any, file: File): Observable<RespAA1121> {
    const path = `${this.basePath}/AA1121`;
    return this.api.excuteTsmpSetting<RespAA1121>(path, file, req);
  }

  importClientRelatedAllCover(ReqBody:AA1128Req): Observable<RespAA1128> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.importClientRelatedAllCover),
      ReqBody: ReqBody
    } as ReqAA1128;
    const path = `${this.basePath}/AA1128`;
    return this.api.npPost<RespAA1128>(path, body);
  }

  importClientRelatedConfirm(ReqBody:AA1129Req): Observable<RespAA1129> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.importClientRelatedConfirm),
      ReqBody: ReqBody
    } as ReqAA1129;
    const path = `${this.basePath}/AA1129`;
    return this.api.npPost<RespAA1129>(path, body);
  }

}
