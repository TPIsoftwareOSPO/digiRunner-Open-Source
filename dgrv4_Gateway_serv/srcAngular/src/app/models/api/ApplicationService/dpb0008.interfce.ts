import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0008 extends BaseReq {
    ReqBody: DPB0008Req;
}
export interface DPB0008Req {
    appCateId: number;
    keyword?: string;
}

export interface ResDPB0008 extends BaseRes {
    RespBody: DPB0008Resp;
}
export interface DPB0008Resp {
    appCateList: Array<DPB0008AppCate>;
}
export interface DPB0008AppCate {
    appCateId: number;
    appCateName: string;
    dataSort: number;
    createDateTime: string;
    updateDateTime: string;
}