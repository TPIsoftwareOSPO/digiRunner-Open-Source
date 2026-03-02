import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPF0021 extends BaseReq {
    ReqBody: DPF0021Req;
}
export interface DPF0021Req {
    filePath: string;
}