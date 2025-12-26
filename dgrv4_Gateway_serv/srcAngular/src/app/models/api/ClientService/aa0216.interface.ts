import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0216 extends BaseReq {
    ReqBody: AA0216Req;
}
export interface AA0216Req {
    clientID: string;
    groupIDList: Array<string>;
}

export interface ResAA0216 extends BaseRes { }