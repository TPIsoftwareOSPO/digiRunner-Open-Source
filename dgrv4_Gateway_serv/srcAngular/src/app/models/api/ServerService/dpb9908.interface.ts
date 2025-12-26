import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9908 extends BaseReq {
    ReqBody: DPB9908Req;
}

export interface DPB9908Req {    
    itemNo: string;    
    oriItemNo: string;    
    dataList:Array<DPB9908Item>;
}

export interface RespDPB9908 extends BaseRes {
    RespBody: DPB9908Resp;
}

export interface DPB9908Resp { }

export interface DPB9908Item {    
    locale: string;
    itemName: string;
}


