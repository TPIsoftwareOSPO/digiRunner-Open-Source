import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0801 extends BaseReq {
    ReqBody: AA0801Req
}
export interface AA0801Req {
    regHost: string;
    enabled: string;
    clientID: string;
    memo: string;
}

export interface ResAA0801Before extends BaseRes {
    RespBody: AA0801RespBefore;
}
export interface AA0801RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0801 extends BaseRes {
    RespBody: AA0801Resp;
}
export interface AA0801Resp {
    regHostID: string;
}