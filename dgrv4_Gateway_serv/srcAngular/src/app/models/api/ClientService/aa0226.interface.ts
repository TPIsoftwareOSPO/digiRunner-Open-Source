import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0226 extends BaseReq {
    ReqBody: AA0226Req;
}
export interface AA0226Req {
    clientID: string;
    vgroupIDList: Array<string>;
}

export interface ResAA0226 extends BaseRes { }
