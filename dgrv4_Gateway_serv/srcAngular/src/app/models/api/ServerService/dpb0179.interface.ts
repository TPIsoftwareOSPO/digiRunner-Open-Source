import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0179 extends BaseReq {
  ReqBody: DPB0179Req;
}

export interface DPB0179Req { }

export interface RespDPB0179 extends BaseRes {
  RespBody: DPB0179Resp;
}

export interface DPB0179Resp {
  idPInfoList: Array<DPB0179IdPInfoItem>;
}

export interface DPB0179IdPInfoItem {
  id: string;
  longId: string;
  iconFile?: string;
  status: string;
  pageTitle: string;
  ldapTimeout: string;
  ldapDataList: Array<DPB0179LdapDataItem>;
}

export interface DPB0179LdapDataItem {
  longId: string;
  orderNo: number;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
}
