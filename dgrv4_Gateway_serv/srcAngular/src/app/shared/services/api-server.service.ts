import {
  DPB0208Req,
  ReqDPB0208,
  RespDPB0208,
  RespDPB0208Before,
} from './../../models/api/ServerService/dpb0208.interface';
import {
  ReqDPB0184,
  RespDPB0184,
} from './../../models/api/ServerService/dpb0184.interface';
import { RespDPB0167Before } from './../../models/api/ServerService/dpb0167.interface';
import {
  DPB0162Req,
  RespDPB0162,
  ReqDPB0162,
} from './../../models/api/ServerService/dpb0162.interface';
import {
  DPB0161Req,
  ReqDPB0161,
  RespDPB0161,
  RespDPB0161Before,
} from './../../models/api/ServerService/dpb0161.interface';
import {
  DPB0160Req,
  RespDPB0160,
  ReqDPB0160,
  RespDPB0160Before,
} from './../../models/api/ServerService/dpb0160.interface';
import {
  DPB0159Req,
  ReqDPB0159,
  RespDPB0159,
} from './../../models/api/ServerService/dpb0159.interface';
import {
  DPB9922Req,
  RespDPB9922,
  ReqDPB9922,
} from './../../models/api/ServerService/dpb9922.interface';
import { ReqDPB9921 } from './../../models/api/ServerService/dpb9921.interface';
import {
  DPB0146Req,
  RespDPB0146,
  ReqDPB0146,
} from './../../models/api/ServerService/dpb0146.interface';
import {
  RespDPB0145,
  DPB0145Req,
  ReqDPB0145,
} from './../../models/api/ServerService/dpb0145.interface';
import { RespDPB0144 } from './../../models/api/ServerService/dpb0144.interface';
import {
  DPB0142Req,
  RespDPB0142,
  ReqDPB0142,
} from './../../models/api/ServerService/dpb0142.interface';
import {
  DPB0127Req,
  ReqDPB0127,
  RespDPB0127,
  ReqDPB0127Before,
} from './../../models/api/ServerService/dpb0127.interface';
import {
  DPB0126Req,
  RespDPB0126,
  ReqDPB0126,
} from './../../models/api/ServerService/dpb0126.interface';
import {
  DPB9914Req,
  RespDPB9914,
  ReqDPB9914,
} from './../../models/api/ServerService/dpb9914.interface';
import {
  ResDPB9913Before,
  ReqDPB9913,
  DPB9913Req,
  RespDPB9913,
} from './../../models/api/ServerService/dpb9913.interface';
import {
  DPB9911Req,
  RespDPB9911,
} from './../../models/api/ServerService/dpb9911.interface';
import {
  ResDPB9912Before,
  ReqDPB9912,
  RespDPB9912,
  DPB9912Req,
} from './../../models/api/ServerService/dpb9912.interface';
import {
  DPB9910Req,
  RespDPB9910,
  ReqDPB9910,
} from './../../models/api/ServerService/dpb9910.interface';
import {
  DPB9907Req,
  RespDPB9907,
  ReqDPB9907,
} from './../../models/api/ServerService/dpb9907.interface';
import {
  DPB9906Req,
  RespDPB9906,
  ReqDPB9906,
} from './../../models/api/ServerService/dpb9906.interface';
import {
  DPB9908Req,
  RespDPB9908,
  ReqDPB9908,
} from './../../models/api/ServerService/dpb9908.interface';
import {
  ResDPB9909Before,
  ReqDPB9909,
  DPB9909Req,
  RespDPB9909,
} from './../../models/api/ServerService/dpb9909.interface';
import {
  RespDPB9905,
  ReqDPB9905,
} from './../../models/api/ServerService/dpb9905.interface';
import { DPB9905Req } from 'src/app/models/api/ServerService/dpb9905.interface';
import {
  DPB9904Req,
  RespDPB9904,
  ReqDPB9904,
} from './../../models/api/ServerService/dpb9904.interface';
import {
  ReqDPB9902,
  DPB9902Req,
  RespDPB9902,
} from './../../models/api/ServerService/dpb9902.interface';
import { TxID } from 'src/app/models/common.enum';
import {
  DPB9900Req,
  ReqDPB9900,
  RespDPB9900,
} from './../../models/api/ServerService/dpb9900.interface';
import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { ResDPB9902Before } from 'src/app/models/api/ServerService/dpb9902.interface';
import {
  DPB9901Req,
  RespDPB9901,
} from 'src/app/models/api/ServerService/dpb9901.interface';
import {
  DPB9903Req,
  ReqDPB9903,
  ResDPB9903Before,
  RespDPB9903,
} from 'src/app/models/api/ServerService/dpb9903.interface';
import {
  DPB0125Req,
  ReqDPB0125,
  RespDPB0125,
} from 'src/app/models/api/ServerService/dpb0125.interface';
import {
  DPB0124Req,
  ReqDPB0124,
  RespDPB0124,
} from 'src/app/models/api/ServerService/dpb0124.interface';
import {
  DPB0128Req,
  ReqDPB0128,
  RespDPB0128,
} from 'src/app/models/api/ServerService/dpb0128.interface';
import {
  DPB9915Req,
  ReqDPB9915,
  ResDPB9915Before,
  RespDPB9915,
} from 'src/app/models/api/ServerService/dpb9915.interface';
import { BaseRes } from 'src/app/models/api/base.interface';
import {
  DPB9919Req,
  ReqDPB9919,
} from 'src/app/models/api/ServerService/dpb9919.interface';
import {
  DPB9920Req,
  ReqDPB9920,
} from 'src/app/models/api/ServerService/dpb9920.interface';
import {
  DPB9917Req,
  ReqDPB9917,
  ResDPB9917Before,
} from 'src/app/models/api/ServerService/dpb9917.interface';
import {
  DPB9918Req,
  ReqDPB9918,
  ResDPB9918Before,
  RespDPB9918,
} from 'src/app/models/api/ServerService/dpb9918.interface';
import {
  DPB9916Req,
  ReqDPB9916,
  RespDPB9916,
} from 'src/app/models/api/ServerService/dpb9916.interface';
import {
  ReqDPB0149,
  RespDPB0149,
} from 'src/app/models/api/ServerService/dpb0149.interface';
import {
  DPB0150Req,
  ReqDPB0150,
  RespDPB0150,
  RespDPB0150Before,
} from 'src/app/models/api/ServerService/dpb0150.interface';
import {
  DPB0151Req,
  ReqDPB0151,
  RespDPB0151,
} from 'src/app/models/api/ServerService/dpb0151.interface';
import {
  DPB0152Req,
  ReqDPB0152,
  RespDPB0152,
} from 'src/app/models/api/ServerService/dpb0152.interface';
import {
  DPB0147Req,
  ReqDPB0147,
  RespDPB0147,
  RespDPB0147Before,
} from 'src/app/models/api/ServerService/dpb0147.interface';
import {
  DPB0148Req,
  ReqDPB0148,
  RespDPB0148,
} from 'src/app/models/api/ServerService/dpb0148.interface';
import {
  DPB0174Req,
  ReqDPB0174,
  RespDPB0174,
} from 'src/app/models/api/ServerService/dpb0174.interface';
import {
  DPB0176Req,
  ReqDPB0176,
  RespDPB0176,
  RespDPB0176Before,
} from 'src/app/models/api/ServerService/dpb0176.interface';
import {
  DPB0175Req,
  ReqDPB0175,
  RespDPB0175,
} from 'src/app/models/api/ServerService/dpb0175.interface';
import {
  DPB0177Req,
  ReqDPB0177,
  RespDPB0177,
  RespDPB0177Before,
} from 'src/app/models/api/ServerService/dpb0177.interface';
import {
  DPB0178Req,
  ReqDPB0178,
  RespDPB0178,
} from 'src/app/models/api/ServerService/dpb0178.interface';
import {
  DPB0163Req,
  ReqDPB0163,
  RespDPB0163,
  RespDPB0163Before,
} from 'src/app/models/api/ServerService/dpb0163.interface';
import {
  DPB0164Req,
  ReqDPB0164,
  RespDPB0164,
} from 'src/app/models/api/ServerService/dpb0164.interface';
import {
  DPB0165Req,
  ReqDPB0165,
  RespDPB0165,
} from 'src/app/models/api/ServerService/dpb0165.interface';
import {
  DPB0166Req,
  ReqDPB0166,
  RespDPB0166,
  RespDPB0166Before,
} from 'src/app/models/api/ServerService/dpb0166.interface';
import {
  DPB0167Req,
  ReqDPB0167,
  RespDPB0167,
} from 'src/app/models/api/ServerService/dpb0167.interface';
import {
  DPB0168Req,
  ReqDPB0168,
  RespDPB0168,
} from 'src/app/models/api/ServerService/dpb0168.interface';
import {
  DPB0153Req,
  ReqDPB0153,
  RespDPB0153,
} from 'src/app/models/api/ServerService/dpb0153.interface';
import {
  DPB0157Req,
  ReqDPB0157,
  RespDPB0157,
} from 'src/app/models/api/ServerService/dpb0157.interface';
import {
  DPB0154Req,
  ReqDPB0154,
  RespDPB0154,
  RespDPB0154Before,
} from 'src/app/models/api/ServerService/dpb0154.interface';
import {
  DPB0158Req,
  ReqDPB0158,
  RespDPB0158,
} from 'src/app/models/api/ServerService/dpb0158.interface';
import {
  DPB0155Req,
  ReqDPB0155,
  RespDPB0155,
  RespDPB0155Before,
} from 'src/app/models/api/ServerService/dpb0155.interface';
import {
  DPB0156Req,
  ReqDPB0156,
  RespDPB0156,
} from 'src/app/models/api/ServerService/dpb0156.interface';
import {
  DPB0095Req,
  ReqDPB0095,
  RespDPB0095,
} from 'src/app/models/api/OpenApiService/dpb0095.interface';
import {
  DPB0169Req,
  ReqDPB0169,
  RespDPB0169,
} from 'src/app/models/api/ServerService/dpb0169.interface';
import {
  DPB0170Req,
  ReqDPB0170,
  RespDPB0170,
} from 'src/app/models/api/ServerService/dpb0170.interface';
import {
  DPB0171Req,
  ReqDPB0171,
  RespDPB0171,
  RespDPB0171Before,
} from 'src/app/models/api/ServerService/dpb0171.interface';
import {
  DPB0172Req,
  ReqDPB0172,
  RespDPB0172,
  RespDPB0172Before,
} from 'src/app/models/api/ServerService/dpb0172.interface';
import {
  DPB0173Req,
  ReqDPB0173,
  RespDPB0173,
} from 'src/app/models/api/ServerService/dpb0173.interface';
import {
  AA1211Req,
  ReqAA1211,
  ResAA1211,
} from 'src/app/models/api/ReportService/aa1211.interface';
import {
  DPB0179Resp,
  ReqDPB0179,
  RespDPB0179,
} from 'src/app/models/api/ServerService/dpb0179.interface';
import {
  DPB0181Req,
  ReqDPB0181,
  RespDPB0181,
  RespDPB0181Before,
} from 'src/app/models/api/ServerService/dpb0181.interface';
import {
  DPB0180Req,
  ReqDPB0180,
  RespDPB0180,
} from 'src/app/models/api/ServerService/dpb0180.interface';
import {
  DPB0182Req,
  ReqDPB0182,
  RespDPB0182,
  RespDPB0182Before,
} from 'src/app/models/api/ServerService/dpb0182.interface';
import { DPB0184Req } from 'src/app/models/api/ServerService/dpb0184.interface';
import {
  DPB0185Req,
  ReqDPB0185,
  RespDPB0185,
} from 'src/app/models/api/ServerService/dpb0185.interface';
import {
  DPB0186Req,
  ReqDPB0186,
  RespDPB0186,
  RespDPB0186Before,
} from 'src/app/models/api/ServerService/dpb0186.interface';
import {
  DPB0187Req,
  ReqDPB0187,
  RespDPB0187,
  RespDPB0187Before,
} from 'src/app/models/api/ServerService/dpb0187.interface';
import {
  DPB0188Req,
  ReqDPB0188,
  RespDPB0188,
} from 'src/app/models/api/ServerService/dpb0188.interface';
import {
  DPB0195Req,
  ReqDPB0195,
  RespDPB0195,
} from 'src/app/models/api/ServerService/dpb0195.interface';
import {
  DPB0197Req,
  ReqDPB0197,
  RespDPB0197,
  RespDPB0197Before,
} from 'src/app/models/api/ServerService/dpb0197.interface';
import {
  DPB0196Req,
  ReqDPB0196,
  RespDPB0196,
} from 'src/app/models/api/ServerService/dpb0196.interface';
import {
  DPB0198Req,
  ReqDPB0198,
  RespDPB0198,
  RespDPB0198Before,
} from 'src/app/models/api/ServerService/dpb0198.interface';
import {
  DPB0199Req,
  ReqDPB0199,
  RespDPB0199,
} from 'src/app/models/api/ServerService/dpb0199.interface';
import {
  DPB0183Req,
  ReqDPB0183,
  RespDPB0183,
} from 'src/app/models/api/ServerService/dpb0183.interface';
import {
  DPB0190Req,
  ReqDPB0190,
  RespDPB0190,
} from 'src/app/models/api/ServerService/dpb0190.interface';
import {
  DPB0191Req,
  ReqDPB0191,
  RespDPB0191,
} from 'src/app/models/api/ServerService/dpb0191.interface';
import {
  DPB0192Req,
  ReqDPB0192,
  RespDPB0192,
  RespDPB0192Before,
} from 'src/app/models/api/ServerService/dpb0192.interface';
import {
  DPB0193Req,
  ReqDPB0193,
  RespDPB0193,
  RespDPB0193Before,
} from 'src/app/models/api/ServerService/dpb0193.interface';
import {
  DPB0194Req,
  ReqDPB0194,
  RespDPB0194,
} from 'src/app/models/api/ServerService/dpb0194.interface';
import {
  DPB0200Req,
  ReqDPB0200,
  RespDPB0200,
} from 'src/app/models/api/ServerService/dpb0200.interface';
import {
  DPB0201Req,
  ReqDPB0201,
  RespDPB0201,
} from 'src/app/models/api/ServerService/dpb0201.interface';
import {
  DPB0202Req,
  ReqDPB0202,
  RespDPB0202,
} from 'src/app/models/api/ServerService/dpb0202.interface';
import {
  DPB0204Req,
  ReqDPB0204,
  RespDPB0204,
  RespDPB0204Before,
} from 'src/app/models/api/ServerService/dpb0204.interface';
import {
  DPB0203Req,
  ReqDPB0203,
  RespDPB0203,
} from 'src/app/models/api/ServerService/dpb0203.interface';
import {
  DPB0205Req,
  ReqDPB0205,
  RespDPB0205,
  RespDPB0205Before,
} from 'src/app/models/api/ServerService/dpb0205.interface';
import {
  DPB0206Req,
  ReqDPB0206,
  RespDPB0206,
} from 'src/app/models/api/ServerService/dpb0206.interface';
import {
  DPB0207Req,
  ReqDPB0207,
  RespDPB0207,
} from 'src/app/models/api/ServerService/dpb0207.interface';
import {
  DPB0209Req,
  ReqDPB0209,
  RespDPB0209,
} from 'src/app/models/api/ServerService/dpb0209.interface';
import { ReqDPB9923 } from 'src/app/models/api/ServerService/dpb9923.interface';
import { DPB9927Req } from 'src/app/models/api/ServerService/dpb9927.interface';
import {
  DPB0294Req,
  ReqDPB0294,
  RespDPB0294,
} from 'src/app/models/api/ServerService/dpb0294.interface';
import {
  DPB0290Req,
  ReqDPB0290,
  RespDPB0290,
  RespDPB0290Before,
} from 'src/app/models/api/ServerService/dpb0290.interface';
import { DPB0292Req, ReqDPB0292, RespDPB0292, RespDPB0292Before } from 'src/app/models/api/ServerService/dpb0292.interface';
import { DPB0293Req, ReqDPB0293, RespDPB0293 } from 'src/app/models/api/ServerService/dpb0293.interface';
import { DPB0291Req, ReqDPB0291, RespDPB0291 } from 'src/app/models/api/ServerService/dpb0291.interface';
import { DPB0220Req, ReqDPB0220, RespDPB0220 } from 'src/app/models/api/ServerService/dpb0220.interface';
import { DPB0222Req, DPB0222RespBefore, ReqDPB0222, RespDPB0222 } from 'src/app/models/api/ServerService/dpb0222.interface';
import { DPB0221Req, ReqDPB0221, RespDPB0221 } from 'src/app/models/api/ServerService/dpb0221.interface';
import { DPB0223Req, DPB0223RespBefore, ReqDPB0223, RespDPB0223, RespDPB0223RespBefore } from 'src/app/models/api/ServerService/dpb0223.interface';
import { DPB0224Req, ReqDPB0224, RespDPB0224 } from 'src/app/models/api/ServerService/dpb0224.interface';
import { DPB0240Req, ReqDPB0240, RespDPB0240 } from 'src/app/models/api/ServerService/dpb0240.interface';
import { DPB0242Req, ReqDPB0242, RespDPB0242, RespDPB0242Before } from 'src/app/models/api/ServerService/dpb0242.interface';
import { DPB0241Req, ReqDPB0241, RespDPB0241 } from 'src/app/models/api/ServerService/dpb0241.interface';
import { DPB0243Req, ReqDPB0243, RespDPB0243, RespDPB0243Before } from 'src/app/models/api/ServerService/dpb0243.interface';
import { DPB0244Req, ReqDPB0244, RespDPB0244 } from 'src/app/models/api/ServerService/dpb0244.interface';
import { DPB0234Req, ReqDPB0234, RespDPB0234 } from 'src/app/models/api/ServerService/dpb0234.interface';
import { DPB0232Req, ReqDPB0232, RespDPB0232 } from 'src/app/models/api/ServerService/dpb0232.interface';
import { DPB0233Req, DPB0233RespBefore, ReqDPB0233, RespDPB0233 } from 'src/app/models/api/ServerService/dpb0233.interface';
import { ReqDPB9938, RespDPB9938 } from 'src/app/models/api/ServerService/dpb9938.interface';
import { ReqDPB9939, RespDPB9939 } from 'src/app/models/api/ServerService/dpb9939.interface';
import { DPB0280Req, ReqDPB0280, RespDPB0280 } from 'src/app/models/api/ServerService/dpb0280.interface';
import { DPB0281Req, ReqDPB0281, RespDPB0281, RespDPB0281Before } from 'src/app/models/api/ServerService/dpb0281.interface';
import { DPB0282Req, ReqDPB0282, RespDPB0282 } from 'src/app/models/api/ServerService/dpb0282.interface';
import { DPB0283Req, ReqDPB0283, RespDPB0283, RespDPB0283Before } from 'src/app/models/api/ServerService/dpb0283.interface';
import { DPB0284Req, ReqDPB0284, RespDPB0284 } from 'src/app/models/api/ServerService/dpb0284.interface';
import { DPB0285Req, ReqDPB0285, RespDPB0285 } from 'src/app/models/api/ServerService/dpb0285.interface';
import { DPB0286Req, ReqDPB0286, RespDPB0286 } from 'src/app/models/api/ServerService/dpb0286.interface';
import { AA1212Req, ReqAA1212, ResAA1212 } from 'src/app/models/api/ReportService/aa1212.interface';

@Injectable({
  providedIn: 'root',
})
export class ServerService {
  public get basePath(): string {
    return environment.isv4 ? 'dgrv4/17' : 'tsmpdpaa/17';
  }

  public get indexPath(): string {
    return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
  }

  constructor(private api: ApiBaseService) {
    this.api.baseUrl = environment.dpPath;
  }

  /**
   * 查詢TSMP_SETTING清單
   */
  queryTsmpSettingList(ReqBody: DPB9900Req): Observable<RespDPB9900> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpSettingList),
      ReqBody: ReqBody,
    } as ReqDPB9900;
    const path = `${this.basePath}/DPB9900`;
    return this.api.npPost<RespDPB9900>(path, body);
  }

  queryTsmpSettingList_ignore1298(
    ReqBody: DPB9900Req
  ): Observable<RespDPB9900> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpSettingList),
      ReqBody: ReqBody,
    } as ReqDPB9900;
    const path = `${this.basePath}/DPB9900`;
    return this.api.excuteNpPost_ignore1298<RespDPB9900>(path, body);
  }

  /*
   *  查詢TSMP_SETTING明細
   */
  queryTsmpSettingDetail(ReqBody: DPB9901Req): Observable<RespDPB9901> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpSettingDetail),
      ReqBody: ReqBody,
    } as ReqDPB9902;
    const path = `${this.basePath}/DPB9901`;
    return this.api.excuteNpPost<RespDPB9901>(path, body);
  }

  /**
   * before
   * DPB9902: create TSMP_SETTING
   * @param ReqBody
   */
  addTsmpSetting_before(): Observable<ResDPB9902Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTsmpSetting),
      ReqBody: {},
    } as ReqDPB9902;
    const path = `${this.basePath}/DPB9902?before`;
    return this.api.excuteNpPost<ResDPB9902Before>(path, body);
  }

  /**
   * DPB9902: create TSMP_SETTING
   * @param ReqBody
   */
  addTsmpSetting(ReqBody: DPB9902Req): Observable<RespDPB9902> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTsmpSetting),
      ReqBody: ReqBody,
    } as ReqDPB9902;
    const path = `${this.basePath}/DPB9902`;
    return this.api.excuteNpPost<RespDPB9902>(path, body);
  }

  updateTsmpSetting_before(): Observable<ResDPB9903Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTsmpSetting),
      ReqBody: {},
    } as ReqDPB9903;
    const path = `${this.basePath}/DPB9903?before`;
    return this.api.excuteNpPost<ResDPB9903Before>(path, body);
  }

  /**更新TSMP_SETTING */
  updateTsmpSetting(ReqBody: DPB9903Req): Observable<RespDPB9903> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTsmpSetting),
      ReqBody: ReqBody,
    } as ReqDPB9903;
    const path = `${this.basePath}/DPB9903`;
    return this.api.excuteNpPost<RespDPB9903>(path, body);
  }

  /**刪除TSMP_SETTING */
  deleteTsmpSetting(ReqBody: DPB9904Req): Observable<RespDPB9904> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteTsmpSetting),
      ReqBody: ReqBody,
    } as ReqDPB9904;
    const path = `${this.basePath}/DPB9904`;
    return this.api.excuteNpPost<RespDPB9904>(path, body);
  }

  /*查詢TSMP_DP_ITEMS清單 */
  queryTsmpDpItemsList(DPB9905Req: DPB9905Req): Observable<RespDPB9905> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpDpItemsList),
      ReqBody: DPB9905Req,
    } as ReqDPB9905;
    const path = `${this.basePath}/DPB9905`;
    return this.api.npPost<RespDPB9905>(path, body);
  }

  updateTsmpDpItemsDetail_before(): Observable<ResDPB9909Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTsmpDpItemsDetail),
      ReqBody: {},
    } as ReqDPB9909;
    const path = `${this.basePath}/DPB9909?before`;
    return this.api.excuteNpPost<ResDPB9909Before>(path, body);
  }

  updateTsmpDpItemsDetail(DPB9909Req: DPB9909Req): Observable<RespDPB9909> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTsmpDpItemsDetail),
      ReqBody: DPB9909Req,
    } as ReqDPB9909;
    const path = `${this.basePath}/DPB9909`;
    return this.api.npPost<RespDPB9909>(path, body);
  }

  updateItemNameList(DPB9908Req: DPB9908Req): Observable<RespDPB9908> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateItemNameList),
      ReqBody: DPB9908Req,
    } as ReqDPB9908;
    const path = `${this.basePath}/DPB9908`;
    return this.api.npPost<RespDPB9908>(path, body);
  }

  /*查詢TSMP_DP_ITEMS明細 */
  queryTsmpDpItemsDetail(DPB9906Req: DPB9906Req): Observable<RespDPB9906> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpDpItemsList),
      ReqBody: DPB9906Req,
    } as ReqDPB9906;
    const path = `${this.basePath}/DPB9906`;
    return this.api.npPost<RespDPB9906>(path, body);
  }

  queryItemNameList(DPB9907Req: DPB9907Req): Observable<RespDPB9907> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryItemNameList),
      ReqBody: DPB9907Req,
    } as ReqDPB9907;
    const path = `${this.basePath}/DPB9907`;
    return this.api.npPost<RespDPB9907>(path, body);
  }

  queryCusSettingList(DPB9910Req: DPB9910Req): Observable<RespDPB9910> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryCusSettingList),
      ReqBody: DPB9910Req,
    } as ReqDPB9910;
    const path = `${this.basePath}/DPB9910`;
    return this.api.npPost<RespDPB9910>(path, body);
  }

  queryCusSettingList_ignore1298(
    DPB9910Req: DPB9910Req
  ): Observable<RespDPB9910> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryCusSettingList),
      ReqBody: DPB9910Req,
    } as ReqDPB9910;
    const path = `${this.basePath}/DPB9910`;
    return this.api.npPost<RespDPB9910>(path, body);
  }

  queryCusSettingDetail(DPB9911Req: DPB9911Req): Observable<RespDPB9911> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryCusSettingDetail),
      ReqBody: DPB9911Req,
    } as ReqDPB9912;
    const path = `${this.basePath}/DPB9911`;
    return this.api.npPost<RespDPB9911>(path, body);
  }

  addCusSetting_before(): Observable<ResDPB9912Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addCusSetting),
      ReqBody: {},
    } as ReqDPB9912;
    const path = `${this.basePath}/DPB9912?before`;
    return this.api.excuteNpPost<ResDPB9912Before>(path, body);
  }

  addCusSetting(DPB9912Req: DPB9912Req): Observable<RespDPB9912> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addCusSetting),
      ReqBody: DPB9912Req,
    } as ReqDPB9912;
    const path = `${this.basePath}/DPB9912`;
    return this.api.npPost<RespDPB9912>(path, body);
  }

  updateCusSetting_before(): Observable<ResDPB9913Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateCusSetting),
      ReqBody: {},
    } as ReqDPB9913;
    const path = `${this.basePath}/DPB9913?before`;
    return this.api.excuteNpPost<ResDPB9913Before>(path, body);
  }

  updateCusSetting(DPB9913Req: DPB9913Req): Observable<RespDPB9913> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateCusSetting),
      ReqBody: DPB9913Req,
    } as ReqDPB9913;
    const path = `${this.basePath}/DPB9913`;
    return this.api.npPost<RespDPB9913>(path, body);
  }

  deleteCusSetting(ReqBody: DPB9914Req): Observable<RespDPB9914> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteCusSetting),
      ReqBody: ReqBody,
    } as ReqDPB9914;
    const path = `${this.basePath}/DPB9914`;
    return this.api.npPost<RespDPB9914>(path, body);
  }

  queryAllIndex(ReqBody: DPB0126Req): Observable<RespDPB0126> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryAllIndex),
      ReqBody: ReqBody,
    } as ReqDPB0126;
    const path = `${this.indexPath}/DPB0126`;
    return this.api.npPost<RespDPB0126>(path, body);
  }

  getIndex(ReqBody: DPB0125Req): Observable<RespDPB0125> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getIndex),
      ReqBody: ReqBody,
    } as ReqDPB0125;
    const path = `${this.indexPath}/DPB0125`;
    return this.api.npPost<RespDPB0125>(path, body);
  }

  updateIndexOpenOrClose(ReqBody: DPB0124Req): Observable<RespDPB0124> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIndexOpenOrClose),
      ReqBody: ReqBody,
    } as ReqDPB0124;
    const path = `${this.indexPath}/DPB0124`;
    return this.api.npPost<RespDPB0124>(path, body);
  }

  querySALMaster(ReqBody: DPB0127Req): Observable<RespDPB0127> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.querySALMaster),
      ReqBody: ReqBody,
    } as ReqDPB0127;
    const path = `${this.indexPath}/DPB0127`;
    return this.api.npPost<RespDPB0127>(path, body);
  }

  querySALMaster_ignore1298(ReqBody: DPB0127Req): Observable<RespDPB0127> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.querySALMaster),
      ReqBody: ReqBody,
    } as ReqDPB0127;
    const path = `${this.indexPath}/DPB0127`;
    return this.api.excuteNpPost_ignore1298<RespDPB0127>(path, body);
  }

  querySALMaster_before(): Observable<ReqDPB0127Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.querySALMaster),
      ReqBody: {},
    } as ReqDPB0127;
    const path = `${this.indexPath}/DPB0127?before`;
    return this.api.npPost<ReqDPB0127Before>(path, body);
  }

  querySALDetail(ReqBody: DPB0128Req): Observable<RespDPB0128> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.querySALDetail),
      ReqBody: ReqBody,
    } as ReqDPB0128;
    const path = `${this.indexPath}/DPB0128`;
    return this.api.npPost<RespDPB0128>(path, body);
  }

  getACEntryTicket(ReqBody: DPB0142Req): Observable<RespDPB0142> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getACEntryTicket),
      ReqBody: ReqBody,
    } as ReqDPB0142;
    const path = `${this.indexPath}/DPB0142`;
    return this.api.npPost<RespDPB0142>(path, body);
  }

  /**
   * 查詢TSMP_DP_FILE清單
   */
  queryTsmpdpFileList(ReqBody: DPB9915Req): Observable<RespDPB9915> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpdpFileList),
      ReqBody: ReqBody,
    } as ReqDPB9915;
    const path = `${this.basePath}/DPB9915`;
    return this.api.npPost<RespDPB9915>(path, body);
  }

  queryTsmpdpFileList_ignore1298(ReqBody: DPB9915Req): Observable<RespDPB9915> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpSettingList),
      ReqBody: ReqBody,
    } as ReqDPB9915;
    const path = `${this.basePath}/DPB9915`;
    return this.api.excuteNpPost_ignore1298<RespDPB9915>(path, body);
  }

  reductionTsmpdpFile(ReqBody: DPB9919Req): Observable<BaseRes> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.reductionTsmpdpFile),
      ReqBody: ReqBody,
    } as ReqDPB9919;
    const path = `${this.basePath}/DPB9919`;
    return this.api.npPost(path, body);
  }

  deletePermanentlyTsmpdpFile(ReqBody: DPB9920Req): Observable<BaseRes> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deletePermanentlyTsmpdpFile),
      ReqBody: ReqBody,
    } as ReqDPB9920;
    const path = `${this.basePath}/DPB9920`;
    return this.api.npPost(path, body);
  }

  addTsmpdpFile_before(): Observable<ResDPB9917Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTsmpdpFile),
      ReqBody: {},
    } as ReqDPB9902;
    const path = `${this.basePath}/DPB9917?before`;
    return this.api.excuteNpPost<ResDPB9917Before>(path, body);
  }

  addTsmpdpFile(ReqBody: DPB9917Req): Observable<BaseRes> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTsmpdpFile),
      ReqBody: ReqBody,
    } as ReqDPB9917;
    const path = `${this.basePath}/DPB9917`;
    return this.api.npPost<BaseRes>(path, body);
  }

  updateTsmpdpFile_before(): Observable<ResDPB9918Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTsmpSetting),
      ReqBody: {},
    } as ReqDPB9903;
    const path = `${this.basePath}/DPB9918?before`;
    return this.api.excuteNpPost<ResDPB9918Before>(path, body);
  }

  queryTsmpdpFileDetail(ReqBody: DPB9916Req): Observable<RespDPB9916> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpSettingDetail),
      ReqBody: ReqBody,
    } as ReqDPB9916;
    const path = `${this.basePath}/DPB9916`;
    return this.api.excuteNpPost<RespDPB9916>(path, body);
  }

  updateTsmpdpFile(ReqBody: DPB9918Req): Observable<RespDPB9918> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTsmpSetting),
      ReqBody: ReqBody,
    } as ReqDPB9918;
    const path = `${this.basePath}/DPB9918`;
    return this.api.excuteNpPost<RespDPB9918>(path, body);
  }

  queryTsmpdpFile_before(): Observable<ResDPB9915Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpdpFileList),
      ReqBody: {},
    } as ReqDPB9902;
    const path = `${this.basePath}/DPB9915?before`;
    return this.api.excuteNpPost<ResDPB9915Before>(path, body);
  }

  /* 查詢監控 */
  queryMonitor(): Observable<RespDPB0144> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryMonitor),
      ReqBody: {},
    } as ReqDPB9902;
    const path = `${this.indexPath}/DPB0144`;
    return this.api.excuteNpPost<RespDPB0144>(path, body);
  }

  // Delegate AC User
  QueryALL(): Observable<RespDPB0145> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.QueryALL),
      ReqBody: {},
    } as ReqDPB0145;
    const path = `${this.indexPath}/DPB0145`;
    return this.api.excuteNpPost<RespDPB0145>(path, body);
  }

  QueryDetail(ReqBody: DPB0146Req): Observable<RespDPB0146> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.QueryDetail),
      ReqBody: ReqBody,
    } as ReqDPB0146;
    const path = `${this.indexPath}/DPB0146`;
    return this.api.excuteNpPost<RespDPB0146>(path, body);
  }

  UpdateOne_Role_Org(ReqBody: DPB0147Req): Observable<RespDPB0147> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.UpdateOne_Role_Org),
      ReqBody: ReqBody,
    } as ReqDPB0147;
    const path = `${this.indexPath}/DPB0147`;
    return this.api.excuteNpPost<RespDPB0147>(path, body);
  }

  UpdateOne_Role_Org_before(): Observable<RespDPB0147Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.UpdateOne_Role_Org),
      ReqBody: {},
    } as ReqDPB0147;
    const path = `${this.indexPath}/DPB0147?before`;
    return this.api.excuteNpPost<RespDPB0147Before>(path, body);
  }

  DeleteOne(ReqBody: DPB0148Req): Observable<RespDPB0148> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.DeleteOne),
      ReqBody: ReqBody,
    } as ReqDPB0148;
    const path = `${this.indexPath}/DPB0148`;
    return this.api.excuteNpPost<RespDPB0148>(path, body);
  }

  //AC OAuth 2.0 Idp
  QueryDgrAcIdInfoAll(): Observable<RespDPB0149> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.QueryDgrAcIdInfoAll),
      ReqBody: {},
    } as ReqDPB0149;
    const path = `${this.indexPath}/DPB0149`;
    return this.api.excuteNpPost<RespDPB0149>(path, body);
  }

  AddDgrAcIdInfo(ReqBody: DPB0150Req): Observable<RespDPB0150> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.AddDgrAcIdInfo),
      ReqBody: ReqBody,
    } as ReqDPB0150;
    const path = `${this.indexPath}/DPB0150`;
    return this.api.excuteNpPost<RespDPB0150>(path, body);
  }

  AddDgrAcIdInfo_before(): Observable<RespDPB0150Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.AddDgrAcIdInfo),
      ReqBody: {},
    } as ReqDPB0150;
    const path = `${this.indexPath}/DPB0150?before`;
    return this.api.excuteNpPost<RespDPB0150Before>(path, body);
  }

  updateDgrAcIdInfo(ReqBody: DPB0151Req): Observable<RespDPB0151> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateDgrAcIdInfo),
      ReqBody: ReqBody,
    } as ReqDPB0151;
    const path = `${this.indexPath}/DPB0151`;
    return this.api.excuteNpPost<RespDPB0151>(path, body);
  }

  deleteDgrAcIdInfo(ReqBody: DPB0152Req): Observable<RespDPB0152> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteDgrAcIdInfo),
      ReqBody: ReqBody,
    } as ReqDPB0152;
    const path = `${this.indexPath}/DPB0152`;
    return this.api.excuteNpPost<RespDPB0152>(path, body);
  }

  // 匯出tsmp setting
  exportTsmpSetting(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportTsmpSetting),
      ReqBody: {},
    } as ReqDPB9921;
    const path = `${this.basePath}/DPB9921`;
    return this.api.excutePostGetFile(path, body);
  }

  importTsmpSetting(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9922`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  queryIdPInfoList_ldap(ReqBody: DPB0159Req): Observable<RespDPB0159> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoList_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0159;
    const path = `${this.indexPath}/DPB0159`;
    return this.api.excuteNpPost<RespDPB0159>(path, body);
  }

  createIdPInfo_ldap(ReqBody: DPB0160Req): Observable<RespDPB0160> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0160;
    const path = `${this.indexPath}/DPB0160`;
    return this.api.excuteNpPost<RespDPB0160>(path, body);
  }

  createIdPInfo_ldap_before(): Observable<RespDPB0160Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_ldap),
      ReqBody: {},
    } as ReqDPB0160;
    const path = `${this.indexPath}/DPB0160?before`;
    return this.api.excuteNpPost<RespDPB0160Before>(path, body);
  }

  updateIdPInfo_ldap(ReqBody: DPB0161Req): Observable<RespDPB0161> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIdPInfo_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0161;
    const path = `${this.indexPath}/DPB0161`;
    return this.api.excuteNpPost<RespDPB0161>(path, body);
  }

  updateIdPInfo_ldap_before(): Observable<RespDPB0161Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIdPInfo_ldap),
      ReqBody: {},
    } as ReqDPB0161;
    const path = `${this.indexPath}/DPB0161?before`;
    return this.api.excuteNpPost<RespDPB0161Before>(path, body);
  }

  deleteIdPInfo_ldap(ReqBody: DPB0162Req): Observable<RespDPB0162> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteIdPInfo_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0162;
    const path = `${this.indexPath}/DPB0162`;
    return this.api.excuteNpPost<RespDPB0162>(path, body);
  }

  createIdPUser(ReqBody: DPB0163Req): Observable<RespDPB0163> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPUser),
      ReqBody: ReqBody,
    } as ReqDPB0163;
    const path = `${this.indexPath}/DPB0163`;
    return this.api.excuteNpPost<RespDPB0163>(path, body);
  }

  createIdPUser_before(): Observable<RespDPB0163Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPUser),
      ReqBody: {},
    } as ReqDPB0163;
    const path = `${this.indexPath}/DPB0163?before`;
    return this.api.excuteNpPost<RespDPB0163Before>(path, body);
  }

  queryWsList(ReqBody: DPB0174Req): Observable<RespDPB0174> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWsList),
      ReqBody: ReqBody,
    } as ReqDPB0174;
    const path = `${this.indexPath}/DPB0174`;
    return this.api.excuteNpPost<RespDPB0174>(path, body);
  }

  queryWsList_ignore1298(ReqBody: DPB0174Req): Observable<RespDPB0174> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWsList),
      ReqBody: ReqBody,
    } as ReqDPB0174;
    const path = `${this.indexPath}/DPB0174`;
    return this.api.excuteNpPost_ignore1298<RespDPB0174>(path, body);
  }

  createWs(ReqBody: DPB0176Req): Observable<RespDPB0176> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createWs),
      ReqBody: ReqBody,
    } as ReqDPB0176;
    const path = `${this.indexPath}/DPB0176`;
    return this.api.excuteNpPost<RespDPB0176>(path, body);
  }

  createWs_before(): Observable<RespDPB0176Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createWs),
      ReqBody: {},
    } as ReqDPB0176;
    const path = `${this.indexPath}/DPB0176?before`;
    return this.api.excuteNpPost<RespDPB0176Before>(path, body);
  }

  queryWsDetail(ReqBody: DPB0175Req): Observable<RespDPB0175> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWsDetail),
      ReqBody: ReqBody,
    } as ReqDPB0175;
    const path = `${this.indexPath}/DPB0175`;
    return this.api.excuteNpPost<RespDPB0175>(path, body);
  }

  updateWs(ReqBody: DPB0177Req): Observable<RespDPB0177> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateWs),
      ReqBody: ReqBody,
    } as ReqDPB0177;
    const path = `${this.indexPath}/DPB0177`;
    return this.api.excuteNpPost<RespDPB0177>(path, body);
  }

  updateWs_before(): Observable<RespDPB0177Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateWs),
      ReqBody: {},
    } as ReqDPB0177;
    const path = `${this.indexPath}/DPB0177?before`;
    return this.api.excuteNpPost<RespDPB0177Before>(path, body);
  }

  deleteWs(ReqBody: DPB0178Req): Observable<RespDPB0178> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteWs),
      ReqBody: ReqBody,
    } as ReqDPB0178;
    const path = `${this.indexPath}/DPB0178`;
    return this.api.excuteNpPost<RespDPB0178>(path, body);
  }

  queryGtwIdPInfoByClientId_ldap(ReqBody: DPB0164Req): Observable<RespDPB0164> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoByClientId_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0164;
    const path = `${this.indexPath}/DPB0164`;
    return this.api.excuteNpPost<RespDPB0164>(path, body);
  }

  queryIdPUserDetail_ldap(ReqBody: DPB0165Req): Observable<RespDPB0165> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPUserDetail_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0165;
    const path = `${this.indexPath}/DPB0165`;
    return this.api.excuteNpPost<RespDPB0165>(path, body);
  }

  createGtwIdPInfo_ldap(ReqBody: DPB0166Req): Observable<RespDPB0166> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0166;
    const path = `${this.indexPath}/DPB0166`;
    return this.api.excuteNpPost<RespDPB0166>(path, body);
  }

  createGtwIdPInfo_ldap_before(): Observable<RespDPB0166Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_ldap),
      ReqBody: {},
    } as ReqDPB0166;
    const path = `${this.indexPath}/DPB0166?before`;
    return this.api.excuteNpPost<RespDPB0166Before>(path, body);
  }

  updateGtwIdPInfo_ldap(ReqBody: DPB0167Req): Observable<RespDPB0167> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0167;
    const path = `${this.indexPath}/DPB0167`;
    return this.api.excuteNpPost<RespDPB0167>(path, body);
  }

  updateGtwIdPInfo_ldap_before(): Observable<RespDPB0167Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_ldap),
      ReqBody: {},
    } as ReqDPB0167;
    const path = `${this.indexPath}/DPB0167?before`;
    return this.api.excuteNpPost<RespDPB0167Before>(path, body);
  }

  deleteGtwIdPInfo_ldap(ReqBody: DPB0168Req): Observable<RespDPB0168> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteGtwIdPInfo_ldap),
      ReqBody: ReqBody,
    } as ReqDPB0168;
    const path = `${this.indexPath}/DPB0168`;
    return this.api.excuteNpPost<RespDPB0168>(path, body);
  }

  queryWebsite_ignore1298(ReqBody: DPB0153Req): Observable<RespDPB0153> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWebsite),
      ReqBody: ReqBody,
    } as ReqDPB0153;
    const path = `${this.indexPath}/DPB0153`;
    return this.api.excuteNpPost_ignore1298<RespDPB0153>(path, body);
  }

  queryWebsite(ReqBody: DPB0153Req): Observable<RespDPB0153> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWebsite),
      ReqBody: ReqBody,
    } as ReqDPB0153;
    const path = `${this.indexPath}/DPB0153`;
    return this.api.excuteNpPost<RespDPB0153>(path, body);
  }

  createWebsite(ReqBody: DPB0154Req): Observable<RespDPB0154> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createWebsite),
      ReqBody: ReqBody,
    } as ReqDPB0154;
    const path = `${this.indexPath}/DPB0154`;
    return this.api.excuteNpPost<RespDPB0154>(path, body);
  }

  createWebsite_before(): Observable<RespDPB0154Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createWebsite),
      ReqBody: {},
    } as ReqDPB0154;
    const path = `${this.indexPath}/DPB0154?before`;
    return this.api.excuteNpPost<RespDPB0154Before>(path, body);
  }

  updateWebsite(ReqBody: DPB0155Req): Observable<RespDPB0155> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateWebsite),
      ReqBody: ReqBody,
    } as ReqDPB0155;
    const path = `${this.indexPath}/DPB0155`;
    return this.api.excuteNpPost<RespDPB0155>(path, body);
  }

  updateWebsite_before(): Observable<RespDPB0155Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateWebsite),
      ReqBody: {},
    } as ReqDPB0155;
    const path = `${this.indexPath}/DPB0155?before`;
    return this.api.excuteNpPost<RespDPB0155Before>(path, body);
  }

  getWebsiteInfo(ReqBody: DPB0158Req): Observable<RespDPB0158> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getWebsiteInfo),
      ReqBody: ReqBody,
    } as ReqDPB0158;
    const path = `${this.indexPath}/DPB0158`;
    return this.api.excuteNpPost<RespDPB0158>(path, body);
  }

  setDefaulWebsite(ReqBody: DPB0157Req): Observable<RespDPB0157> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.setDefaulWebsite),
      ReqBody: ReqBody,
    } as ReqDPB0157;
    const path = `${this.indexPath}/DPB0157`;
    return this.api.excuteNpPost<RespDPB0157>(path, body);
  }

  deleteWebsite(ReqBody: DPB0156Req): Observable<RespDPB0156> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteWebsite),
      ReqBody: ReqBody,
    } as ReqDPB0156;
    const path = `${this.indexPath}/DPB0156`;
    return this.api.excuteNpPost<RespDPB0156>(path, body);
  }

  queryGtwIdPInfoByClientId_oauth2(
    ReqBody: DPB0169Req
  ): Observable<RespDPB0169> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoByClientId_oauth2),
      ReqBody: ReqBody,
    } as ReqDPB0169;
    const path = `${this.indexPath}/DPB0169`;
    return this.api.excuteNpPost<RespDPB0169>(path, body);
  }

  queryGtwIdPInfo_oauth2(ReqBody: DPB0170Req): Observable<RespDPB0170> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfo_oauth2),
      ReqBody: ReqBody,
    } as ReqDPB0170;
    const path = `${this.indexPath}/DPB0170`;
    return this.api.excuteNpPost<RespDPB0170>(path, body);
  }

  createGtwIdPInfo_oauth2(ReqBody: DPB0171Req): Observable<RespDPB0171> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_oauth2),
      ReqBody: ReqBody,
    } as ReqDPB0171;
    const path = `${this.indexPath}/DPB0171`;
    return this.api.excuteNpPost<RespDPB0171>(path, body);
  }

  createGtwIdPInfo_oauth2_before(): Observable<RespDPB0171Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_oauth2),
      ReqBody: {},
    } as ReqDPB0171;
    const path = `${this.indexPath}/DPB0171?before`;
    return this.api.excuteNpPost<RespDPB0171Before>(path, body);
  }

  updateGtwIdPInfo_oauth2(ReqBody: DPB0172Req): Observable<RespDPB0172> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_oauth2),
      ReqBody: ReqBody,
    } as ReqDPB0172;
    const path = `${this.indexPath}/DPB0172`;
    return this.api.excuteNpPost<RespDPB0172>(path, body);
  }

  updateGtwIdPInfo_oauth2_before(): Observable<RespDPB0172Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_oauth2),
      ReqBody: {},
    } as ReqDPB0172;
    const path = `${this.indexPath}/DPB0172?before`;
    return this.api.excuteNpPost<RespDPB0172Before>(path, body);
  }

  deleteGtwIdPInfo_oauth2(ReqBody: DPB0173Req): Observable<RespDPB0173> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteGtwIdPInfo_oauth2),
      ReqBody: ReqBody,
    } as ReqDPB0173;
    const path = `${this.indexPath}/DPB0173`;
    return this.api.excuteNpPost<RespDPB0173>(path, body);
  }

  getDashboardData(ReqBody: AA1211Req): Observable<ResAA1211> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getDashboardData),
      ReqBody: ReqBody,
    } as ReqAA1211;
    const path = `${this.indexPath}/AA1211`;
    return this.api.excuteNpPost_ignore1298<ResAA1211>(path, body);
  }

  queryIdPInfoList_mldap(): Observable<RespDPB0179> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoList_mldap),
      ReqBody: {},
    } as ReqDPB0179;
    const path = `${this.indexPath}/DPB0179`;
    return this.api.excuteNpPost<RespDPB0179>(path, body);
  }

  createIdPInfo_mldap(reqBody: DPB0181Req): Observable<RespDPB0181> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_mldap),
      ReqBody: reqBody,
    } as ReqDPB0181;
    const path = `${this.indexPath}/DPB0181`;
    return this.api.excuteNpPost<RespDPB0181>(path, body);
  }

  createIdPInfo_mldap_before(): Observable<RespDPB0181Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_mldap),
      ReqBody: {},
    } as ReqDPB0181;
    const path = `${this.indexPath}/DPB0181?before`;
    return this.api.excuteNpPost<RespDPB0181Before>(path, body);
  }

  queryIdPInfoDetailByPk_mldap(reqBody: DPB0180Req): Observable<RespDPB0180> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoDetailByPk_mldap),
      ReqBody: reqBody,
    } as ReqDPB0180;
    const path = `${this.indexPath}/DPB0180`;
    return this.api.excuteNpPost<RespDPB0180>(path, body);
  }

  updateIdPInfo_mldap(reqBody: DPB0182Req): Observable<RespDPB0182> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIdPInfo_mldap),
      ReqBody: reqBody,
    } as ReqDPB0182;
    const path = `${this.indexPath}/DPB0182`;
    return this.api.excuteNpPost<RespDPB0182>(path, body);
  }

  updateIdPInfo_mldap_before(): Observable<RespDPB0182Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIdPInfo_mldap),
      ReqBody: {},
    } as ReqDPB0182;
    const path = `${this.indexPath}/DPB0182?before`;
    return this.api.excuteNpPost<RespDPB0182Before>(path, body);
  }

  deleteIdPInfo_mldap(ReqBody: DPB0183Req): Observable<RespDPB0183> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteIdPInfo_mldap),
      ReqBody: ReqBody,
    } as ReqDPB0183;
    const path = `${this.indexPath}/DPB0183`;
    return this.api.excuteNpPost<RespDPB0183>(path, body);
  }

  queryGtwIdPInfoByClientId_api(ReqBody: DPB0184Req): Observable<RespDPB0184> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoByClientId_api),
      ReqBody: ReqBody,
    } as ReqDPB0184;
    const path = `${this.indexPath}/DPB0184`;
    return this.api.excuteNpPost<RespDPB0184>(path, body);
  }

  queryIdPUserDetail_api(ReqBody: DPB0185Req): Observable<RespDPB0185> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPUserDetail_api),
      ReqBody: ReqBody,
    } as ReqDPB0185;
    const path = `${this.indexPath}/DPB0185`;
    return this.api.excuteNpPost<RespDPB0185>(path, body);
  }

  createGtwIdPInfo_api(ReqBody: DPB0186Req): Observable<RespDPB0186> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_api),
      ReqBody: ReqBody,
    } as ReqDPB0186;
    const path = `${this.indexPath}/DPB0186`;
    return this.api.excuteNpPost<RespDPB0186>(path, body);
  }

  createGtwIdPInfo_api_before(): Observable<RespDPB0186Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_api),
      ReqBody: {},
    } as ReqDPB0186;
    const path = `${this.indexPath}/DPB0186?before`;
    return this.api.excuteNpPost<RespDPB0186Before>(path, body);
  }

  updateGtwIdPInfo_api(ReqBody: DPB0187Req): Observable<RespDPB0187> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_api),
      ReqBody: ReqBody,
    } as ReqDPB0187;
    const path = `${this.indexPath}/DPB0187`;
    return this.api.excuteNpPost<RespDPB0187>(path, body);
  }

  updateGtwIdPInfo_api_before(): Observable<RespDPB0187Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_api),
      ReqBody: {},
    } as ReqDPB0187;
    const path = `${this.indexPath}/DPB0187?before`;
    return this.api.excuteNpPost<RespDPB0187Before>(path, body);
  }

  deleteGtwIdPInfo_api(ReqBody: DPB0188Req): Observable<RespDPB0188> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteGtwIdPInfo_api),
      ReqBody: ReqBody,
    } as ReqDPB0188;
    const path = `${this.indexPath}/DPB0188`;
    return this.api.excuteNpPost<RespDPB0188>(path, body);
  }

  queryIdPInfoList_api(ReqBody: DPB0195Req): Observable<RespDPB0195> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoList_api),
      ReqBody: ReqBody,
    } as ReqDPB0195;
    const path = `${this.indexPath}/DPB0195`;
    return this.api.excuteNpPost<RespDPB0195>(path, body);
  }

  createIdPInfo_api(ReqBody: DPB0197Req): Observable<RespDPB0197> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_api),
      ReqBody: ReqBody,
    } as ReqDPB0197;
    const path = `${this.indexPath}/DPB0197`;
    return this.api.excuteNpPost<RespDPB0197>(path, body);
  }

  createIdPInfo_api_before(): Observable<RespDPB0197Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_api),
      ReqBody: {},
    } as ReqDPB0197;
    const path = `${this.indexPath}/DPB0197?before`;
    return this.api.excuteNpPost<RespDPB0197Before>(path, body);
  }

  queryIdPInfoDetail_api(ReqBody: DPB0196Req): Observable<RespDPB0196> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoDetail_api),
      ReqBody: ReqBody,
    } as ReqDPB0196;
    const path = `${this.indexPath}/DPB0196`;
    return this.api.excuteNpPost<RespDPB0196>(path, body);
  }

  updateIdPInfo_api(ReqBody: DPB0198Req): Observable<RespDPB0198> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_api),
      ReqBody: ReqBody,
    } as ReqDPB0198;
    const path = `${this.indexPath}/DPB0198`;
    return this.api.excuteNpPost<RespDPB0198>(path, body);
  }

  updateIdPInfo_api_before(): Observable<RespDPB0198Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_api),
      ReqBody: {},
    } as ReqDPB0198;
    const path = `${this.indexPath}/DPB0198?before`;
    return this.api.excuteNpPost<RespDPB0198Before>(path, body);
  }

  deleteIdPInfo_api(ReqBody: DPB0199Req): Observable<RespDPB0199> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteIdPInfo_api),
      ReqBody: ReqBody,
    } as ReqDPB0199;
    const path = `${this.indexPath}/DPB0199`;
    return this.api.excuteNpPost<RespDPB0199>(path, body);
  }

  queryRdbConnectionInfoList(): Observable<RespDPB0190> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryRdbConnectionInfoList),
      ReqBody: {},
    } as ReqDPB0190;
    const path = `${this.indexPath}/DPB0190`;
    return this.api.npPost<RespDPB0190>(path, body);
  }

  queryRdbConnectionInfoDetail(ReqBody: DPB0191Req): Observable<RespDPB0191> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryRdbConnectionInfoDetail),
      ReqBody: ReqBody,
    } as ReqDPB0191;
    const path = `${this.indexPath}/DPB0191`;
    return this.api.npPost<RespDPB0191>(path, body);
  }

  createRdbConnectionInfo(ReqBody: DPB0192Req): Observable<RespDPB0192> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createRdbConnectionInfo),
      ReqBody: ReqBody,
    } as ReqDPB0192;
    const path = `${this.indexPath}/DPB0192`;
    return this.api.npPost<RespDPB0192>(path, body);
  }

  createRdbConnectionInfo_before(): Observable<RespDPB0192Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createRdbConnectionInfo),
      ReqBody: {},
    } as ReqDPB0192;
    const path = `${this.indexPath}/DPB0192?before`;
    return this.api.npPost<RespDPB0192Before>(path, body);
  }

  updateRdbConnectionInfo(ReqBody: DPB0193Req): Observable<RespDPB0193> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateRdbConnectionInfo),
      ReqBody: ReqBody,
    } as ReqDPB0193;
    const path = `${this.indexPath}/DPB0193`;
    return this.api.npPost<RespDPB0193>(path, body);
  }

  updateRdbConnectionInfo_before(): Observable<RespDPB0193Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateRdbConnectionInfo),
      ReqBody: {},
    } as ReqDPB0193;
    const path = `${this.indexPath}/DPB0193?before`;
    return this.api.npPost<RespDPB0193Before>(path, body);
  }

  deleteRdbConnectionInfo(ReqBody: DPB0194Req): Observable<RespDPB0194> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteRdbConnectionInfo),
      ReqBody: ReqBody,
    } as ReqDPB0194;
    const path = `${this.indexPath}/DPB0194`;
    return this.api.npPost<RespDPB0194>(path, body);
  }

  testRdbConnection(ReqBody: DPB0200Req): Observable<RespDPB0200> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.testRdbConnection),
      ReqBody: ReqBody,
    } as ReqDPB0200;
    const path = `${this.indexPath}/DPB0200`;
    return this.api.npPost<RespDPB0200>(path, body);
  }

  queryTargetThroughput(ReqBody: DPB0201Req): Observable<RespDPB0201> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTargetThroughput),
      ReqBody: ReqBody,
    } as ReqDPB0201;
    const path = `${this.indexPath}/DPB0201`;
    return this.api.npPost<RespDPB0201>(path, body);
  }

  queryGtwIdPInfoByClientId_jdbc(ReqBody: DPB0202Req): Observable<RespDPB0202> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoByClientId_jdbc),
      ReqBody: ReqBody,
    } as ReqDPB0202;
    const path = `${this.indexPath}/DPB0202`;
    return this.api.npPost<RespDPB0202>(path, body);
  }

  createGtwIdPInfo_jdbc(ReqBody: DPB0204Req): Observable<RespDPB0204> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_jdbc),
      ReqBody: ReqBody,
    } as ReqDPB0204;
    const path = `${this.indexPath}/DPB0204`;
    return this.api.npPost<RespDPB0204>(path, body);
  }

  createGtwIdPInfo_jdbc_before(): Observable<RespDPB0204Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_jdbc),
      ReqBody: {},
    } as ReqDPB0204;
    const path = `${this.indexPath}/DPB0204?before`;
    return this.api.npPost<RespDPB0204Before>(path, body);
  }

  queryGtwIdPInfoDetail_jdbc(ReqBody: DPB0203Req): Observable<RespDPB0203> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoDetail_jdbc),
      ReqBody: ReqBody,
    } as ReqDPB0203;
    const path = `${this.indexPath}/DPB0203`;
    return this.api.npPost<RespDPB0203>(path, body);
  }

  updateGtwIdPInfo_jdbc(ReqBody: DPB0205Req): Observable<RespDPB0205> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_jdbc),
      ReqBody: ReqBody,
    } as ReqDPB0205;
    const path = `${this.indexPath}/DPB0205`;
    return this.api.npPost<RespDPB0205>(path, body);
  }

  updateGtwIdPInfo_jdbc_before(): Observable<RespDPB0205Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_jdbc),
      ReqBody: {},
    } as ReqDPB0205;
    const path = `${this.indexPath}/DPB0205?before`;
    return this.api.npPost<RespDPB0205Before>(path, body);
  }

  deleteGtwIdPInfo_jdbc(ReqBody: DPB0206Req): Observable<RespDPB0206> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteGtwIdPInfo_jdbc),
      ReqBody: ReqBody,
    } as ReqDPB0206;
    const path = `${this.indexPath}/DPB0206`;
    return this.api.excuteNpPost<RespDPB0206>(path, body);
  }

  queryXApiKeyListByClientId(ReqBody: DPB0207Req): Observable<RespDPB0207> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryXApiKeyListByClientId),
      ReqBody: ReqBody,
    } as ReqDPB0207;
    const path = `${this.indexPath}/DPB0207`;
    return this.api.npPost<RespDPB0207>(path, body);
  }

  createXApiKey(ReqBody: DPB0208Req): Observable<RespDPB0208> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createXApiKey),
      ReqBody: ReqBody,
    } as ReqDPB0208;
    const path = `${this.indexPath}/DPB0208`;
    return this.api.npPost<RespDPB0208>(path, body);
  }

  createXApiKey_before(): Observable<RespDPB0208Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createXApiKey),
      ReqBody: {},
    } as ReqDPB0208;
    const path = `${this.indexPath}/DPB0208?before`;
    return this.api.npPost<RespDPB0208Before>(path, body);
  }

  deleteXApiKey(ReqBody: DPB0209Req): Observable<RespDPB0209> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteGtwIdPInfo_jdbc),
      ReqBody: ReqBody,
    } as ReqDPB0209;
    const path = `${this.indexPath}/DPB0209`;
    return this.api.excuteNpPost<RespDPB0209>(path, body);
  }

  // 匯出tsmp dp items
  exportTsmpDpItems(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportTsmpDpItems),
      ReqBody: {},
    } as ReqDPB9923;
    const path = `${this.basePath}/DPB9923`;
    return this.api.excutePostGetFile(path, body);
  }

  importTsmpDpItems(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9924`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  exportTsmpDpMailTplt(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportTsmpDpMailTplt),
      ReqBody: {},
    };
    const path = `${this.basePath}/DPB9925`;
    return this.api.excutePostGetFile(path, body);
  }

  importTsmpDpMailTplt(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9926`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  exportWebsiteProxy(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportWebsiteProxy),
      ReqBody: {},
    };
    const path = `${this.basePath}/DPB9929`;
    return this.api.excutePostGetFile(path, body);
  }

  importWebsiteProxy(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9930`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  exportTsmpFunc(ReqBody: DPB9927Req): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportTsmpFunc),
      ReqBody: ReqBody,
    };
    const path = `${this.basePath}/DPB9927`;
    return this.api.excutePostGetFile(path, body);
  }

  importTsmpFunc(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9928`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  exportTsmpRtnCode(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportTsmpRtnCode),
      ReqBody: {},
    };
    const path = `${this.basePath}/DPB9935`;
    return this.api.excutePostGetFile(path, body);
  }

  importTsmpRtnCode(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9936`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  exportJwe(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportJwe),
      ReqBody: {},
    };
    const path = `${this.basePath}/DPB9933`;
    return this.api.excutePostGetFile(path, body);
  }

  importJwe(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9934`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  exportWebsocketProxy(): Observable<Blob> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportWebsocketProxy),
      ReqBody: {},
    };
    const path = `${this.basePath}/DPB9931`;
    return this.api.excutePostGetFile(path, body);
  }

  importWebsocketProxy(req: any, file: File): Observable<RespDPB9922> {
    const path = `${this.basePath}/DPB9932`;
    return this.api.excuteTsmpSetting<RespDPB9922>(path, file, req);
  }

  queryIdPInfoList_cus(ReqBody?: DPB0220Req): Observable<RespDPB0220> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoList_cus),
      ReqBody: ReqBody ? ReqBody : {},
    } as ReqDPB0220;
    const path = `${this.indexPath}/DPB0220`;
    return this.api.excuteNpPost<RespDPB0220>(path, body);
  }

  createIdPInfo_cus(ReqBody: DPB0222Req): Observable<RespDPB0222> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_cus),
      ReqBody: ReqBody,
    } as ReqDPB0222;
    const path = `${this.indexPath}/DPB0222`;
    return this.api.excuteNpPost<RespDPB0222>(path, body);
  }

  createIdPInfo_cus_before(): Observable<DPB0222RespBefore> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createIdPInfo_cus),
      ReqBody: {},
    } as ReqDPB0222;
    const path = `${this.indexPath}/DPB0222?before`;
    return this.api.excuteNpPost<DPB0222RespBefore>(path, body);
  }

  queryIdPInfoDetail_cus(ReqBody: DPB0221Req): Observable<RespDPB0221> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryIdPInfoDetail_cus),
      ReqBody: ReqBody,
    } as ReqDPB0221;
    const path = `${this.indexPath}/DPB0221`;
    return this.api.excuteNpPost<RespDPB0221>(path, body);
  }

  updateIdPInfo_cus(ReqBody: DPB0223Req): Observable<RespDPB0223> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIdPInfo_cus),
      ReqBody: ReqBody,
    } as ReqDPB0223;
    const path = `${this.indexPath}/DPB0223`;
    return this.api.excuteNpPost<RespDPB0223>(path, body);
  }

  updateIdPInfo_cus_before(): Observable<DPB0223RespBefore> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateIdPInfo_cus),
      ReqBody: {},
    } as ReqDPB0223;
    const path = `${this.indexPath}/DPB0223?before`;
    return this.api.excuteNpPost<DPB0223RespBefore>(path, body);
  }

  deleteIdPInfo_cus(ReqBody: DPB0224Req): Observable<RespDPB0224> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteIdPInfo_cus),
      ReqBody: ReqBody,
    } as ReqDPB0224;
    const path = `${this.indexPath}/DPB0224`;
    return this.api.excuteNpPost<RespDPB0224>(path, body);
  }

  queryGtwIdPInfoByClientId_cus(ReqBody: DPB0240Req): Observable<RespDPB0240> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoByClientId_cus),
      ReqBody: ReqBody,
    } as ReqDPB0240;
    const path = `${this.indexPath}/DPB0240`;
    return this.api.npPost<RespDPB0240>(path, body);
  }

  createGtwIdPInfo_cus_before(): Observable<RespDPB0242Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_cus),
      ReqBody: {},
    } as ReqDPB0242;
    const path = `${this.indexPath}/DPB0242?before`;
    return this.api.npPost<RespDPB0242Before>(path, body);
  }

  createGtwIdPInfo_cus(ReqBody: DPB0242Req): Observable<RespDPB0242> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGtwIdPInfo_cus),
      ReqBody: ReqBody,
    } as ReqDPB0242;
    const path = `${this.indexPath}/DPB0242`;
    return this.api.npPost<RespDPB0242>(path, body);
  }

  queryGtwIdPInfoDetail_cus(ReqBody: DPB0241Req): Observable<RespDPB0241> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGtwIdPInfoDetail_cus),
      ReqBody: ReqBody,
    } as ReqDPB0241;
    const path = `${this.indexPath}/DPB0241`;
    return this.api.npPost<RespDPB0241>(path, body);
  }

  updateGtwIdPInfo_cus(ReqBody: DPB0243Req): Observable<RespDPB0243> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_cus),
      ReqBody: ReqBody,
    } as ReqDPB0243;
    const path = `${this.indexPath}/DPB0243`;
    return this.api.npPost<RespDPB0243>(path, body);
  }

  updateGtwIdPInfo_cus_before(): Observable<RespDPB0243Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGtwIdPInfo_cus),
      ReqBody: {},
    } as ReqDPB0243;
    const path = `${this.indexPath}/DPB0243?before`;
    return this.api.npPost<RespDPB0243Before>(path, body);
  }

  deleteGtwIdPInfo_cus(ReqBody: DPB0244Req): Observable<RespDPB0244> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteGtwIdPInfo_cus),
      ReqBody: ReqBody,
    } as ReqDPB0244;
    const path = `${this.indexPath}/DPB0244`;
    return this.api.npPost<RespDPB0244>(path, body);
  }

  queryApiStatusByGroup(ReqBody: DPB0234Req): Observable<RespDPB0234> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryApiStatusByGroup),
      ReqBody: ReqBody,
    } as ReqDPB0234;
    const path = `${this.indexPath}/DPB0234`;
    return this.api.npPost<RespDPB0234>(path, body);
  }

  queryBotDetectionList(): Observable<RespDPB0232> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryBotDetectionList),
      ReqBody: {},
    } as ReqDPB0232;
    const path = `${this.indexPath}/DPB0232`;
    return this.api.npPost<RespDPB0232>(path, body);
  }

  createAndUpdateBotDetectionList(
    ReqBody: DPB0233Req
  ): Observable<RespDPB0233> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createAndUpdateBotDetectionList),
      ReqBody: ReqBody,
    } as ReqDPB0233;
    const path = `${this.indexPath}/DPB0233`;
    return this.api.npPost<RespDPB0233>(path, body);
  }

  createAndUpdateBotDetectionList_before(): Observable<DPB0233RespBefore> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createAndUpdateBotDetectionList),
      ReqBody: {},
    } as ReqDPB0233;
    const path = `${this.indexPath}/DPB0233?before`;
    return this.api.npPost<DPB0233RespBefore>(path, body);
  }

  testEsConnection(): Observable<RespDPB9938> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.testEsConnection),
      ReqBody: {},
    } as ReqDPB9938;
    const path = `${this.basePath}/DPB9938`;
    return this.api.npPost<RespDPB9938>(path, body);
  }

  testKibanaConnection(): Observable<RespDPB9939> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.testKibanaConnection),
      ReqBody: {},
    } as ReqDPB9939;
    const path = `${this.basePath}/DPB9939`;
    return this.api.npPost<RespDPB9939>(path, body);
  }

  queryRealtimeDashboardData(ReqBody: AA1212Req): Observable<ResAA1212> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getDashboardData),
      ReqBody: ReqBody,
    } as ReqAA1212;
    const path = `${this.indexPath}/AA1212`;
    return this.api.excuteNpPost_ignoreAll<ResAA1212>(path, body);
  }

  queryGrpcService_ignore1298(ReqBody: DPB0294Req): Observable<RespDPB0294> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGrpcService),
      ReqBody: ReqBody,
    } as ReqDPB0294;
    const path = `${this.indexPath}/DPB0294`;
    return this.api.excuteNpPost_ignore1298<RespDPB0294>(path, body);
  }

  queryGrpcService(ReqBody: DPB0294Req): Observable<RespDPB0294> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryGrpcService),
      ReqBody: ReqBody,
    } as ReqDPB0294;
    const path = `${this.indexPath}/DPB0294`;
    return this.api.excuteNpPost<RespDPB0294>(path, body);
  }

  createGrpcService(ReqBody: DPB0290Req): Observable<RespDPB0290> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGrpcService),
      ReqBody: ReqBody,
    } as ReqDPB0290;
    const path = `${this.indexPath}/DPB0290`;
    return this.api.excuteNpPost<RespDPB0290>(path, body);
  }

  createGrpcService_before(): Observable<RespDPB0290Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createGrpcService),
      ReqBody: {},
    } as ReqDPB0290;
    const path = `${this.indexPath}/DPB0290?before`;
    return this.api.excuteNpPost<RespDPB0290Before>(path, body);
  }

  updateGrpcService(ReqBody: DPB0292Req): Observable<RespDPB0292> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGrpcService),
      ReqBody: ReqBody,
    } as ReqDPB0292;
    const path = `${this.indexPath}/DPB0292`;
    return this.api.excuteNpPost<RespDPB0292>(path, body);
  }

  updateGrpcService_before(): Observable<RespDPB0292Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGrpcService),
      ReqBody: {},
    } as ReqDPB0292;
    const path = `${this.indexPath}/DPB0292?before`;
    return this.api.excuteNpPost<RespDPB0292Before>(path, body);
  }

  deleteGrpcService(ReqBody: DPB0293Req): Observable<RespDPB0293> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateGrpcService),
      ReqBody: ReqBody,
    } as ReqDPB0293;
    const path = `${this.indexPath}/DPB0293`;
    return this.api.excuteNpPost<RespDPB0293>(path, body);
  }

  batchUpdateProxyStatus(ReqBody: DPB0291Req): Observable<RespDPB0291> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.batchUpdateProxyStatus),
      ReqBody: ReqBody,
    } as ReqDPB0291;
    const path = `${this.indexPath}/DPB0291`;
    return this.api.excuteNpPost<RespDPB0291>(path, body);
  }
  queryWebhookNotify(ReqBody: DPB0280Req): Observable<RespDPB0280> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0280;
    const path = `${this.indexPath}/DPB0280`;
    return this.api.npPost<RespDPB0280>(path, body);
  }

  queryWebhookNotify_ignore1298(ReqBody: DPB0280Req): Observable<RespDPB0280> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0280;
    const path = `${this.indexPath}/DPB0280`;
    return this.api.excuteNpPost_ignore1298<RespDPB0280>(path, body);
  }

  createWebhookNotify(ReqBody: DPB0281Req): Observable<RespDPB0281> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0281;
    const path = `${this.indexPath}/DPB0281`;
    return this.api.npPost<RespDPB0281>(path, body);
  }

  createWebhookNotify_before(): Observable<RespDPB0281Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createWebhookNotify),
      ReqBody: {},
    } as ReqDPB0281;
    const path = `${this.indexPath}/DPB0281?before`;
    return this.api.npPost<RespDPB0281Before>(path, body);
  }

  getWebhookNotify(ReqBody: DPB0282Req): Observable<RespDPB0282> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0282;
    const path = `${this.indexPath}/DPB0282`;
    return this.api.npPost<RespDPB0282>(path, body);
  }

  updateWebhookNotify(ReqBody: DPB0283Req): Observable<RespDPB0283> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0283;
    const path = `${this.indexPath}/DPB0283`;
    return this.api.npPost<RespDPB0283>(path, body);
  }

  updateWebhookNotify_before(): Observable<RespDPB0283Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateWebhookNotify),
      ReqBody: {},
    } as ReqDPB0283;
    const path = `${this.indexPath}/DPB0283?before`;
    return this.api.npPost<RespDPB0283Before>(path, body);
  }

  deleteWebhookNotify(ReqBody: DPB0284Req): Observable<RespDPB0284> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0284;
    const path = `${this.indexPath}/DPB0284`;
    return this.api.npPost<RespDPB0284>(path, body);
  }

  queryWebhookLogs(ReqBody: DPB0285Req): Observable<RespDPB0285> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWebhookLogs),
      ReqBody: ReqBody,
    } as ReqDPB0285;
    const path = `${this.indexPath}/DPB0285`;
    return this.api.npPost<RespDPB0285>(path, body);
  }

  queryWebhookLogs_ignore1298(ReqBody: DPB0285Req): Observable<RespDPB0285> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryWebhookLogs),
      ReqBody: ReqBody,
    } as ReqDPB0285;
    const path = `${this.indexPath}/DPB0285`;
    return this.api.excuteNpPost_ignore1298<RespDPB0285>(path, body);
  }

  resendWebhookNotify(ReqBody: DPB0286Req): Observable<RespDPB0286> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.resendWebhookNotify),
      ReqBody: ReqBody,
    } as ReqDPB0286;
    const path = `${this.indexPath}/DPB0286`;
    return this.api.npPost<RespDPB0286>(path, body);
  }

}
