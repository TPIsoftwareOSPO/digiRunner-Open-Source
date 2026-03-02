import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0404 extends BaseDotNetReq {
    Req_0404: Req_n0404;
}
export interface Req_n0404 {
    moduleId: number;
    moduleName: string;
    force: string; // Char(1)
    siteId?: number;
}

export interface ResNAA0404 extends BaseDotNetRes { }