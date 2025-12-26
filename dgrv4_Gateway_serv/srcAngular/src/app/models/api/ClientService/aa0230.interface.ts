import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0230 extends BaseReq {
    ReqBody: AA0230Req;
}
export interface AA0230Req {
    vgroupID: string;
    clientID: string;
}

export interface ResAA0230 extends BaseRes { }