import { BaseReq, BaseRes } from '../base.interface';
import { CUS0003ShowUI } from './cus0003.invoke.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';
import { CUS0004CgRespBody } from './cus0004.invoke.interface';

export interface ReqCUS0004 extends BaseReq {
    ReqBody: CUS0004Req;
}
export interface CUS0004Req {
    apptJobId: number;
}

export interface ResCUS0004 extends BaseRes {
    RespBody: CUS0004Resp;
}
export interface CUS0004Resp {
    result: CUS0004Result;
}
export interface CUS0004Result {
    commLoopStatus: CusCommLoopStatus;
    showUI: CUS0004ShowUI;
}
export interface CUS0004ShowUI{
    cgRespBody: CUS0004CgRespBody;
}
