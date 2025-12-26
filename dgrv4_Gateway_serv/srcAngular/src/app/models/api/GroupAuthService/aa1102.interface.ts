import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1102 extends BaseReq {
    ReqBody: Req_1102;
}
export interface Req_1102 {
    // SECURITY_LEVEL_ID?: string;
    // SECURITY_LEVEL_NAME?: string;
}

export interface ResAA1102 extends BaseRes {
    RespBody: Res_1102;
}
export interface Res_1102 {
    // listCount: number;
    securityLevelList?: Array<TTSecurityLV>;
}
export interface TTSecurityLV {
    securityLevelId: string;
    securityLevelName: string;
    securityLevelDesc: string;
}