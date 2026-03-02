import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0153 extends BaseReq {
  ReqBody: DPB0153Req;
}
export interface DPB0153Req {
  id?: string;
  keyword?: string;
  websiteStatus?: string;
}

export interface RespDPB0153 extends BaseRes {
  RespBody: DPB0153Res;
}
export interface DPB0153Res {
  websiteList: Array<DPB0153WebsiteItem>

}


export interface DPB0153WebsiteItem {
    id: string;
    name: string;
    status: string;
    remark?: DPB0153Trunc;
}

export interface DPB0153Trunc {
  val: string;
  t: string;
  ori?: string;
}
