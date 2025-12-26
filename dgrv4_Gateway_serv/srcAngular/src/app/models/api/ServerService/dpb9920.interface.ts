import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9920 extends BaseReq {
    ReqBody: DPB9920Req;
}

export interface DPB9920Req { 
    fileIdList:number[];
}
