import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0705 extends BaseReq {
    ReqBody: AA0705Req;
}
export interface AA0705Req {
    alertID: string;
    alertName: string;
}

export interface ResAA0705 extends BaseRes { }