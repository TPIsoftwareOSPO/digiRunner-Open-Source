import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import * as FileSaver from 'file-saver';
// import * as XLSX from 'xlsx';

const EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
const EXCEL_EXTENSION = '.xlsx';
const CSV_TYPE = 'text/csv;charset=utf-8;';
const CSV_EXTENSION = '.csv';

@Injectable({
    providedIn: 'root'
})
export class ExportService {

    constructor() { }

    public exportFile(json: any[], fileName: string, isExcel: boolean): void {
        let newJson = this.jsonFormat(json, isExcel);
        if (isExcel) {
            this.exportAsExcelFile(newJson, fileName, isExcel);
        }
        else {
            this.exportAsCsvFile(newJson, fileName, isExcel);
        }
    }

    private jsonFormat(json: any[], isExcel: boolean): any[] {

        if (isExcel) {
            let excelJson = json.map(item => {

                let formatHttpHeader = (item.httpHeader == undefined || item.httpHeader == '') ? '無資料' : item.httpHeader;
                formatHttpHeader = (formatHttpHeader.length > 32767) ? '資料長度過長，超過Excel可顯示的字元數' : formatHttpHeader;
                let formatBody = (item.mBody == undefined || item.mBody == '') ? '無資料' : item.mBody;
                formatBody = (formatBody.length > 32767) ? '資料長度過長，超過Excel可顯示的字元數' : formatBody;
                return {
                    txsn: item.txsn,
                    type: item.type,
                    txid: item.txId,
                    msgType: item.mType,
                    txDate: item.txDate,
                    clientId: item.cId,
                    rtnCode: item.rCode,
                    rtnMsg: item.rMsg,
                    user: item.user,
                    ip: item.cip,
                    httpHeader: formatHttpHeader,
                    mbody: formatBody
                }
            });

            return excelJson;
        }
        else {
            let csvJson = json.map(item => {
                return {
                    txsn: item.txsn,
                    type: item.type,
                    txid: item.txId,
                    msgType: item.mType,
                    txDate: item.txDate,
                    clientId: item.cId,
                    rtnCode: item.rCode,
                    rtnMsg: item.rMsg,
                    user: item.user,
                    ip: item.cip,
                    httpHeader: item.httpHeader,
                    mbody: item.mBody
                }
            });
            return csvJson;
        }
    }

    private exportAsExcelFile(json: any[], fileName: string, isExcel: boolean): void {
        // console.log('json', json);
        // const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(json);
        // // console.log('worksheet', worksheet);
        // const workbook: XLSX.WorkBook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
        // const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
        // //const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'buffer' });
        // this.saveFile(excelBuffer, fileName, isExcel);
        const data: Blob = new Blob([JSON.stringify(json)], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
        });
        this.saveFile(data, fileName, isExcel);
    }

    private exportAsCsvFile(json: any[], fileName: string, isExcel: boolean): void {
        let array = typeof json != 'object' ? JSON.parse(json) : json;
        let csvStr = '';
        let csvHeader = '';
        for (let i = 0; i < array.length; i++) {
            let line = '';
            Object.keys(array[i]).map((tag, idx, arr) => {

                if (i == 0) {
                    if(idx==0)
                    {
                        csvHeader += tag;
                    }
                    else{
                        csvHeader += ',' + tag;
                    }
                }
                if(idx<arr.length-2)
                {
                    line += array[i][tag] + ','
                }
                else{
                    line += array[i][tag]
                }

            })
            if (i == 0) csvStr += csvHeader + '\r\n';
            csvStr += line + '\r\n';
        }
        this.saveFile(csvStr, fileName, isExcel);
    }

    private saveFile(buffer: any, fileName: string, isExcel: boolean): void {
        if (isExcel) {
            const data: Blob = new Blob([buffer], {
                type: EXCEL_TYPE
            });
            FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
        }
        else {
            const data: Blob = new Blob([buffer], {
                type: CSV_TYPE
            });
            FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + CSV_EXTENSION);
        }
    }
}
