import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0007 extends BaseReq {
    ReqBody: CUS0007Req;
}
export interface CUS0007Req {
    clientCertId: string;
}

export interface ResCUS0007 extends BaseRes {
    RespBody: CUS0007Resp;
}
export interface CUS0007Resp {
    apptJobId?: number;
    showUI?: CUS0007ShowUI;
}
export interface CUS0007ShowUI {
    cgRespBody: CUS0007CgRespBody;
}
export interface CUS0007CgRespBody  {
    code: string;
    message: string;
    data: string;
}
 