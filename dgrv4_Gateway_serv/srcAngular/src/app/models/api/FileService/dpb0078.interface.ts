import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0078 extends BaseReq {
    ReqBody: DPB0078Req;
}
export interface DPB0078Req {
    filePath: string;
}

export interface ResDPB0078 extends BaseRes { }