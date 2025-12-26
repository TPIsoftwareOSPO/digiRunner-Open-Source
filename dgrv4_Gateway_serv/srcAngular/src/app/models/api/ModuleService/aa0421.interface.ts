import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0421 extends BaseReq {
    ReqBody: AA0421Req;
}
export interface AA0421Req {
    id?: number;
    apiModuleId: number;
    moduleName: string;
    keyword?: string;
}

export interface ResAA0421 extends BaseRes {
    RespBody: AA0421Resp;
}
export interface AA0421Resp {
    apiModuleId: number;
    moduleName: string;
    dataList: Array<AA0421Item>;
}
export interface AA0421Item {
    id: number;
    apiId: string;
    apiName: string;
}