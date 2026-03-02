import { BaseReq, BaseRes } from '../base.interface';
import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0403 extends BaseDotNetReq {
    Req_0403: Req_n0403;
}
export interface Req_n0403 {
    moduleId: number;
    moduleName: string;
    siteId?: number;
}

export interface ResNAA0403 extends BaseDotNetRes {
    res_0403: Res_n0403;
}
export interface Res_n0403 {
    moduleId: number;
    moduleName: string;
    moduleApp: string;
    version?: string;
    desc?: string;
    siteId: number;
    siteCode: string
    status: string;
    statusDate?: string;
    statusUser?: string;
    uploadDate: string;
    uploader: string;
    apiList?: Array<string>;
    clrVersion: string;
    targetVersion?: string;
    orgName?: string;
}