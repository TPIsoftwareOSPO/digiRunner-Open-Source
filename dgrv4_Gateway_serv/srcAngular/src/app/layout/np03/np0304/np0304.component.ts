import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0083RespItem } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { DPB0090Req, DPB0090RespItem } from 'src/app/models/api/OpenApiService/dpb0090.interface';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { DPB0091Req, DPB0091Resp } from 'src/app/models/api/OpenApiService/dpb0091.interface';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0092Req } from 'src/app/models/api/OpenApiService/dpb0092.intrface';
import { OpenApiKeyFormComponent } from './open-api-key-form/open-api-key-form.component';
import { MessageService } from 'primeng/api';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import { DPB0095Item, DPB0095Req } from 'src/app/models/api/OpenApiService/dpb0095.interface';

@Component({
    selector: 'app-np0304',
    templateUrl: './np0304.component.html',
    styleUrls: ['./np0304.component.css']
})
export class Np0304Component extends BaseComponent implements OnInit {

    @ViewChild('openApiKeyForm') _openApiKeyForm!: OpenApiKeyFormComponent;

    currentTitle = this.title;
    pageNum: number = 1; // 1: 用戶列表、2: Open API Key 列表、3: Open API Key Detail、
    form!: FormGroup;
    clientListCols: { field: string; header: string; }[] = [];
    clientList: Array<DPB0083RespItem> = new Array<DPB0083RespItem>();
    clientListRowcount: number = 0;
    applyOpenApiKeyTitle: string = '';
    updateOpenApiKeyTitle: string = '';
    revokeOpenApiKeyTitle: string = '';
    applyOpenApiKeyDataParams?: FormParams;
    openApiKeyListTitle: string = '';
    openApiKeyListCols: { field: string; header: string; width: string }[] = [];
    openApiKeyList: Array<DPB0090RespItem> = new Array<DPB0090RespItem>();
    openApiKeyListRowcount: number = 0;
    openApiKeyDetailTitle: string = '';
    openApiKeyDetailCols: { field: string; header: string; width: string }[] = [];
    openApiKeyDetailRowcount: number = 0;
    openApiKeyDetailData?: DPB0091Resp;
    currentClient?: DPB0095Item;
    canCreate: boolean = false;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private openApiService: OpenApiKeyService,
        private file: FileService,
        private message: MessageService,
        private roleService: RoleService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.form = this.fb.group({
            keyword: new FormControl('')
        });
        this.roleService.queryRTMapByUk({ txIdList: ['DPB0092'] } as DPB0115Req).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.canCreate = res.RespBody.dataList.find(item => item.txId === 'DPB0092') ? res.RespBody.dataList.find(item => item.txId === 'DPB0092')!.available : false;
            }
        });
        this.init();
    }

    checkLength(obj: object, index: number): boolean {
        if (Object.keys(obj).length > 1 && (index + 1) != Object.keys(obj).length)
            return true;
        else
            return false;
    }

    async init() {
        const codes = ['client_id', 'client_name', 'client_alias', 'open_api_key_alias', 'create_date', 'expired_date', 'revoked_date', 'status', 'api_name', 'api_desc', 'org_name', 'theme_name', 'api_doc', 'button.view', 'button.detail', 'button.apply_open_api_key', 'button.change', 'button.revoke', 'button.apply', 'api_key_list'];
        const dict = await this.tool.getDict(codes);
        this.clientListCols = [
            { field: 'clientId', header: dict['client_id'] },
            { field: 'clientName', header: dict['client_name'] },
            { field: 'clientAlias', header: dict['client_alias'] }
        ];
        this.openApiKeyListTitle = `${this.title} > ${dict['api_key_list']}`;
        this.openApiKeyListCols = [
            { field: 'clientId', header: dict['client_id'], width: '15%' },
            { field: 'openApiKey', header: 'API Key', width: '25%' },
            { field: 'openApiKeyAlias', header: dict['open_api_key_alias'], width: '15%' },
            { field: 'createDateTime', header: dict['create_date'], width: '10%' },
            { field: 'expiredAt', header: dict['expired_date'], width: '10%' },
            { field: 'revokedAt', header: dict['revoked_date'], width: '10%' },
            { field: 'openApiKeyStatusName', header: dict['status'], width: '5%' }
        ];
        this.openApiKeyDetailTitle = `${this.title} > ${dict['api_key_list']} > ${dict['button.detail']}`;
        this.openApiKeyDetailCols = [
            { field: 'apiName', header: dict['api_name'], width: '15%' },
            { field: 'apiDesc', header: dict['api_desc'], width: '15%' },
            // { field: 'themeDatas', header: dict['theme_name'], width: '15%' },
            { field: 'orgName', header: dict['org_name'], width: '15%' },
            // { field: 'fileName', header: dict['api_doc'], width: '15%' }
        ];
        this.applyOpenApiKeyTitle = `${this.title} > ${dict['api_key_list']} > ${dict['button.apply']}`;
        this.updateOpenApiKeyTitle = `${this.title} > ${dict['api_key_list']} > ${dict['button.change']}`;
        this.revokeOpenApiKeyTitle = `${this.title} > ${dict['api_key_list']} > ${dict['button.revoke']}`;
        this.clientList = [];
        this.clientListRowcount = this.clientList.length;
        this.currentClient = {} as DPB0095Item;
        let ReqBody = {
            keyword: this.keyword!.value,
            regStatus: '2'
        } as DPB0095Req;
        this.openApiService.queryClientListByRegStatusLike_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.pageNum = 1;
                this.clientList = res.RespBody.dataList;
                this.clientListRowcount = this.clientList.length;
            }
        });
    }

    queryClientList() {
        this.clientList = [];
        this.clientListRowcount = this.clientList.length;
        this.currentClient = {} as DPB0095Item;
        let ReqBody = {
            keyword: this.keyword!.value,
            regStatus: '2'
        } as DPB0095Req;
        this.openApiService.queryClientListByRegStatusLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.pageNum = 1;
                this.clientList = res.RespBody.dataList;
                this.clientListRowcount = this.clientList.length;
            }
        });
    }

    moreClientList() {
        let ReqBody = {
            clientId: this.clientList[this.clientList.length - 1].clientId,
            keyword: this.keyword!.value,
            regStatus: '2'
        } as DPB0095Req;
        this.openApiService.queryClientListByRegStatusLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.clientList = this.clientList.concat(res.RespBody.dataList);
                this.clientListRowcount = this.clientList.length;
            }
        });
    }

    viewClientOpenApiList(rowData: DPB0095Item) {
        this.currentClient = rowData;
        this.openApiKeyList = [];
        this.openApiKeyListRowcount = this.openApiKeyList.length;
        let ReqBody = {
            clientId: this.currentClient.clientId
        } as DPB0090Req;
        this.openApiService.queryOpenApiKeyByClientId(ReqBody).subscribe(res => {
            this.currentTitle = this.openApiKeyListTitle;
            this.pageNum = 2;
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.openApiKeyList = res.RespBody.dataList;
                this.openApiKeyListRowcount = this.openApiKeyList.length;
            }
        });
    }

    moreOpenApiList() {
        let ReqBody = {
            openApiKeyId: this.openApiKeyList[this.openApiKeyList.length - 1].openApiKeyId,
            clientId: this.currentClient?.clientId
        } as DPB0090Req;
        this.openApiService.queryOpenApiKeyByClientId(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.openApiKeyList = this.openApiKeyList.concat(res.RespBody.dataList);
                this.openApiKeyListRowcount = this.openApiKeyList.length;
            }
        });
    }

    applyOpenApiKey(applyCategory: string, rowData?: DPB0090RespItem) {
        let ReqBody = {
            clientId: this.currentClient?.clientId,
            encodeReqSubtype: this.tool.Base64Encoder(this.tool.BcryptEncoder('OPEN_API_KEY_APPLICA')) + ',' + 0
        } as DPB0092Req;
        switch (applyCategory) {
            case 'create':
                ReqBody.encodeReqSubtype = this.tool.Base64Encoder(this.tool.BcryptEncoder('OPEN_API_KEY_APPLICA')) + ',' + 0;
                break;
            case 'update':
                ReqBody.encodeReqSubtype = this.tool.Base64Encoder(this.tool.BcryptEncoder('OPEN_API_KEY_UPDATE')) + ',' + 1;
                ReqBody.openApiKeyId = rowData?.openApiKeyId;
                break;
            case 'revoke':
                ReqBody.encodeReqSubtype = this.tool.Base64Encoder(this.tool.BcryptEncoder('OPEN_API_KEY_REVOKE')) + ',' + 2;
                ReqBody.openApiKeyId = rowData?.openApiKeyId;
                break;
        }
        this.openApiService.writeOpenApiKeyReq(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                switch (applyCategory) {
                    case 'create':
                        this.currentTitle = this.applyOpenApiKeyTitle;
                        break;
                    case 'update':
                        this.currentTitle = this.updateOpenApiKeyTitle;
                        break;
                    case 'revoke':
                        this.currentTitle = this.revokeOpenApiKeyTitle;
                        break;
                }
                this.pageNum = 4;
                this.applyOpenApiKeyDataParams = {
                    operate: FormOperate.create,
                    data: res.RespBody
                }
            }
        });
    }

    queryOpenApiKeyDetail(rowData: DPB0090RespItem) {
        this.openApiKeyDetailData = {} as DPB0091Resp;
        let ReqBody = {
            openApiKeyId: rowData.openApiKeyId
        } as DPB0091Req;
        this.openApiService.queryOpenApiKeyDetailByPk(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.openApiKeyDetailData = res.RespBody;
                this.openApiKeyDetailRowcount = this.openApiKeyDetailData.dataList.length;
                this.currentTitle = this.openApiKeyDetailTitle;
                this.pageNum = 3;
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

    async refreshData(event: boolean) {
        const code = ['message.save', 'message.requisition.open_api_key', 'message.success'];
        const dict = await this.tool.getDict(code);
        this.message.add({ severity: 'success', summary: `${dict['message.save']} ${dict['message.requisition.open_api_key']}`, detail: `${dict['message.save']} ${dict['message.success']}!` });
        this.viewClientOpenApiList(this.currentClient!);
    }

    changePage(action: string, clearFlag: boolean) {
        switch (action) {
            case 'queryClientList':
                this.currentTitle = this.title;
                this.pageNum = 1;
                break;
            case 'viewClientOpenApiList':
                this.currentTitle = this.openApiKeyListTitle;
                this.pageNum = 2;
                break;
        }
        if (clearFlag) {
            this._openApiKeyForm.clearData();
        }
    }

    headerReturn() {
      if(this.pageNum == 3)
      {
        this.changePage('viewClientOpenApiList', false)
      }
      else if(this.pageNum == 4) {
        this.changePage('viewClientOpenApiList', true)
      }
      else
        this.changePage('queryClientList', false)
    }

    public get keyword() { return this.form.get('keyword'); };

}
