import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0102 extends BaseReq {
    ReqBody: DPB0102Req;
}
export interface DPB0102Req {
    apptRjobId?: string;
    status?: string;
    keyword?: string;
}

export interface RespDPB0102 extends BaseRes {
    RespBody: DPB0102Resp;
}
export interface DPB0102Resp {
    dataList: Array<DPB0102Items>;
}
export interface DPB0102Items {
    apptRjobId: string;
    lv: number;
    rjobName: string;
    statusName: string;
    cronDesc?: string;
    nextDateTime: string;
    effPeriod?: string;
    remark?: string;
    detailFlag: boolean;
    updateFlag: boolean;
    pauseVisible: boolean;
    pauseFlag: boolean;
    activeVisible: boolean;
    activeFlag: boolean;
    skipFlag: boolean;
    inactiveFlag: boolean;
    historyFlag: boolean;
} 