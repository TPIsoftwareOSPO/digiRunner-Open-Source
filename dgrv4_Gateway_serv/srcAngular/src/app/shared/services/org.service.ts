import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { ApiBaseService } from './api-base.service';
import {
  ResAA1001,
  ReqAA1001,
  AA1001Req,
} from 'src/app/models/api/OrgService/aa1001.interface';
import { Observable } from 'rxjs';
import { TxID } from 'src/app/models/common.enum';
import {
  ResAA1002,
  ReqAA1002,
  AA1002Req,
} from 'src/app/models/api/OrgService/aa1002.interface';
import {
  ResAA1003,
  ReqAA1003,
  AA1003Req,
} from 'src/app/models/api/OrgService/aa1003.interface';
import {
  ResAA1004,
  ReqAA1004,
  AA1004Req,
} from 'src/app/models/api/OrgService/aa1004.interface';
import {
  ResAA1005,
  ReqAA1005,
  AA1005Req,
} from 'src/app/models/api/OrgService/aa1005.interface';
import {
  AA0433Req,
  ReqAA0433,
  RespAA0433,
} from 'src/app/models/api/ApiService/aa0433.interfcae';
import {
  AA0434DTO,
  ReqAA0434,
  RespAA0434,
} from 'src/app/models/api/ApiService/aa0434.interfcae';

@Injectable()
export class TOrgService {
  public get npBasePath(): string {
    return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
  }

  constructor(private api: ApiBaseService) {}

  /**
   * AA1001: 新增TOrg
   * 在TSMP Admin中新增TSMP組織單位，簡稱addTOrg。新增成功時返回ORG_ID。
   */
  addTOrg(ReqBody: AA1001Req): Observable<ResAA1001> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTOrg),
      ReqBody: ReqBody,
    } as ReqAA1001;
    const path = `${this.npBasePath}/AA1001`;
    return this.api.npPost<ResAA1001>(path, body);
  }

  /**
   * AA1002: 查詢TOrgList
   * 在TSMP中查詢TSMP裡的組織單位清單。預設查詢所有TOrgList。
   */
  queryTOrgList(ReqBody: AA1002Req): Observable<ResAA1002> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTOrgList),
      ReqBody: ReqBody,
    } as ReqAA1002;
    const path = `${this.npBasePath}/AA1002`;
    return this.api.npPost<ResAA1002>(path, body);
  }

  /**
   * AA1003: 更新TOrgByOrgID
   * 在TSMP Admin中根據ORG_ID更新該組織單位，簡稱updateTOrgByOrgId。更新成功時返回ORG_ID＆UPDATE_TIME。
   */
  updateTOrgByOrgId(ReqBody: AA1003Req): Observable<ResAA1003> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTOrgByOrgId),
      ReqBody: ReqBody,
    } as ReqAA1003;
    const path = `${this.npBasePath}/AA1003`;
    return this.api.npPost<ResAA1003>(path, body);
  }

  /**
   * AA1004: 刪除TOrg
   * 1. 在TSMP Admin中刪除TSMP組織單位by ORG_ID。需同時有ORG_ID與ORG_Name才能刪除TOrg。此API只有Administrator(具有Admin Role)可以使用。
   * 2. 刪除組織單位by ORG_ID前查詢TSMP_API_MODULE, TSMPN_API_MODULE, TSMP_API任一TABLE如有符合欲刪除之ORG_ID,則該筆ORG_ID不可刪除
   */
  deleteTOrgByOrgId(ReqBody: AA1004Req): Observable<ResAA1004> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteTOrgByOrgId),
      ReqBody: ReqBody,
    } as ReqAA1004;
    const path = `${this.npBasePath}/AA1004`;
    return this.api.npPost<ResAA1004>(path, body);
  }

  /**
   * AA1005: 查詢TOrgDetail
   * 在TSMP Admin中查詢TSMP後台組織資料。需同時需要ORG_ID與ORG_Name才能查詢TOrg Detail。
   */
  queryTOrgDetail(ReqBody: AA1005Req): Observable<ResAA1005> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTOrgDetail),
      ReqBody: ReqBody,
    } as ReqAA1005;
    const path = `${this.npBasePath}/AA1005`;
    return this.api.npPost<ResAA1005>(path, body);
  }

  queryOrgList(ReqBody: AA0433Req): Observable<RespAA0433> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTOrgList),
      ReqBody: ReqBody,
    } as ReqAA0433;
    const path = `${this.npBasePath}/AA0433`;
    return this.api.npPost<RespAA0433>(path, body);
  }

  batchApiOrg(ReqBody: Array<AA0434DTO>): Observable<RespAA0434> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.batchApiOrg),
      ReqBody: ReqBody,
    } as ReqAA0434;
    const path = `${this.npBasePath}/AA0434`;
    return this.api.npPost<RespAA0434>(path, body);
  }
}
