import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0003 extends BaseReq {
    ReqBody: CUS0003Req;
}
export interface CUS0003Req {
    tspid: string;
}

export interface ResCUS0003 extends BaseRes {
    RespBody: CUS0003Resp;
}
export interface CUS0003Resp {
    apptJobId?: number;
    showUI?: CUS0003ShowUI;
}
export interface CUS0003ShowUI {
    cgRespBody: CUS0003CgRespBody;
}
export interface CUS0003CgRespBody {
    code: string;
    message: string;
    data?: CUS0003CgRespBodyItem[];
}
export interface CUS0003CgRespBodyItem{
    tspid: string;
}
