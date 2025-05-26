import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0215: 刪除Group
 * 在TSMP中刪除Group與相關API資料。若該Group已有Client參考，則不可刪除。
 */
export interface ReqAA0215 extends BaseReq {
    ReqBody: AA0215Req;
}
export interface AA0215Req {
    groupID: string;

    
    //下面這些等創建群組完成後要清除掉
    groupName: string;
}

export interface ResAA0215 extends BaseRes {
    RespBody: AA0215Resp;
}
export interface AA0215Resp {
}

