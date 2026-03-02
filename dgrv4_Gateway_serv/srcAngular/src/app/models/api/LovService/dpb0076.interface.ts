import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0076 extends BaseReq {
    ReqBody: DPB0076Req;
}
export interface DPB0076Req {
    themeId?: number;
    keyword?: string;
}

export interface ResDPB0076 extends BaseRes {
    RespBody: DPB0076Resp;
}
export interface DPB0076Resp {
    dataList: Array<DPB0076RespItem>;
}
export interface DPB0076RespItem {
    themeId: number;
    themeName: string;
    orgId: string;
}