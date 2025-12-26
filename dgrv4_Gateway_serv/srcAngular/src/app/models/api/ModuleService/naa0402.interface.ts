import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0402 extends BaseDotNetReq {
    Req_0402: Req_n0402;
}
export interface Req_n0402 {
    moduleName?: string;
    siteId?: number;
}

export interface ResNAA0402 extends BaseDotNetRes {
    res_0402: Res_n0402;
}
export interface Res_n0402 {
    listCount: number;
    moduleList?: Array<ModuleKey>
}
export interface ModuleKey {
    moduleId: number;
    moduleName: string;
    siteId: number;
    siteCode: string;
    status: string;
    version?: string;
    targetVersion?: string;
    clrVersion: string;
    orgName?: string;
}