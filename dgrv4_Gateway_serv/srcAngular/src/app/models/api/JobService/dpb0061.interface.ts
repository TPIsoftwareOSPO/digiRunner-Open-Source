import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0061 extends BaseReq {
    ReqBody: DPB0061Req;
}
export interface DPB0061Req {
    apptJobId: number;
}

export interface ResDPB0061 extends BaseRes {
    RespBody: DPB0061Resp;
}
export interface DPB0061Resp {
    apptJobId: number;
    refItemNo: string;
    itemName: string;
    refSubitemNo: string;
    subItemName: string;
    status: string;
    inParams: string;
    execResult: string;
    execOwner: string;
    stackTrace: string;
    jobStep: string;
    startDateTime: string;
    fromJobId: number;
    createDateTime: string;
    createUser: string;
    updateDateTime: string;
    updateUser: string;
    lv: number;
    canExec: string;
    identifData: string;
    fileList: FileList[];
}
export interface FileList {
    fileId: number;
    fileName: string;
    filePath: string;
    isPreviewable: string;
    fileContent: string;
}
