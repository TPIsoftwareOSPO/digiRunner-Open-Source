import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1106 extends BaseReq {
    ReqBody: AA1106Req;
}
export interface AA1106Req {
    groupAuthoritieId: string;
    groupAuthoritieName: string;
    groupAuthoritieDesc: string;
    groupAuthoritieLevel: string;
}

export interface ResAA1106Before extends BaseRes {
    RespBody: AA1106ResBefore;
}
export interface AA1106ResBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1106 extends BaseRes {
    RespBody: AA1106Resp;
}
export interface AA1106Resp {
    groupAuthoritieName: string;
}