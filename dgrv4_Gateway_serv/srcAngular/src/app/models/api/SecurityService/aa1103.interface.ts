import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1103 extends BaseReq {
    ReqBody: AA1103Req;
}
export interface AA1103Req {
    securityLevelId: string;
    oriSecurityLevelName: string; // 原始資料-安全等級名稱
    newSecurityLevelName: string; // 新-安全等級名稱
    newSecurityLevelDesc?: string; // 新-安全等級描述
}

export interface ResAA1103Before extends BaseRes {
    RespBody: AA1103RespBefore;
}
export interface AA1103RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1103 extends BaseRes {
    RespBody: AA1103Resp;
}
export interface AA1103Resp {
    securityLevelId?: string; // 更新成功時才回傳, 否則回傳空值
}