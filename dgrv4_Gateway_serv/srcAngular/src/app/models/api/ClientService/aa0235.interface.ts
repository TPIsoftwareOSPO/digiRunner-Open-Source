import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0235: 查詢群組所擁有的Module
 */
export interface ReqAA0235 extends BaseReq {
    ReqBody: Req_0235;
}
export interface Req_0235 {
    groupId: string;
}

export interface ResAA0235 extends BaseRes {
    RespBody: Res_0235;
}
export interface Res_0235 {
    moduleList: Array<Object>;
}