import { APIKey } from './aa0211.interface';
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0224: 更新Virtul Group
 * 在TSMP中更新Virtul Group。
 */
export interface ReqAA0224 extends BaseReq {
    ReqBody: AA0224Req;
}
export interface AA0224Req {
    vgroupId: string;
    oriVgroupName: string;
    newVgroupName?: string;
    oriVgroupAlias?: string;
    newVgroupAlias?: string;
    oriAllowDays?: number;
    newAllowDays?: number;
    oriTimeUnit: string;
    newTimeUnit: string;
    oriAllowTimes?: number;
    newAllowTimes?: number;
    oriVgroupAuthoritieIds: Array<string>;
    newVgroupAuthoritieIds: Array<string>;
    oriSecurityLevelId?: string;
    newSecurityLevelId?: string;
    oriVgroupDesc?: string;
    newVgroupDesc?: string;
    oriApiList: Array<AA0224Item>;
    newApiList: Array<AA0224Item>;

    //之後要刪掉
    vgroupID: string;
    vgroupName: string;
    newSecurityLevel?: string;
    newVgroupAccess?: Array<string>;
    newVgroupAuthorities?: Array<string>;
    newApiKeyList?: Array<APIKey>;
}

export interface ResAA0224 extends BaseRes { }

export interface AA0224Item {
    moduleName: string;
    apiKeyList: Array<string>;
}