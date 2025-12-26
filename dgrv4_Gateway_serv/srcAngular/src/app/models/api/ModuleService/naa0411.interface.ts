import { BaseDotNetRes, BaseDotNetReq } from '../baseDotNet.interface';

export interface ReqNAA0411 extends BaseDotNetReq {
    Req_0411: Req_0411;
}
export interface Req_0411 {
    siteCode: string;
    clrVersion?: string;
    siteMemo: string;
}

export interface ResNAA0411 extends BaseDotNetRes {
    res_0411: Res_0411;
}
export interface Res_0411 {
    siteId: number;
}