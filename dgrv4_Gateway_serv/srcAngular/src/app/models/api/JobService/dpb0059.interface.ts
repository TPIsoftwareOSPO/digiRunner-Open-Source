import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0059 extends BaseReq {
    ReqBody: DPB0059Req;
}
export interface DPB0059Req {
    apptJobId: number;
}

export interface ResDPB0059 extends BaseRes {
    RespBody: DPB0059Resp;
}
export interface DPB0059Resp {
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
}