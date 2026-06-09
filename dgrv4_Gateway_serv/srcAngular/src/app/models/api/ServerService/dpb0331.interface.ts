import { BaseReq, BaseRes } from '../base.interface';
import { SmartClientDto } from './dpb0330.interface';

// ==================== DPB0331：新增 SMART Client ====================

export interface ReqDPB0331 extends BaseReq {
  ReqBody: DPB0331ReqBody;
}

export interface DPB0331ReqBody {
  clientId?: string;
  clientType?: string;
  idpType?: string;
  idpClientId?: string;
  allowedScopes?: string[];
  redirectUris?: string[];
  launchMode?: string;
  autoApprove?: string;
  tokenEndpointAuthMethod?: string;
  jwksUri?: string;
  jwks?: string;
}

export interface RespDPB0331 extends BaseRes {
  RespBody: SmartClientDto;
}
