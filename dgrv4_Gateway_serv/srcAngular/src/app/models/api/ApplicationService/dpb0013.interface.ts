import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0013 extends BaseReq {
    ReqBody: DPB0013Req;
}
export interface DPB0013Req {
    appId: number;
    keyword?: string;
    dataStatus: string;
}

export interface ResDPB0013 extends BaseRes {
    RespBody: DPB0013Resp;
}
export interface DPB0013Resp {
    appList: Array<DPB0013App>;
}
export interface DPB0013App {
    appId: number;
    refAppCateId: number;
    name: string;
    dataStatus: string;
    appCateName: string;
}