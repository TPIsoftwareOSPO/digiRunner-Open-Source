import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0017 extends BaseReq {
    ReqBody: AA0017Req;
}
export interface AA0017Req {
    roleName: string;
}

export interface ResAA0017 extends BaseRes { }