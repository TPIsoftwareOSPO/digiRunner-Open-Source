import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';

import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0094Req, DPB0094RespItem } from 'src/app/models/api/OpenApiService/dpb0094.interface';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import * as dayjs from 'dayjs';
import { DPB0091Resp, DPB0091Req } from 'src/app/models/api/OpenApiService/dpb0091.interface';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';

@Component({
    selector: 'app-np0504',
    templateUrl: './np0504.component.html',
    styleUrls: ['./np0504.component.css']
})
export class Np0504Component extends BaseComponent implements OnInit {

    currentTitle: string = this.title;
    pageNum: number = 1; // 1：open api key 列表、2：open api key detail
    form: FormGroup;
    clientOpenApiKeyListCols: { field: string; header: string; width: string; }[] = [];
    clientOpenApiKeyListData: Array<DPB0094RespItem> = new Array<DPB0094RespItem>();
    clientOpenApiKeyRowcount: number = 0;
    openApiKeyDetailTitle: string = '';
    openApiKeyDetailCols: { field: string; header: string; width: string }[] = [];
    openApiKeyDetailData?: DPB0091Resp;
    openApiKeyDetailRowcount: number = 0;

    constructor(
         route: ActivatedRoute,
         tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private openApiService: OpenApiKeyService,
        private file: FileService
    ) {
        super(route, tr);

        this.form = this.fb.group({
          keyword: new FormControl(''),
          startDate: new FormControl(''),
          endDate: new FormControl('')
        });
    }

    async ngOnInit() {

        this.converDateInit();
        const code = ['client_id', 'client_name', 'client_alias', 'open_api_key_alias', 'create_date', 'expired_date', 'revoked_date', 'status', 'button.detail', 'api_name', 'api_desc', 'theme_name', 'dept', 'api_doc'];
        const dict = await this.tool.getDict(code);
        this.clientOpenApiKeyListCols = [
            { field: 'clientId', header: dict['client_id'], width: '10%' },
            { field: 'clientName', header: dict['client_name'], width: '10%' },
            { field: 'clientAlias', header: dict['client_alias'], width: '10%' },
            { field: 'openApiKey', header: 'API Key', width: '20%' },
            { field: 'openApiKeyAlias', header: dict['open_api_key_alias'], width: '10%' },
            { field: 'createDateTime', header: dict['create_date'], width: '10%' },
            { field: 'expiredAt', header: dict['expired_date'], width: '10%' },
            { field: 'revokedAt', header: dict['revoked_date'], width: '10%' },
            { field: 'openApiKeyStatusName', header: dict['status'], width: '10%' }
        ];
        this.openApiKeyDetailTitle = `${this.title} > API Key ${dict['button.detail']}`;
        this.openApiKeyDetailCols = [
            { field: 'apiName', header: dict['api_name'], width: '15%' },
            { field: 'apiDesc', header: dict['api_desc'], width: '15%' },
            // { field: 'themeDatas', header: dict['theme_name'], width: '15%' },
            { field: 'orgName', header: dict['dept'], width: '15%' },
            // { field: 'fileName', header: dict['api_doc'], width: '15%' }
        ];
        this.openApiService.queryOpenApiKeyByDateAndLike_before().subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
                this.clientOpenApiKeyListData = [];
                this.clientOpenApiKeyRowcount = this.clientOpenApiKeyListData.length;
                let ReqBody = {
                    startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
                    endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
                    keyword: this.keyword!.value
                } as DPB0094Req;
                this.openApiService.queryOpenApiKeyByDateAndLike_ignore1298(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.clientOpenApiKeyListData = res.RespBody.dataList;
                        this.clientOpenApiKeyRowcount = this.clientOpenApiKeyListData.length;
                    }
                });
            }
        });
    }

    converDateInit() {
        let date = new Date();
        this.startDate!.setValue(this.tool.addDay(date, -6));
        this.endDate!.setValue(date);
    }

    submitForm() {
        this.clientOpenApiKeyListData = [];
        this.clientOpenApiKeyRowcount = this.clientOpenApiKeyListData.length;
        let ReqBody = {
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            keyword: this.keyword!.value
        } as DPB0094Req;
        this.openApiService.queryOpenApiKeyByDateAndLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.clientOpenApiKeyListData = res.RespBody.dataList;
                this.clientOpenApiKeyRowcount = this.clientOpenApiKeyListData.length;
            }
        });
    }

    moreClientOpenApiKeyList() {
        let ReqBody = {
            openApiKeyId: this.clientOpenApiKeyListData[this.clientOpenApiKeyListData.length - 1].openApiKeyId,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            keyword: this.keyword!.value
        } as DPB0094Req;
        this.openApiService.queryOpenApiKeyByDateAndLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.clientOpenApiKeyListData = this.clientOpenApiKeyListData.concat(res.RespBody.dataList);
                this.clientOpenApiKeyRowcount = this.clientOpenApiKeyListData.length;
            }
        });
    }

    queryOpenApiKeyDetail(rowData: DPB0094RespItem) {
        this.openApiKeyDetailData = {} as DPB0091Resp;
        let ReqBody = {
            openApiKeyId: rowData.openApiKeyId
        } as DPB0091Req;
        this.openApiService.queryOpenApiKeyDetailByPk(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.openApiKeyDetailData = res.RespBody;
                this.openApiKeyDetailRowcount = this.openApiKeyDetailData.dataList.length;
                this.changePage('detail');
            }
        });
    }

    moreOpenApiKeyDetailData() {
        let ReqBody = {
            apiKey: this.openApiKeyDetailData?.dataList[this.openApiKeyDetailData.dataList.length - 1].apiKey,
            moduleName: this.openApiKeyDetailData?.dataList[this.openApiKeyDetailData.dataList.length - 1].moduleName,
            openApiKeyId: this.openApiKeyDetailData?.openApiKeyId
        } as DPB0091Req;
        this.openApiService.queryOpenApiKeyDetailByPk(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.openApiKeyDetailData!.dataList = this.openApiKeyDetailData!.dataList.concat(res.RespBody.dataList);
                this.openApiKeyDetailRowcount = this.openApiKeyDetailData!.dataList.length;
            }
        });
    }

    changePage(action: string) {
        switch (action) {
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                this.converDateInit();
                break;
            case 'detail':
                this.currentTitle = this.openApiKeyDetailTitle;
                this.pageNum = 2;
                break;
        }
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

    headerReturn() {
      this.changePage('query');
    }

    public get keyword() { return this.form.get('keyword'); };
    public get startDate() { return this.form.get('startDate'); };
    public get endDate() { return this.form.get('endDate'); };

}
