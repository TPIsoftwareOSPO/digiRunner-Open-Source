import { Injectable } from '@angular/core';
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import {
  ReqDPB0250,
  RespDPB0250,
} from 'src/app/models/api/ServerService/dpb0250.interface';
import { Observable } from 'rxjs';
import { TxID } from 'src/app/models/common.enum';
import {
  DPB0251Req,
  ReqDPB0251,
  RespDPB0251,
  RespDPB0251Before,
} from 'src/app/models/api/ServerService/dpb0251.interface';
import { RespDPB0254 } from 'src/app/models/api/ServerService/dpb0254.interface';
import {
  ReqDPB0252,
  RespDPB0252,
} from 'src/app/models/api/ServerService/dpb0252.interface';
import {
  DPB0253Req,
  ReqDPB0253,
  RespDPB0253,
} from 'src/app/models/api/ServerService/dpb0253.interface';
import {
  DPB0255Req,
  ReqDPB0255,
  RespDPB0255,
} from 'src/app/models/api/ServerService/dpb0255.interface';
import {
  DPB0256Req,
  ReqDPB0256,
  RespDPB0256,
} from 'src/app/models/api/ServerService/dpb0256.interface';
import {
  DPB0257Req,
  ReqDPB0257,
  RespDPB0257,
} from 'src/app/models/api/ServerService/dpb0257.interface';
import { RespDPB0258 } from 'src/app/models/api/ServerService/dpb0258.interface';
import {
  ReqDPB0259,
  RespDPB0259,
} from 'src/app/models/api/ServerService/dpb0259.interface';
import {
  DPB0260Req,
  ReqDPB0260,
  RespDPB0260,
} from 'src/app/models/api/ServerService/dpb0260.interface';
import {
  ReqDPB02601,
  RespDPB02601,
} from 'src/app/models/api/ServerService/dpb02601.interface';
import {
  DPB0262Req,
  ReqDPB0262,
  RespDPB0262,
} from 'src/app/models/api/ServerService/dpb0262.interface';
import {
  DPB0263Req,
  ReqDPB0263,
  RespDPB0263,
} from 'src/app/models/api/ServerService/dpb0263.interface';
import {
  ReqDPB0264,
  RespDPB0264,
} from 'src/app/models/api/ServerService/dpb0264.interface';
import {
  DPB0265Req,
  ReqDPB0265,
  RespDPB0265,
} from 'src/app/models/api/ServerService/dpb0265.interface';
import {
  ReqDPB0266,
  RespDPB0266,
} from 'src/app/models/api/ServerService/dpb0266.interface';
import {
  DPB02671Req,
  ReqDPB02671,
  RespDPB02671,
} from 'src/app/models/api/ServerService/dpb02671.interface';
import {
  DPB0267Req,
  ReqDPB0267,
  RespDPB0267,
} from 'src/app/models/api/ServerService/dpb0267.interface';
import { ReqDPB0268, RespDPB0268 } from 'src/app/models/api/ServerService/dpb0268.interface';

@Injectable({
  providedIn: 'root',
})
export class AiService {
  public get basePath(): string {
    return 'dgrv4/11';
  }

  constructor(private api: ApiBaseService) {
    this.api.baseUrl = environment.apiUrl;
  }

  listAIProvider(): Observable<RespDPB0250> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIProvider),
      ReqBody: {},
    } as ReqDPB0250;
    const path = `${this.basePath}/DPB0250`;
    return this.api.excuteNpPost<RespDPB0250>(path, body);
  }

  listAIProvider_ignore1298(): Observable<RespDPB0250> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIProvider),
      ReqBody: {},
    } as ReqDPB0250;
    const path = `${this.basePath}/DPB0250`;
    return this.api.excuteNpPost_ignore1298<RespDPB0250>(path, body);
  }

  createAIProvider(ReqBody: DPB0251Req): Observable<RespDPB0251> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createAIProvider),
      ReqBody: ReqBody,
    } as ReqDPB0251;
    const path = `${this.basePath}/DPB0251`;
    return this.api.excuteNpPost<RespDPB0251>(path, body);
  }

  createAIProvider_before(): Observable<RespDPB0251Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createAIProvider),
      ReqBody: {},
    } as ReqDPB0251;
    const path = `${this.basePath}/DPB0251?before`;
    return this.api.excuteNpPost<RespDPB0251Before>(path, body);
  }

  getAIProvider(id: string): Observable<RespDPB0252> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getAIProvider),
      ReqBody: {},
    } as ReqDPB0252;
    const path = `${this.basePath}/DPB0252/${id}`;
    return this.api.excuteNpPost<RespDPB0252>(path, body);
  }

  updateAIProvider(id: string, ReqBody: DPB0253Req): Observable<RespDPB0253> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateAIProvider),
      ReqBody: ReqBody,
    } as ReqDPB0253;
    const path = `${this.basePath}/DPB0253/${id}`;
    return this.api.excuteNpPost<RespDPB0253>(path, body);
  }

  deleteAIProvider(id: string): Observable<RespDPB0254> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.createAIProvider),
      ReqBody: {},
    } as ReqDPB0251;
    const path = `${this.basePath}/DPB0254/${id}`;
    return this.api.excuteNpPost<RespDPB0254>(path, body);
  }

  listAIAPIKEY(ReqBody: DPB0255Req): Observable<RespDPB0255> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIAPIKEY),
      ReqBody: ReqBody,
    } as ReqDPB0255;
    const path = `${this.basePath}/DPB0255`;
    return this.api.excuteNpPost<RespDPB0255>(path, body);
  }

  listAIAPIKEY_ignore1298(ReqBody: DPB0255Req): Observable<RespDPB0255> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIAPIKEY),
      ReqBody: ReqBody,
    } as ReqDPB0255;
    const path = `${this.basePath}/DPB0255`;
    return this.api.excuteNpPost_ignore1298<RespDPB0255>(path, body);
  }

  registerAIAPIKEY(ReqBody: DPB0256Req): Observable<RespDPB0256> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.registerAIAPIKEY),
      ReqBody: ReqBody,
    } as ReqDPB0256;
    const path = `${this.basePath}/DPB0256`;
    return this.api.excuteNpPost_ignore1298<RespDPB0256>(path, body);
  }

  getAIAPIKEY(id: string): Observable<RespDPB0257> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getAIAPIKEY),
      ReqBody: {},
    } as ReqDPB0257;
    const path = `${this.basePath}/DPB0257/${id}`;
    return this.api.excuteNpPost<RespDPB0257>(path, body);
  }

  updateAIAPIKEY(id: string, ReqBody: DPB0257Req): Observable<RespDPB0258> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateAIAPIKEY),
      ReqBody: ReqBody,
    } as ReqDPB0257;
    const path = `${this.basePath}/DPB0258/${id}`;
    return this.api.excuteNpPost<RespDPB0258>(path, body);
  }

  deleteAIAPIKEY(id: string): Observable<RespDPB0259> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteAIAPIKEY),
      ReqBody: {},
    } as ReqDPB0259;
    const path = `${this.basePath}/DPB0259/${id}`;
    return this.api.excuteNpPost<RespDPB0259>(path, body);
  }

  listAIUsage(ReqBody: DPB0260Req): Observable<RespDPB0260> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIUsage),
      ReqBody: ReqBody,
    } as ReqDPB0260;
    const path = `${this.basePath}/DPB0260`;
    return this.api.excuteNpPost<RespDPB0260>(path, body);
  }

  listAIUsage_ignore1298(ReqBody: DPB0260Req): Observable<RespDPB0260> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIUsage),
      ReqBody: ReqBody,
    } as ReqDPB0260;
    const path = `${this.basePath}/DPB0260`;
    return this.api.excuteNpPost_ignore1298<RespDPB0260>(path, body);
  }

  getAIUsageDetail(id: string): Observable<RespDPB02601> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getAIUsageDetail),
      ReqBody: {},
    } as ReqDPB02601;
    const path = `${this.basePath}/DPB02601/${id}`;
    return this.api.excuteNpPost<RespDPB02601>(path, body);
  }

  listAIPromptTemplate(ReqBody: DPB0262Req): Observable<RespDPB0262> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIPromptTemplate),
      ReqBody: ReqBody,
    } as ReqDPB0262;
    const path = `${this.basePath}/DPB0262`;
    return this.api.excuteNpPost<RespDPB0262>(path, body);
  }

  listAIPromptTemplate_ignore1298(
    ReqBody: DPB0262Req
  ): Observable<RespDPB0262> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIPromptTemplate),
      ReqBody: ReqBody,
    } as ReqDPB0262;
    const path = `${this.basePath}/DPB0262`;
    return this.api.excuteNpPost_ignore1298<RespDPB0262>(path, body);
  }

  createAIPromptTemplate(ReqBody: DPB0263Req): Observable<RespDPB0263> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.listAIPromptTemplate),
      ReqBody: ReqBody,
    } as ReqDPB0263;
    const path = `${this.basePath}/DPB0263`;
    return this.api.excuteNpPost<RespDPB0263>(path, body);
  }

  getAIPromptTemplate(id: string): Observable<RespDPB0264> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.getAIPromptTemplate),
      ReqBody: {},
    } as ReqDPB0264;
    const path = `${this.basePath}/DPB0264/${id}`;
    return this.api.excuteNpPost<RespDPB0264>(path, body);
  }

  updateAIPromptTemplate(
    id: string,
    ReqBody: DPB0265Req
  ): Observable<RespDPB0265> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateAIPromptTemplate),
      ReqBody: ReqBody,
    } as ReqDPB0265;
    const path = `${this.basePath}/DPB0265/${id}`;
    return this.api.excuteNpPost<RespDPB0265>(path, body);
  }

  deleteAIPromptTemplate(id: string): Observable<RespDPB0266> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteAIPromptTemplate),
      ReqBody: {},
    } as ReqDPB0266;
    const path = `${this.basePath}/DPB0266/${id}`;
    return this.api.excuteNpPost<RespDPB0266>(path, body);
  }

  listConsumerAIPromptTemplateSetting(
    ReqBody: DPB02671Req
  ): Observable<RespDPB02671> {
    let body = {
      ReqHeader: this.api.getReqHeader(
        TxID.listConsumerAIPromptTemplateSetting
      ),
      ReqBody: ReqBody,
    } as ReqDPB02671;
    const path = `${this.basePath}/DPB02671`;
    return this.api.excuteNpPost<RespDPB02671>(path, body);
  }

  updateConsumerAIPromptTemplateSetting(
    ReqBody: DPB0267Req
  ): Observable<RespDPB0267> {
    let body = {
      ReqHeader: this.api.getReqHeader(
        TxID.updateConsumerAIPromptTemplateSetting
      ),
      ReqBody: ReqBody,
    } as ReqDPB0267;
    const path = `${this.basePath}/DPB0267`;
    return this.api.excuteNpPost<RespDPB0267>(path, body);
  }

  deleteConsumerAIPromptTemplateSetting(id: string): Observable<RespDPB0268> {
    let body = {
      ReqHeader: this.api.getReqHeader(
        TxID.deleteConsumerAIPromptTemplateSetting
      ),
      ReqBody: {},
    } as ReqDPB0268;
    const path = `${this.basePath}/DPB0268/${id}`;
    return this.api.excuteNpPost<RespDPB0268>(path, body);
  }
}
