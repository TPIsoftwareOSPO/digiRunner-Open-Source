import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0252 extends BaseReq {
  ReqBody: DPB0252Req;
}

export interface DPB0252Req {
  // aiProviderId:string;
}

export interface RespDPB0252 extends BaseRes {
  RespBody: DPB0252Resp;
}

export interface DPB0252Resp {
  aiProviderId: string;
  aiProviderName: string;
  aiModel: string;
  generateApi: string;
  countTokenApi: string;
  aiProviderEnable: string;
  
  aiProviderAlias:string;
}

// export interface RespDPB0252Before extends BaseRes {
//   RespBody: RespDPB0252RespBefore;
// }

// export interface RespDPB0252RespBefore {
//   constraints: Array<ValidatorFormat>;
// }

