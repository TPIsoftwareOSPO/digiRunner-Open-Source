import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0067 extends BaseReq {
    ReqBody: DPB0067Req;
}
export interface DPB0067Req {
    reqOrdermId?: number;
    keyword?: string;
    startDate: string; // YYYY/MM/DD
    endDate: string; // YYYY/MM/DD
    encodeReqType?: string; // 用戶端申請API 或是 API上下架 或是 全部(部傳送此參數) Bcrypt
    encodeReqSubtype?: string; // API上架 或是 API下架 或是 API異動 或是 全部(部傳送此參數) Bcrypt，此參數只會在encodeReqType = API上下架
    encodeQuyType: string; // REQ: 申請單, EXA: 待審單, REV: 已審單
}

export interface ResDPB0067 extends BaseRes {
    RespBody: DPB0067Resp;
}
export interface DPB0067Resp {
    dataList: Array<DPB0067RespItem>;
}
export interface DPB0067RespItem {
    reqOrdermId: number;
    createDateTime: string;
    reqOrderNo: string;
    clientId?: string;
    reqUserId?: string;
    applyUserName: string;
    applyType: string;
    nextCheckPoint: number; // 下一關卡 1、2、3...
    checkPointName: string;
    chkStatus: string;
    orgName: string;
    reviewVisiable: string; // [簽核] "Y"/"N"
    trakerVisiable: string; // [歷程] "Y"/"N"
    closeVisiable: string; // [結案] "Y"/"N"
    resendVisiable: string; // [重送] "Y"/"N"
    updateVisiable: string; // [更新] "Y"/"N"
    sendVisible: string; // [送審] "Y"/"N"
}