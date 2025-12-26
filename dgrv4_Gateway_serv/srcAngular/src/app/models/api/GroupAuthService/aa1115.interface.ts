import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1115 extends BaseReq {
    ReqBody: AA1115Req;
}
export interface AA1115Req {
    lastGroupAuthoritieId?: string;
    keyword?: string;
    selectedGroupAuthoritieIdList?: Array<string>;
}

export interface ResAA1115 extends BaseRes {
    RespBody: AA1115Resp;
}
export interface AA1115Resp {
    groupAuthoritiesList: Array<AA1115GroupAuthorities>;
}
export interface AA1115GroupAuthorities {
    groupAuthoritiesId: string;
    groupAuthoritiesName: string;
    groupAuthoritiesDesc: String;
    groupAuthoritiesLevel: String;
}