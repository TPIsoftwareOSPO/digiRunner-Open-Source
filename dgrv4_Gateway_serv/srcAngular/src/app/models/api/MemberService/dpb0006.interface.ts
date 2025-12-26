import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0006 extends BaseReq {
    ReqBody: DPB0006Req;
}
export interface DPB0006Req {
    clientId?: string;
    keyword?: string;
    regStatus: string; // v3.8 版申請單固定帶入2 (放行)
    startDate?: string; // YYYY/MM/DD
    endDate?: string; //YYYY/MM/DD
}

export interface ResDPB0006 extends BaseRes {
    RespBody: DPB0006Resp;
}
export interface DPB0006Resp {
    clientList: Array<DPB0006Client>
}
export interface DPB0006Client {
    refClientId: string;
    clientId: string;
    clientName: string;
    clientAlias?: string;
    emails?: string;
    clientStatus: string;
    applyPurpose: string;
    reviewRemark: string;
    refReviewUser: string;
    resubmitDateTime: string;
    regStatus: string;
    applyFlag: boolean; // 用戶註冊狀態必須是"已放行"才可申請API
    fileList: Array<DPB0004File>;
    refClientName: string; // 前端顯示用
    publicFlag?: string;
    publicFlagName?: string;
    status: string; // clientStatus
    statusName: string; // 狀態名稱
    checkPointName: string; // 關卡名稱
    chkStatus: string; // 最近關卡審核狀態代碼
    chkStatusName: string; // 最近關卡審核狀態名稱
}
export interface DPB0004File {
    fileName: string;
    filePath: string;
    fileId: number;
}