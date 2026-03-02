import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0219 extends BaseReq {
    ReqBody: AA0219Req;
}
export interface AA0219Req {
    clientID: string;
}

export interface ResAA0219 extends BaseRes {
    RespBody: AA0219Resp;
}
export interface AA0219Resp {
    clientID: string;
    authorizedGrantType: Array<string>;
    webServerRedirectUri: string;
    accessTokenValidity: number;
    raccessTokenValidity?: number;
    accessTokenQuota: number;
    refreshTokenQuota: number;
    webServerRedirectUri1?: string;
    webServerRedirectUri2?: string;
    webServerRedirectUri3?: string;
    webServerRedirectUri4?: string;
    webServerRedirectUri5?: string;
}
