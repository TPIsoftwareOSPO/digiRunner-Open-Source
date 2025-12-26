import { BaseReq, BaseRes } from '../base.interface';


export interface ReqAA0306 extends BaseReq {
    ReqBody: AA0306Req;
}
export interface AA0306Req {
    apiList: Array<AA0306Item>;
    revokeFlag:string;
}
export interface AA0306Item {
    apiKey: string;
    moduleName: string;
}

export interface ResAA0306 extends BaseRes {
    RespBody: AA0306Resp;
}
export interface AA0306Resp {
  apiList: Array<AA0306Item>;
}
