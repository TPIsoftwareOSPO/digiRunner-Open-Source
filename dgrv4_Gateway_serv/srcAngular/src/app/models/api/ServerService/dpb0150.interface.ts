import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';


export interface ReqDPB0150 extends BaseReq {
    ReqBody: DPB0150Req;
}
export interface DPB0150Req {
    idpType:string;
    clientId:string;
    clientMima:string;
    clientName:string;
    clientStatus:string;
    idpWellKnownUrl:string;
    callbackUrl:string;
    authUrl?:String;
    accessTokenUrl?:string;
    scope?:string;
}

export interface RespDPB0150 extends BaseRes {
    RespBody: DPB0150Res;
}
export interface DPB0150Res { }

export interface RespDPB0150Before extends BaseRes {
    RespBody: DPB0150RespBefore;
}
export interface DPB0150RespBefore {
    constraints: Array<ValidatorFormat>;
}

