import { APIKey } from './aa0211.interface';
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0214: 更新Group
 * 在TSMP中更新Group。
 */
export interface ReqAA0214 extends BaseReq {
    ReqBody: AA0214Req;
}
export interface AA0214Req {
    groupID: string;
    groupName: string;
    groupAlias: String;
    allowDays: number;
    allowDaysUnit: String;
    allowTimes: number;
    groupDesc: String;
    securityLevel: String;
    // groupAuthoritiesId: Array<object>;
    groupAuthoritiesId: string[];
    oriApiList?:  Array<object>;
    newApiList:  Array<object>;
    groupAccess: Array<string>;

    //下面這些等創建群組完成後要清除掉
    newGroupName?: string;
    newGroupAlias?: string;
    newGroupDesc?: string;
    newSecurityLevel?: string;
    newGroupAccess?: Array<string>;
    newGroupAuthorities?: Array<string>;
    newAllowDays?: number;
    newAllowTimes?: number;
    newApiKeyList?: Array<APIKey>;
}

export interface ResAA0214 extends BaseRes {
    RespBody: AA0214Resp;
}
export interface AA0214Resp {
}
