import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0227 extends BaseReq {
    ReqBody: AA0227Req;
}
export interface AA0227Req {
    groupID: string;
    clientID: string;
}

export interface ResAA0227 extends BaseRes { }