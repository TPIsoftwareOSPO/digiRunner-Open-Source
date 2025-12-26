import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0414 extends BaseDotNetReq {
    Req_0414: Req_0414;
}
export interface Req_0414 {
    siteId: number;
    siteCode: string;
    newStatus: string; //0:Inactive 1:Active
}

export interface ResNAA0414 extends BaseDotNetRes { }