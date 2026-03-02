import { BaseReq, BaseRes } from '../base.interface';
import { CUS0003ShowUI } from './cus0003.invoke.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';

export interface ReqCUS0003 extends BaseReq {
    ReqBody: CUS0003Req;
}
export interface CUS0003Req {
    apptJobId: number;
}

export interface ResCUS0003 extends BaseRes {
    RespBody: CUS0003Resp;
}
export interface CUS0003Resp {
    result: CUS0003Result;
}
export interface CUS0003Result {
    commLoopStatus: CusCommLoopStatus;
    showUI: CUS0003ShowUI;
}
