import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0218 extends BaseReq {
    ReqBody: AA0218Req;
}
export interface AA0218Req {
    clientID: string;
    authorizedGrantType: Array<string>;
    accessTokenValidity: number;
    raccessTokenValidity: number;
    accessTokenValidityTimeUnit: string;
    raccessTokenValidityTimeUnit: string;
    accessTokenQuota: number;
    refreshTokenQuota: number;
    webServerRedirectUri: string;
    webServerRedirectUri1?: string;
    webServerRedirectUri2?: string;
    webServerRedirectUri3?: string;
    webServerRedirectUri4?: string;
    webServerRedirectUri5?: string;
}

export interface ResAA0218Before extends BaseRes {
    RespBody: AA0218RespBefore;
}
export interface AA0218RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0218 extends BaseRes { }
