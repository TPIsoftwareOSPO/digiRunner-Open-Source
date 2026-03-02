import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9914 extends BaseReq {
    ReqBody: DPB9914Req;
}

export interface DPB9914Req {
    settingNo: string;    
    subsettingNo: string;
}

export interface RespDPB9914 extends BaseRes {
    RespBody: DPB9914Resp;
}

export interface DPB9914Resp { }
