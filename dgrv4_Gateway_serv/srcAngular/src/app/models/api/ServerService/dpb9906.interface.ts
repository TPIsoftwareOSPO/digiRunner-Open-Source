import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9906 extends BaseReq {
    ReqBody: DPB9906Req;
}

export interface DPB9906Req {
    locale: string;
    itemNo: string;
    subitemNo: string;
    getSubitemNameList?: string;
}

export interface RespDPB9906 extends BaseRes {
    RespBody: DPB9906Resp;
}

export interface DPB9906Resp {
    locale: string;
    itemId: number|null;
    sortBy: number|null;
    isDefault?: string;
    itemNo: string;
    itemName: string;
    subitemNo: string;
    subitemName: string;
    params:Array<string>;
    subitemNameList:Array<DPB9906ItemValid>;
    oriSubitemNo?:string;
    paramSize:number;
}

export interface DPB9906Item {
    version: number;
    locale: string;
    subitemName: string;
    params: Array<string>;
}

export interface DPB9906ItemValid {
  version: number;
  locale: string;
  subitemName: string;
  params: Array<string>;
  valid?:boolean
}


