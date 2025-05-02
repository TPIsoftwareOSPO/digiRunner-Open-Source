import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0079 extends BaseReq {
    ReqBody: DPB0079Req;
}
export interface DPB0079Req {
    filePath: string;
}

export interface ResDPB0079 extends BaseRes { }