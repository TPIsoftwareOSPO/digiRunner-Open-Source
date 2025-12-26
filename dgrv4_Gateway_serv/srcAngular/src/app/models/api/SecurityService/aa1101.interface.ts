import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1101 extends BaseReq {
    ReqBody: AA1101Req;
}
export interface AA1101Req {
    securityLevelId: string;
    securityLevelName: string;
    securityLevelDesc?: string;
}

export interface ResAA1101Before extends BaseRes {
    RespBody: AA1101RespBefore;
}
export interface AA1101RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1101 extends BaseRes {
    RespBody: AA1101Resp;
}
export interface AA1101Resp {
    securityLevelId: string;
}