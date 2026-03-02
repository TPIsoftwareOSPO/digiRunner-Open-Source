export interface ReqHeader {
    txSN: string;
    txDate: string;
    txID: string;
    cID: string;
    locale: string;
    [key:string]: string
  }
export interface ResHeader {
    txSN: string;
    txDate: string;
    txID: string;
    rtnCode: string;
    rtnMsg: string;
}
