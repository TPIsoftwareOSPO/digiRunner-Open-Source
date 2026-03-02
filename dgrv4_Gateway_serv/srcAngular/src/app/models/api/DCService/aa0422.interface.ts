import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0422 extends BaseReq {
    ReqBody: AA0422Req;
}
export interface AA0422Req {
    moduleName: string;
}

export interface ResAA0422 extends BaseRes {
    RespBody: AA0422Resp;
}
export interface AA0422Resp {
    dcList: Array<AA0422Dc>;
}
export interface AA0422Dc {
    dcID: number;
    active: boolean;
    dcCode: string;
}