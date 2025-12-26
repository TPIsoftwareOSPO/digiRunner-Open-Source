import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0124 extends BaseReq {
    ReqBody: DPB0124Req;
}
export interface DPB0124Req {
    indexList: Array<String>;
    isOpen: String;
}

export interface RespDPB0124 extends BaseRes {
    RespBody: DPB0124Res;
}
export interface DPB0124Res { }