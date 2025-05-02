import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0412 extends BaseDotNetReq {
    Req_0412: Req_0412;
}
export interface Req_0412 {
    moduleName?: string;
    siteId?: number;
    detailFlag: boolean;
}

export interface ResNAA0412 extends BaseDotNetRes {
    res_0412: Res_0412;
}
export interface Res_0412 {
    listCount: number;
    siteInfoList: Array<SiteInfo>;
}
export interface SiteInfo {
    siteId: number;
    siteCode: string;
    siteMemo?: string;
    status: string; // Bit
    protocolType: string;
    bindingIp: string;
    bindingPort: number;
    clrVersion: string;
    moduleID?: number;
    moduleName?: string;
    targetVersion?: string;
    moduleVersion?: string;
}