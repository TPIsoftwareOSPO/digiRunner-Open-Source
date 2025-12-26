import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9916 extends BaseReq {
    ReqBody: DPB9916Req;
}

export interface DPB9916Req {
    fileId: number;
}

export interface RespDPB9916 extends BaseRes {
    RespBody: DPB9916Resp;
}

export interface DPB9916Resp {
    fileId: number; 
    fileName: string;
    refFileCateCode: string;
    refId: number;
    filePath: string;
    isBlob: string;
    createDateTime: string;
    createUser : string;
    updateUser : string;
    updateDateTime : string;
    blobData : string;
    version : number;
}

