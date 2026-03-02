import { BaseDotNetRes, BaseDotNetReq } from '../baseDotNet.interface';

export interface ReqNAA0413 extends BaseDotNetReq {
    Req_0413: Req_0413;
}
export interface Req_0413 {
    siteId: number;
    siteCode: string;
    siteMemo?: string;
    protocolType?: string;
    bindingIp?: string;
    bindingPort?: number;
    clrVersion?: string;
}

export interface ResNAA0413 extends BaseDotNetRes { }
