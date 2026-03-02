import { ToolService } from 'src/app/shared/services/tool.service';
import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { ApiBaseService } from './api-base.service';
import { TxID } from 'src/app/models/common.enum';
import {
  AA0303Req,
  ReqAA0303,
  ResAA0303,
} from 'src/app/models/api/ApiService/aa0303.interface';
import { environment } from 'src/environments/environment';
import {
  AA0317Req,
  ReqAA0317,
  ResAA0317,
} from 'src/app/models/api/ApiService/aa0317.interface';
import {
  AA0318Req,
  ReqAA0318,
  ResAA0318,
} from 'src/app/models/api/ApiService/aa0318.interface';
import {
  AA0319Req,
  ReqAA0319,
  ResAA0319,
} from 'src/app/models/api/ApiService/aa0319.interface';
import {
  AA0321Req,
  ReqAA0321,
  ResAA0321,
} from 'src/app/models/api/ApiService/aa0321.interface';
import {
  AA0301Req,
  ReqAA0301_v3,
  ResAA0301_v3,
} from 'src/app/models/api/ApiService/aa0301_v3.interfcae';
import {
  AA0302Req,
  ReqAA0302_v3,
  ResAA0302_v3,
} from 'src/app/models/api/ApiService/aa0302_v3.interface';
import {
  AA0311Req,
  ReqAA0311_v3,
  ResAA0311_v3,
  ResAA0311_v3Before,
} from 'src/app/models/api/ApiService/aa0311_v3.interface';
import {
  AA0320Req,
  ResAA0320,
} from 'src/app/models/api/ApiService/aa0320.interface';
import * as shajs from 'sha.js';
import {
  AA0304Req,
  ReqAA0304,
  ResAA0304,
  ResAA0304Before,
} from 'src/app/models/api/ApiService/aa0304.interface';
import {
  AA0313Req,
  ReqAA0313,
  ResAA0313,
  ResAA0313Before,
} from 'src/app/models/api/ApiService/aa0313.interface';
import {
  AA0315Req,
  ResAA0315_v3,
  ReqAA0315_v3,
} from 'src/app/models/api/ApiService/aa0315_v3.interface';
import {
  AA0316Req,
  ReqAA0316_v3,
  ResAA0316_v3,
  ResAA0316_v3Before,
} from 'src/app/models/api/ApiService/aa0316_v3.interface';
import {
  AA0312Req,
  ReqAA0312,
  ResAA0312,
  ResAA0312Before,
} from 'src/app/models/api/ApiService/aa0312_v3.interface';
import { ReqAA0427, RespAA0427 } from 'src/app/models/api/ApiService/aa0427.interface';
import { AA0428Req, ReqAA0428, ResAA0428 } from 'src/app/models/api/ApiService/aa0428.interfcae';
import { AA0423Req, ReqAA0423, RespAA0423 } from 'src/app/models/api/ApiService/aa0423.interface';
import { ReqAA0429, RespAA0429 } from 'src/app/models/api/ApiService/aa0429.interfcae';
import { AA0430Req, ReqAA0430, RespAA0430 } from 'src/app/models/api/ApiService/aa0430.interfcae';
import { AA0431Req, ReqAA0431, RespAA0431 } from 'src/app/models/api/ApiService/aa0431.interfcae';
import { AA0424Req, ReqAA0424, RespAA0424 } from 'src/app/models/api/ApiService/aa0424.interface';
import { AA0425Req, ReqAA0425, RespAA0425 } from 'src/app/models/api/ApiService/aa0425.interface';
import { AA0426Req, ReqAA0426, RespAA0426 } from 'src/app/models/api/ApiService/aa0426.interface';
import { AA0432Req, ReqAA0432, RespAA0432 } from 'src/app/models/api/ApiService/aa0432.interfcae';
import { AA0306Req, ReqAA0306, ResAA0306 } from 'src/app/models/api/ApiService/aa0306.interface';

@Injectable()
export class ApiService {
  public get npBasePath(): string {
    return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
  }

  constructor(private api: ApiBaseService, private tool: ToolService) {}

  /**
   * 忽略查無資料
   * v3版，因為多個畫面使用AA0301，所以先分開避免影響
   * AA0301: 查詢APIList
   * 查詢所有部署在TSMP中的API。預設查詢TSMP_API_LIST Table中所有API。
   * @param body ReqAA0301
   */
  queryAPIList_v3_ignore1298(ReqBody: AA0301Req) {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIList),
      ReqBody: ReqBody,
    } as ReqAA0301_v3;
    const path = `${this.npBasePath}/AA0301`;
    return this.api.excuteNpPost_ignore1298<ResAA0301_v3>(path, body);
  }

  /**
   * v3版，因為多個畫面使用AA0301，所以先分開避免影響
   * AA0301: 查詢APIList
   * 查詢所有部署在TSMP中的API。預設查詢TSMP_API_LIST Table中所有API。
   * @param body ReqAA0301
   */
  queryAPIList_v3(ReqBody: AA0301Req): Observable<ResAA0301_v3> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIList),
      ReqBody: ReqBody,
    } as ReqAA0301_v3;
    const path = `${this.npBasePath}/AA0301`;
    return this.api.npPost<ResAA0301_v3>(path, body);
  }

  /**
   * AA0302: 查詢API明細
   */
  queryAPIDetail_v3(ReqBody: AA0302Req): Observable<ResAA0302_v3> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIDetail),
      ReqBody: ReqBody,
    } as ReqAA0302_v3;
    const path = `${this.npBasePath}/AA0302`;
    return this.api.npPost<ResAA0302_v3>(path, body);
  }

  /**
   * AA0303: 更新APIStatusList
   * 更新API的狀態。
   * @param body ReqAA0303
   */
  updateAPIStatus_1(ReqBody: AA0303Req): Observable<ResAA0303> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateAPIStatus_1),
      ReqBody: ReqBody,
    } as ReqAA0303;
    const path = `${this.npBasePath}/AA0303`;
    return this.api.npPost<ResAA0303>(path, body);
  }

  cancelScheduledDate(ReqBody:AA0306Req): Observable<ResAA0306> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.cancelScheduledDate),
      ReqBody: ReqBody,
    } as ReqAA0306;
    const path = `${this.npBasePath}/AA0306`;
    return this.api.npPost<ResAA0306>(path, body);
  }

  /**
   * before
   * AA0304: 更新API資訊
   * @param ReqBody
   */
  updateAPIInfo_before(): Observable<ResAA0304Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateAPIInfo),
      ReqBody: {},
    } as ReqAA0304;
    const path = `${this.npBasePath}/AA0304?before`;
    return this.api.npPost<ResAA0304Before>(path, body);
  }

  /**
   * v3版
   * AA0304: 更新API資訊
   * @param ReqBody
   */
  updateAPIInfo(ReqBody: AA0304Req): Observable<ResAA0304> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateAPIInfo),
      ReqBody: ReqBody,
    } as ReqAA0304;
    const path = `${this.npBasePath}/AA0304`;
    return this.api.npPost<ResAA0304>(path, body);
  }

  /**
   * AA0311: 註冊外部API
   * 在TSMP中註冊外部既有的Http API。
   * @param req_0311
   */
  registerAPI_v3(req_0311: AA0311Req): Observable<ResAA0311_v3> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.registerAPI),
      // ReqBody: this.tool.deleteProperties(req_0311)
      ReqBody: this.formateReq0311(req_0311),
    };
    const path = `${this.npBasePath}/AA0311`;
    return this.api.npPost<ResAA0311_v3>(path, body);
    // return this.api.npPost<ResAA0311_v3>(path, body, ['0158']);
  }

  formateReq0311(req_0311: AA0311Req) {
    for (var key in req_0311) {
      if (req_0311.hasOwnProperty(key)) {
        if (
          req_0311[key] === null ||
          req_0311[key] == undefined ||
          req_0311[key] === ''
        ) {
          delete req_0311[key];
        }
      }
    }
    return req_0311;
  }

  /**
   * AA0311: 註冊外部API_before
   * 在TSMP中註冊外部既有的Http API。
   * @param req_0311
   */
  registerAPI_v3_before(): Observable<ResAA0311_v3Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.registerAPI),
      ReqBody: {},
    } as ReqAA0311_v3;
    const path = `${this.npBasePath}/AA0311?before`;
    return this.api.npPost<ResAA0311_v3Before>(path, body, ['0158']);
  }

  /**
   * v3
   * before
   * AA0312: 測試外部API
   * 在TSMP中測試外部既有的Http API。
   */
  testAPI_v3_before(): Observable<ResAA0312Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.testAPI),
      ReqBody: {},
    } as ReqAA0312;
    const path = `${this.npBasePath}/AA0312?before`;
    return this.api.npPost<ResAA0312Before>(path, body);
  }

  /**
   * v3
   * AA0312: 測試外部API
   * 在TSMP中測試外部既有的Http API。
   */
  testAPI_v3(ReqBody: AA0312Req): Observable<ResAA0312> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.testAPI),
      ReqBody: ReqBody,
    } as ReqAA0312;
    const path = `${this.npBasePath}/AA0312`;
    return this.api.npPost<ResAA0312>(path, body);
  }

  /**
   * before
   * AA0313: 更新註冊/組合API
   * @param ReqBody
   */
  updateRegCompAPI_before(): Observable<ResAA0313Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateRegCompAPI),
      ReqBody: {},
    } as ReqAA0313;
    const path = `${this.npBasePath}/AA0313?before`;
    return this.api.npPost<ResAA0313Before>(path, body);
  }

  /**
   * AA0313: 更新註冊/組合API
   * @param ReqBody
   */
  updateRegCompAPI(ReqBody: AA0313Req): Observable<ResAA0313> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateRegCompAPI),
      ReqBody: ReqBody,
    } as ReqAA0313;
    const path = `${this.npBasePath}/AA0313`;
    return this.api.npPost<ResAA0313>(path, body);
  }

  /**
   * v3版
   * AA0315: 上傳外部系統介接規格
   */
  uploadOpenApiDoc_v3(ReqBody: AA0315Req): Observable<ResAA0315_v3> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.uploadOpenApiDoc),
      ReqBody: ReqBody,
    } as ReqAA0315_v3;
    const path = `${this.npBasePath}/AA0315`;
    return this.api.npPost<ResAA0315_v3>(path, body);
  }

  /**
   * before
   * AA0316
   * @param ReqBody
   */
  registerAPIList_v3_before(): Observable<ResAA0316_v3Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.uploadOpenApiDoc),
      ReqBody: {},
    } as ReqAA0316_v3;
    const path = `${this.npBasePath}/AA0316?before`;
    return this.api.npPost<ResAA0316_v3Before>(path, body);
  }

  /**
   * v3版
   * AA0316: 匯入方式註冊外部API
   * 查詢所有部署在TSMP中的API。預設查詢TSMP_API_LIST Table中所有API。
   * @param body ReqAA0316
   */
  registerAPIList_v3(ReqBody: AA0316Req): Observable<ResAA0316_v3> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.uploadOpenApiDoc),
      ReqBody: ReqBody,
    } as ReqAA0316_v3;
    const path = `${this.npBasePath}/AA0316`;
    return this.api.npPost<ResAA0316_v3>(path, body);
  }

  /**
   * v3
   * AA0317: 匯出RegCompAPIs
   * 在TSMP Admin中匯出Registered & Composed APIs。
   * 此API匯出時自動乎略Registered & Composed以外的APIs。
   * 匯出時，Res_0317可另存新檔。
   * @param ReqBody
   */
  exportRegCompAPIs(ReqBody: AA0317Req): Observable<ResAA0317> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportRegCompAPIs),
      ReqBody: ReqBody,
    } as ReqAA0317;
    const path = `${this.npBasePath}/AA0317`;
    return this.api.npPost<ResAA0317>(path, body);
  }

  /**
   * v3
   * AA0318: 上傳註冊/組合API
   * 上傳註冊/組合API的檔案，並預覽內容及檢查結果。
   * @param ReqBody
   */
  uploadRegCompAPIs(ReqBody: AA0318Req): Observable<ResAA0318> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.uploadRegCompAPIs),
      ReqBody: ReqBody,
    } as ReqAA0318;
    const path = `${this.npBasePath}/AA0318`;
    return this.api.npPost<ResAA0318>(path, body);
  }

  /**
   * v3
   * AA0319: 匯入RegCompAPIs
   * 在TSMP Admin中指定上傳檔案中要匯入的Registered & Composed APIs。
   * 儲存時:
   * API_STATUS預設為"2" (Disabled)
   * REG_STATUS預設為"1"(確認)
   * @param req_0319
   */
  importRegCompAPIs(ReqBody: AA0319Req): Observable<ResAA0319> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.importRegCompAPIs),
      ReqBody: ReqBody,
    } as ReqAA0319;
    const path = `${this.npBasePath}/AA0319`;
    return this.api.npPost<ResAA0319>(path, body);
  }

  /**
   * AA0320: 查詢API群組清單
   * @param ReqBody
   */
  queryGroupApiList(ReqBody: AA0320Req): Observable<ResAA0320> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGroupApiList),
      ReqBody: ReqBody,
    } as ReqAA0321;
    const path = `${this.npBasePath}/AA0320`;
    return this.api.npPost<ResAA0320>(path, body);
  }

  /**
   * AA0320: 查詢API群組清單
   * @param ReqBody
   */
  queryGroupApiList_ajax(ReqBody: AA0320Req) {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGroupApiList),
      ReqBody: ReqBody,
    } as ReqAA0321;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0320`;
    let token = this.tool.getToken();
    const prefix = this.tool.getSignBlock();
    const bodyJosn = JSON.stringify(body);
    let signCode = shajs('sha256')
      .update(prefix + bodyJosn)
      .digest('hex');
    return new Promise<ResAA0320>((resolve, reject) => {
      $.ajax({
        url: path,
        type: 'POST',
        dataType: 'json', // response的資料格式
        cache: false,
        data: JSON.stringify(body),
        processData: false,
        headers: {
          'Content-Type': 'application/json',
          SignCode: signCode,
          Authorization: `Bearer ${token}`,
        },
      })
        .done(function (res) {
          resolve(res);
        })
        .fail(function (res) {
          reject(res);
        });
    });
  }

  /**
   * AA0321: 以組織查詢API清單
   */
  queryAPIListByOrg(ReqBody: AA0321Req): Observable<ResAA0321> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIListByOrg),
      ReqBody: ReqBody,
    } as ReqAA0321;
    const path = `${this.npBasePath}/AA0321`;
    return this.api.npPost<ResAA0321>(path, body);
  }

  queryAllLabel_ignore1298(): Observable<RespAA0427> {
    let body = {
        ReqHeader: this.api.getReqHeader(TxID.queryAllLabel),
        ReqBody: {}
    } as ReqAA0427
    const path = `${this.npBasePath}/AA0427`;
    return this.api.excuteNpPost_ignore1298<RespAA0427>(path, body);
  }

  queryAPIListByLabel(ReqBody: AA0428Req): Observable<ResAA0428> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIListByLabel),
      ReqBody: ReqBody,
    } as ReqAA0428;
    const path = `${this.npBasePath}/AA0428`;
    return this.api.npPost<ResAA0428>(path, body);
  }

  /**依目標URL或標籤搜尋API清單 */
  queryAPIListBySrcUrlOrLabel(ReqBody: AA0423Req): Observable<RespAA0423> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIListBySrcUrlOrLabel),
      ReqBody: ReqBody,
    } as ReqAA0423;
    const path = `${this.npBasePath}/AA0423`;
    return this.api.npPost<RespAA0423>(path, body);
  }

  queryAllTargetSitList(): Observable<RespAA0429> {
    let body = {
        ReqHeader: this.api.getReqHeader(TxID.queryAllTargetSitList),
        ReqBody: {}
    } as ReqAA0429
    const path = `${this.npBasePath}/AA0429`;
    return this.api.npPost<RespAA0429>(path, body);
  }

  batchNoOauthModify(ReqBody: AA0430Req): Observable<RespAA0430> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.batchNoOauthModify),
      ReqBody: ReqBody,
    } as ReqAA0430;
    const path = `${this.npBasePath}/AA0430`;
    return this.api.npPost<RespAA0430>(path, body);
  }

  batchLabelReset(ReqBody: AA0431Req): Observable<RespAA0431> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.batchLabelReset),
      ReqBody: ReqBody,
    } as ReqAA0431;
    const path = `${this.npBasePath}/AA0431`;
    return this.api.npPost<RespAA0431>(path, body);
  }

  temporaryByModifyBatch(ReqBody: AA0424Req): Observable<RespAA0424> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.temporaryByModifyBatch),
      ReqBody: ReqBody,
    } as ReqAA0424;
    const path = `${this.npBasePath}/AA0424`;
    return this.api.npPost<RespAA0424>(path, body);
  }

  previewByModifyBatch(ReqBody: AA0425Req): Observable<RespAA0425> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.previewByModifyBatch),
      ReqBody: ReqBody,
    } as ReqAA0425;
    const path = `${this.npBasePath}/AA0425`;
    return this.api.npPost<RespAA0425>(path, body);
  }

  batchModify(ReqBody: AA0426Req): Observable<RespAA0426> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.batchModify),
      ReqBody: ReqBody,
    } as ReqAA0426;
    const path = `${this.npBasePath}/AA0426`;
    return this.api.npPost<RespAA0426>(path, body);
  }

  batchFailHandlePolicy(ReqBody: AA0432Req): Observable<RespAA0432> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.batchFailHandlePolicy),
      ReqBody: ReqBody,
    } as ReqAA0432;
    const path = `${this.npBasePath}/AA0432`;
    return this.api.npPost<RespAA0432>(path, body);
  }


}
