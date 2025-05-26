import { BaseReq, BaseRes } from '../base.interface';

export interface ReqSSLDecoder extends BaseReq {
  ReqBody: SSLDecoderReq;
}

export interface SSLDecoderReq {
  cert: string;
}

export interface RespSSLDecoder extends BaseRes {
  RespBody: SSLDecoderResp;
}

export interface SSLDecoderResp {
  subjectDetail: SubjectDetail;
  issuerDetail: IssuerDetail;
  notBefore: string;
  notAfter: string;
  serialNumber: string;
  fingerprintSha1: string;
  fingerprintMd5: string;
}

export interface SubjectDetail {
  common: string;
  organization: string;
  organizationUnit: string;
  country: string;
  state: string;
  city: string;
  eMail?: string;
}

export interface IssuerDetail {
  common: string;
  organization: string;
  organizationUnit: string;
  country: string;
  state: string;
  city: string;
  eMail?: string;
}
