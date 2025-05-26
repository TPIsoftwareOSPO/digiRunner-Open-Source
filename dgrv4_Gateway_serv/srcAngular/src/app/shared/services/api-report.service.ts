import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import {
  AA1201Req,
  ReqAA1201,
  ResAA1201,
  ResAA1201Before,
} from 'src/app/models/api/ReportService/aa1201.interface';
import {
  AA1202Req,
  ReqAA1202,
  ResAA1202,
  ResAA1202Before,
} from 'src/app/models/api/ReportService/aa1202.interface';
import {
  AA1204Req,
  ReqAA1204,
  ResAA1204,
  ResAA1204Before,
} from 'src/app/models/api/ReportService/aa1204.interface';
import {
  AA1205Req,
  ReqAA1205,
  ResAA1205,
  ResAA1205Before,
} from 'src/app/models/api/ReportService/aa1205.interface';
import {
  AA1203Req,
  ReqAA1203,
  ResAA1203,
  ResAA1203Before,
} from 'src/app/models/api/ReportService/aa1203.interface';
import {
  AA1213Req,
  ReqAA1213,
  ResAA1213,
} from 'src/app/models/api/ReportService/aa1213.interface';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  public get basePath(): string {
    // return 'tsmpdpaa/11';
    return 'dgrv4/11';
  }

  constructor(private api: ApiBaseService) {
    this.api.baseUrl = environment.dpPath;
  }

  /**
   * before
   * AA1201: API使用次數統計
   */
  queryApiUsageStatistics_before(): Observable<ResAA1201Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryApiUsageStatistics),
      ReqBody: {},
    } as ReqAA1201;
    const path = `${this.basePath}/AA1201?before`;
    return this.api.npPost<ResAA1201Before>(path, body);
  }

  /**
   * AA1201: API使用次數統計
   */
  queryApiUsageStatistics(ReqBody: AA1201Req): Observable<ResAA1201> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryApiUsageStatistics),
      ReqBody: ReqBody,
    } as ReqAA1201;
    const path = `${this.basePath}/AA1201`;
    return this.api.npPost<ResAA1201>(path, body);
  }

  /**
   * before
   * AA1202: API次數-時間分析
   */
  queryAPITimesAndTime_before(): Observable<ResAA1202Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPITimesAndTime),
      ReqBody: {},
    } as ReqAA1202;
    const path = `${this.basePath}/AA1202?before`;
    return this.api.npPost<ResAA1202Before>(path, body);
  }

  /**
   * AA1202: API次數-時間分析
   */
  queryAPITimesAndTime(ReqBody: AA1202Req): Observable<ResAA1202> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPITimesAndTime),
      ReqBody: ReqBody,
    } as ReqAA1202;
    const path = `${this.basePath}/AA1202`;
    return this.api.npPost<ResAA1202>(path, body);
  }

  /**
   * before
   * AA1203: API平均時間計算分析
   */
  queryAPIAverageTime_before(): Observable<ResAA1203Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIAverageTime),
      ReqBody: {},
    } as ReqAA1203;
    const path = `${this.basePath}/AA1203?before`;
    return this.api.npPost<ResAA1203Before>(path, body);
  }

  /**
   * AA1203: API平均時間計算分析
   */
  queryAPIAverageTime(ReqBody: AA1203Req): Observable<ResAA1203> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAPIAverageTime),
      ReqBody: ReqBody,
    } as ReqAA1203;
    const path = `${this.basePath}/AA1203`;
    return this.api.npPost<ResAA1203>(path, body);
  }

  /**
   * before
   * AA1204: API流量分析
   */
  queryApiTraffic_before(): Observable<ResAA1204Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryApiTraffic),
      ReqBody: {},
    } as ReqAA1204;
    const path = `${this.basePath}/AA1204?before`;
    return this.api.npPost<ResAA1204Before>(path, body);
  }

  /**
   * AA1204: API流量分析
   */
  queryApiTraffic(ReqBody: AA1204Req): Observable<ResAA1204> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryApiTraffic),
      ReqBody: ReqBody,
    } as ReqAA1204;
    const path = `${this.basePath}/AA1204`;
    return this.api.npPost<ResAA1204>(path, body);
  }

  /**
   * before
   * AA1205: Bad Attempt連線報告
   */
  queryBadattemptConnection_before(): Observable<ResAA1205Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryBadattemptConnection),
      ReqBody: {},
    } as ReqAA1205;
    const path = `${this.basePath}/AA1205?before`;
    return this.api.npPost<ResAA1205Before>(path, body);
  }

  /**
   * AA1205: Bad Attempt連線報告
   */
  queryBadattemptConnection(ReqBody: AA1205Req): Observable<ResAA1205> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryBadattemptConnection),
      ReqBody: ReqBody,
    } as ReqAA1205;
    const path = `${this.basePath}/AA1205`;
    return this.api.npPost<ResAA1205>(path, body);
  }

  queryApiAbnormal(ReqBody: AA1213Req): Observable<ResAA1213> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryApiAbnormal),
      ReqBody: ReqBody,
    } as ReqAA1213;
    const path = `${this.basePath}/AA1213`;
    return this.api.npPost<ResAA1213>(path, body);
  }
}
