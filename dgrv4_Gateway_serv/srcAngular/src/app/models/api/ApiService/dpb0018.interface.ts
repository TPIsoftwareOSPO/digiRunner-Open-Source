import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0018 extends BaseReq {
    ReqBody: DPB0018Req;
}
export interface DPB0018Req {
    apiKey?: string;
    moduleName?: string;
    keyword?: string;
}

export interface ResDPB0018 extends BaseRes {
    RespBody: RespBody;
}
export interface RespBody { 
    apiList: Array<DPB0018Api>
}
export interface DPB0018Api {
    apiKey: string;
    moduleName: string;
    apiName: string;
    apiStatus: string;
    apiSrc: string;
    apiDesc: string;
    apiUid: string;
}