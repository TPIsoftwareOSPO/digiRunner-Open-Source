import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0415 extends BaseDotNetReq {
    Req_0415: Req_0415;
}
export interface Req_0415 {
    siteId: number;
    siteCode: string;
}

export interface ResNAA0415 extends BaseDotNetRes { }