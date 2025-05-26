import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';
import { CUS0005CgRespBody } from './cus0005.invoke.interface';
import { CUS0006CgRespBody } from './cus0006.invoke.interface';
import { CUS0007CgRespBody } from './cus0007.invoke.interface';

export interface ReqCUS0007 extends BaseReq {
    ReqBody: CUS0007Req;
}
export interface CUS0007Req {
    apptJobId: number;
}

export interface ResCUS0007 extends BaseRes {
    RespBody: CUS0007Resp;
}
export interface CUS0007Resp {
    result: CUS0007Result;
}
export interface CUS0007Result {
    commLoopStatus: CusCommLoopStatus;
    showUI: CUS0007ShowUI;
}
export interface CUS0007ShowUI{
    cgRespBody: CUS0007CgRespBody;
}
