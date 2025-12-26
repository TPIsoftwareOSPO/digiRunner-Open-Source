import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0006 extends BaseReq {
    ReqBody: CUS0006Req;
}
export interface CUS0006Req {
    clientCertId: string;
}

export interface ResCUS0006 extends BaseRes {
    RespBody: CUS0006Resp;
}
export interface CUS0006Resp {
    apptJobId?: number;
    showUI?: CUS0006ShowUI;
}
export interface CUS0006ShowUI {
    cgRespBody: CUS0006CgRespBody;
}
export interface CUS0006CgRespBody  {
    code: string;
    message: string;
}
 