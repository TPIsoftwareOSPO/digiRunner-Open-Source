import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0058 extends BaseReq {
    ReqBody: DPB0058Req;
}
export interface DPB0058Req {
    apptJobId?: number;
    keyword?: string;
    startDate: string; // YYYY/MM/DD
    endDate: string; // YYYY/MM/DD
    status?: string;
}

export interface ResDPB0058 extends BaseRes {
    RespBody: DPB0058Resp;
}
export interface DPB0058Resp {
    dataList: Array<DPB0058RespItem>;
}
export interface DPB0058RespItem {
    apptJobId: number;
    refItemNo: string;
    refSubitemNo: string;
    status: string;
    startDateTime: string;
    jobStep?: string;
    execResult?: string;
    createDateTime: string;
    createUser: string;
    updateDateTime?: string;
    updateUser?: string;
    canExec: string;
    identifData: string;
}