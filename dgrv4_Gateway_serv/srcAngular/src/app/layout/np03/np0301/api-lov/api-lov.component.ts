import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { DPB0075RespItem, DPB0075Req } from 'src/app/models/api/LovService/dpb0075.interface';
import { LovService } from 'src/app/shared/services/api-lov.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { of } from 'rxjs';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';

@Component({
    selector: 'app-api-lov',
    templateUrl: './api-lov.component.html',
    styleUrls: ['./api-lov.component.css']
})
export class ApiLovComponent implements OnInit {

    @Input() data!: FormParams;
    @Input() close!: Function;

    keyword: string ='';
    cols: { field: string; header: string; }[] = [];
    dataList: Array<DPB0075RespItem> = new Array();
    selected: any; // Array<DPB0075RespItem> || DPB0075RespItem
    rowcount: number=0;

    constructor(
        private lov: LovService,
        private tool: ToolService,
        private file: FileService,
        private ref: DynamicDialogRef,
        private config: DynamicDialogConfig

    ) { }

    async ngOnInit() {
        const code = ['api_name', 'theme_name', 'org_name', 'api_desc', 'public_flag', 'api_doc'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'apiName', header: dict['api_name'] },
            { field: 'apiDesc', header: dict['api_desc'] },
            { field: 'publicFlagName', header: dict['public_flag'] },
            { field: 'orgName', header: dict['org_name'] },
            { field: 'themeDatas', header: dict['theme_name'] },
            { field: 'fileName', header: dict['api_doc'] }
        ];
        this.dataList = this.config.data.data.apiList;
        this.rowcount = this.dataList.length;
        this.keyword = this.config.data.data.keyword;
        if (window.location.hash == '#/np04/np0401') {
            switch (this.config.data.data.dpStatus) {
                case '0': // 挑選未上架的API，所以為單選
                    this.selected = {} as DPB0075RespItem;
                    if (this.config.data.data.selectedApis) {
                        this.selected = this.dataList.filter(api => api.apiUid == this.config.data.data.selectedApis.apiUid)[0];
                    }
                    break;
                case '1': // 挑選已上架的API，所以可多選
                    this.selected = new Array<DPB0075RespItem>();
                    if (this.config.data.data.selectedApis) {
                        this.config.data.data.selectedApis.map(api => {
                            this.selected = this.selected.concat(this.dataList.filter(item => item.apiUid == api.apiUid));
                        });
                    }
                    break;
            }
        }
        else {
            this.selected = this.config.data.data.selectedApis;
        }
    }

    searchAPI() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        if (window.location.hash == '#/np04/np0401') {
            switch (this.data.data.dpStatus) {
                case '0': // 挑選未上架的API，所以為單選
                    this.selected = {} as DPB0075RespItem;
                    break;
                case '1': // 挑選已上架的API，所以可多選
                    this.selected = new Array<DPB0075RespItem>();
                    break;
            }
        }
        else {
            this.selected = new Array<DPB0075RespItem>();
        }
        let ReqBody = {
            keyword: this.keyword,
            dpStatus: this.config.data.data.dpStatus
        } as DPB0075Req;
        this.lov.queryApiLov(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    chooseAPI() {
        // if (this.close) this.close(of(this.selected));
        this.ref.close(this.selected)
    }

    moreData() {
        let ReqBody = {
            apiKey: this.dataList[this.dataList.length - 1].apiKey,
            moduleName: this.dataList[this.dataList.length - 1].moduleName,
            keyword: this.keyword,
            dpStatus: this.config.data.data.dpStatus
        } as DPB0075Req;
        this.lov.queryApiLov(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    checkLength(obj: object, index: number): boolean {
        if (Object.keys(obj).length > 1 && (index + 1) != Object.keys(obj).length)
            return true;
        else
            return false;
    }

    downloadFile(filePath: string, fileName: string) {
        let ReqBody = {
            filePath: filePath
        } as DPB0078Req;
        this.file.downloadFile(ReqBody).subscribe(res => {
            const reader = new FileReader();
            reader.onloadend = function () {
                // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
                //     window.navigator.msSaveBlob(res, fileName)
                // }
                // else {
                    const file = new File([res], fileName);
                    const url = window.URL.createObjectURL(file);
                    const a = document.createElement('a');
                    document.body.appendChild(a);
                    a.setAttribute('style', 'display: none');
                    a.href = url;
                    a.download = fileName;
                    a.click();
                    window.URL.revokeObjectURL(url);
                    a.remove();
                // }
            }
            reader.readAsText(res);
        });
    }

    fileNameConvert(fileName: string): string {
        if (fileName) {
            return decodeURIComponent(fileName);
        }
        return '';
    }
}
