import { EmApptJobStatus } from '../../common.enum';
import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';

export interface ReqCUS0005 extends BaseRes {
    ReqBody: CUS0005Req;
}
export interface CUS0005Req {
    apptJobId: number;
}

export interface ResCUS0005 extends BaseRes {
    RespBody: CUS0005Resp;
}
export interface CUS0005Resp {
    commLoopStatus: CusCommLoopStatus;
}


