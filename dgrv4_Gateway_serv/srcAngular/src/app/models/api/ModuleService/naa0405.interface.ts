import { BaseDotNetReq, BaseDotNetRes } from '../baseDotNet.interface';

export interface ReqNAA0405 extends BaseDotNetReq {
    Req_0405: Req_n0405;
}
export interface Req_n0405 {
    moduleId: number;
    moduleName: string;
}

export interface ResNAA0405 extends BaseDotNetRes { }