import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0231 extends BaseReq {
    ReqBody: AA0231Req;
}
export interface AA0231Req {
    clientID: string;
    resetBlock: string;
    clientBlock?: string;
    newClientBlock?: string;
    confirmNewClientBlock?: string;
}

export interface ResAA0231Before extends BaseRes {
    RespBody: AA0231RespBefore;
}
export interface AA0231RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0231 extends BaseRes { }