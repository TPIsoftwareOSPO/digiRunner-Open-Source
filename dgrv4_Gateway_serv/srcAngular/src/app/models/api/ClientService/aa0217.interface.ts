import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0217 extends BaseReq {
    ReqBody: AA0217Req;
}
export interface AA0217Req {
    clientID: string;
    clientName: string;
    securityID: string;
    newSecurityID: string;
}

export interface ResAA0217Before extends BaseRes {
    RespBody: AA0217RespBefore;
}
export interface AA0217RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0217 extends BaseRes { }