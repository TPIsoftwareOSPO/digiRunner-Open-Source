import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { Component, OnInit, Input, Output } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { DPB0093ApiItem, DPB0093Req } from 'src/app/models/api/OpenApiService/dpb0093.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { of } from 'rxjs';

@Component({
    selector: 'app-api-list',
    templateUrl: './api-list.component.html',
    styleUrls: ['./api-list.component.css']
})
export class ApiListComponent implements OnInit {

    @Input() data!: FormParams;
    @Output() close!: Function;

    keyword: string = '';
    cols: { field: string; header: string; }[] = [];
    dataList: Array<DPB0093ApiItem> = new Array<DPB0093ApiItem>();
    selectedApis: Array<DPB0093ApiItem> = new Array<DPB0093ApiItem>();
    rowcount: number = 0;

    constructor(
        private tool: ToolService,
        private file: FileService,
        private openApiService: OpenApiKeyService,
        private ref: DynamicDialogRef,
        private config: DynamicDialogConfig
    ) { }

    async ngOnInit() {
        this.keyword = this.config.data.data.keyword;
        const code = ['api_name', 'api_desc', 'org_name', 'theme_name', 'api_doc'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'apiName', header: dict['api_name'] },
            { field: 'apiDesc', header: dict['api_desc'] },
            { field: 'orgName', header: dict['org_name'] },
            // { field: 'themeDatas', header: dict['theme_name'] },
            // { field: 'fileName', header: dict['api_doc'] }
        ];
        this.searchAPI();
    }

    searchAPI() {
        let ReqBody = {
            keyword: this.keyword
        } as DPB0093Req;
        this.openApiService.queryApiLikeList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    moreData() {
        let ReqBody = {
            apiKey: this.dataList[this.dataList.length - 1].apiKey,
            moduleName: this.dataList[this.dataList.length - 1].moduleName,
            keyword: this.keyword
        } as DPB0093Req;
        this.openApiService.queryApiLikeList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    chooseAPI() {
        // if (this.close) this.close(of(this.selectedApis));
        this.ref.close(this.selectedApis)
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

}
