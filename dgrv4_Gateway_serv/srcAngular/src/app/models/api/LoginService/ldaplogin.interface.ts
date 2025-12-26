import { BaseReq, BaseRes } from '../base.interface';

export interface RespLdapLogin extends BaseRes {
    RespBody: LdapLoginResp;
}
export interface LdapLoginResp {
    dataList: Array<LdapEnvItem>;    
}
export interface LdapEnvItem {    
    envName: string;
    envUrl: string;
}

export interface LdapLoginItem {
    username: string;
    password: string;
    ldapLoginUrl: string;
    codeVerifier?: string;
    userMail?: string;
}

export interface LdapSsoTokenItem {
    grant_type: string;
    username?: string;
    codeVerifier?: string;
    userMail?: string;
    ldapLoginUrl: string;
    refresh_token?: string;
}

export interface ReqLdapEnvData extends BaseReq{
    ReqBody: LdapEnvDataReq;
}

export interface LdapEnvDataReq {
    udpParam:string;
}

export interface RespLdapEnvData extends BaseRes {
    RespBody: LdapLoginResp;
}

export interface LdapEnvDataResp {
    dataList: Array<LdapEnvItem>;    
}