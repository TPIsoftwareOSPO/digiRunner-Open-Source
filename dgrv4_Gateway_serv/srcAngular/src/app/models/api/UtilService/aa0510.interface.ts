import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0510 extends BaseReq { }

export interface ResAA0510 extends BaseRes {
    RespBody: AA0510Resp;
}
export interface AA0510Resp {
    coreVer: string;
    dcPrefix: string;
    dp: number;
    edition: string;
    apiLogWriteRDB: string;
    net: string;
    logoutUrl?:string;
}
