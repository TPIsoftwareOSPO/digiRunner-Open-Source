import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0234 extends BaseReq {
  ReqBody: DPB0234Req;
}

export interface DPB0234Req {
  keyWords?: string;
  xApiKey?: string;
  flag: string;
}

export interface RespDPB0234 extends BaseRes {
  RespBody: DPB0234Resp;
}

export interface DPB0234Resp {
  dataList: Array<DPB0234RespItem>;
  // dataListFromXapiKey:DPB0234RespItemFromXapiKey;
  responseFromXapiKey:DPB0234RespItemFromXapiKey;
  totalApi:string;
}

export interface DPB0234RespItem {
  groupId: string;
  groupAlias: string;
  groupName: string;
  clientIdList: Array<string>;
  clientNameList: Array<string>;
  apiDataList:Array<DPB0234ApiDataItem>;
}

export interface DPB0234RespItemFromXapiKey {
  apiKeyId:string;
  apiKeyMask:string;
  apiKeyAlias:string;

  effectiveAt:string;
  expiredAt:string;
  // clientIdList:Array<string>;
  // clientNameList:Array<string>;
  // apiDataList:Array<DPB0234XApiKeyDataItem>
  totalApi: string;
  groupList:Array<DPB0234GroupListItem>;
  clientId: string;
  clientName: string;
}

export interface DPB0234GroupListItem{
  groupId:string;
  groupName:string;
  groupAlias:string;
  apiDataList: Array<DPB0234ApiDataItem>
}

export interface DPB0234XApiKeyDataItem {
  apiKeyId: string;
  apiKeyMask: string;
  apiKeyAlias: string;
  effectiveAt: string;
  expiredAt: string;
}
export interface DPB0234ClientDataItem {
  clientId: string;
  clientName: string;
  apiDataList: Array<DPB0234ApiDataItem>;
}
export interface DPB0234ApiDataItem {
  apiStatus: string;
  apiName: string;
  apiPath: string;
  moduleName:string;
}
