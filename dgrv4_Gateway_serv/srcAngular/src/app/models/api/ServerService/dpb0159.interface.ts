import { BaseReq, BaseRes } from '../base.interface';


export interface ReqDPB0159 extends BaseReq {
    ReqBody: DPB0159Req;
}
export interface DPB0159Req { }

export interface RespDPB0159 extends BaseRes {
    RespBody: DPB0159Resp;
}

export interface DPB0159Resp {
  ldapIdPInfoList: Array<DPB0159Item>;
}

export interface DPB0159Item {
  id: string;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
  ldapTimeout: string;
  ldapStatus: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}
