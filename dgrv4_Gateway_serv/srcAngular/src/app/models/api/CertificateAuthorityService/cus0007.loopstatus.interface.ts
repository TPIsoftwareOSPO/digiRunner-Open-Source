import { EmApptJobStatus } from '../../common.enum';
import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';

export interface ReqCUS0007 extends BaseRes {
    ReqBody: CUS0007Req;
}
export interface CUS0007Req {
    apptJobId: number;
}

export interface ResCUS0007 extends BaseRes {
    RespBody: CUS0007Resp;
}
export interface CUS0007Resp {
    cusCommLoopStatus: CusCommLoopStatus;
}


