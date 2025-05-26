import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0005 extends BaseReq {
    ReqBody: CUS0005Req;
}
export interface CUS0005Req {
    tspid: string;
    fileContent:string;
}

export interface ResCUS0005 extends BaseRes {
    RespBody: CUS0005Resp;
}
export interface CUS0005Resp {
    apptJobId?: number;
    showUI?: CUS0005ShowUI;
}
export interface CUS0005ShowUI {
    cgRespBody: CUS0005CgRespBody;
}
export interface CUS0005CgRespBody  {
    code: string;
    message: string;
    data?: string;
}
 