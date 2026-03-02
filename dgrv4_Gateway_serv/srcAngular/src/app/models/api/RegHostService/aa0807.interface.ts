import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0807 extends BaseReq {
    ReqBody: AA0807Req
}
export interface AA0807Req {
    regHostID: string;
}

export interface ResAA0807 extends BaseRes {
    RespBody: AA0807Resp;
}
export interface AA0807Resp {
    regHost: string;
}