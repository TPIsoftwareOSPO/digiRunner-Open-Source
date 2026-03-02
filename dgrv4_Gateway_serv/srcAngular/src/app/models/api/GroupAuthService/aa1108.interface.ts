import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1108 extends BaseReq {
    ReqBody: AA1108Req;
}
export interface AA1108Req {
    groupAuthoritieId: string;
    oriGroupAuthoritieName: string;
    newGroupAuthoritieName: string;
    newGroupAuthoritieDesc: string;
    newGroupAuthoritieLevel: string;
}

export interface ResAA1108Before extends BaseRes {
    RespBody: AA1108ResBefore;
}
export interface AA1108ResBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1108 extends BaseRes { }