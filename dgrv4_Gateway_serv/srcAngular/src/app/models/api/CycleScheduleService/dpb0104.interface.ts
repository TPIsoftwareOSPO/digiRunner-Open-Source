import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0104 extends BaseReq {
    ReqBody: DPB0104Req;
}
export interface DPB0104Req {
    apptJobId?: number;
    apptRjobId: string;
}

export interface RespDPB0104 extends BaseRes {
    RespBody: DPB0104Resp;
}
export interface DPB0104Resp {
    apptRjobId: string;
    rjobName: string;
    remark?: string;
    historyList: Array<DPB0104Items>;
}
export interface DPB0104Items {
    apptJobId: number;
    refItemNo: string;
    refItemName: string;
    refSubitemNo?: string;
    refSubitemName?: string;
    sortBy: string;
    periodNexttime: string;
    updateDateTime: string;
    status: string;
    statusName: string;
    execResult?: string;
} 