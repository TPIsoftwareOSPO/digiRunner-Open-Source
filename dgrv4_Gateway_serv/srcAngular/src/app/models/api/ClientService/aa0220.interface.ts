import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0220 extends BaseReq {
    ReqBody: AA0220Req;
}
export interface AA0220Req {
    clientID: string;
    encodeStatus: string;
    resetPwdFailTimes: string;
    resetFailLoginTreshhold: number;
}

export interface ResAA0220Before extends BaseRes {
    RespBody: AA0220RespBofore;
}
export interface AA0220RespBofore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0220 extends BaseRes { }