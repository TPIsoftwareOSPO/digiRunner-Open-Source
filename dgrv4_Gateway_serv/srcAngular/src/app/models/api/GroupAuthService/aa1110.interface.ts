import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1110 extends BaseReq {
    ReqBody: AA1110Req;
}
export interface AA1110Req {
    groupAuthoritieId: string;
    groupAuthoritieName: string;
}

export interface ResAA1110 extends BaseRes { }