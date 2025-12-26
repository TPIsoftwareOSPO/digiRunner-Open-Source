import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9907 extends BaseReq {
    ReqBody: DPB9907Req;
}

export interface DPB9907Req {
    itemNo: string;
}

export interface RespDPB9907 extends BaseRes {
    RespBody: DPB9907Resp;
}

export interface DPB9907Resp {
    itemNo: string;
    dataList:Array<DPB9907ItemValid>
    oriItemNo?: string;
}

export interface DPB9907Item {
    locale: string;
    itemName: string;
}

export interface DPB9907ItemValid {
  locale: string;
  itemName: string;
  valid: boolean;
}


