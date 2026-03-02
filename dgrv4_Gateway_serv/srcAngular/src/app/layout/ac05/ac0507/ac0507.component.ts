import { UtilService } from 'src/app/shared/services/api-util.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { ExportService } from 'src/app/shared/services/export.service';
import { AA0507AuditLog, AA0507Req } from 'src/app/models/api/UtilService/aa0507.interface';
import { AA0301Item, AA0301Req } from 'src/app/models/api/ApiService/aa0301_v3.interfcae';
import * as dayjs from 'dayjs';
import { MessageService } from 'primeng/api';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
    selector: 'app-ac0507',
    templateUrl: './ac0507.component.html',
    styleUrls: ['./ac0507.component.css'],
    providers: [UtilService, ToolService, ApiService]
})
export class Ac0507Component extends BaseComponent implements OnInit {

    form: FormGroup;
    rowcount: number = 0;
    auditLogList: Array<AA0507AuditLog> = [];
    cols: { field: string; header: string }[] = [];
    apiListCols: { field: string; header: string }[] = [];
    selectedColumns: { field: string; header: string; }[] = [];
    pageNum: number = 1;
    currentTitle: string = this.title;
    apiForm: FormGroup;
    selectedApi?: AA0301Item;
    apiList: Array<AA0301Item> = [];
    apiListRowcount: number = 0; // 1: 查詢稽核日誌、2: API清單
    collectionDate: string = '';
    auditLogDetail?: AA0507AuditLog;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private util: UtilService,
        private exportService: ExportService,
        private apiService: ApiService,
        private messageService: MessageService,
        private ngxService: NgxUiLoaderService
    ) {
        super(route, tr);

        const midnight = new Date((new Date).setHours(0, 0, 0, 0));
        this.form = this.fb.group({
            timeStart: new FormControl(midnight),
            timeEnd: new FormControl(new Date),
            txID: new FormControl(''),
            keyword: new FormControl(''),
            apiAlias: new FormControl({ value: '', disabled: true })
        });
        this.apiForm = this.fb.group({
            keyword: new FormControl('')
        });
    }

    ngOnInit() {

        this.init();
    }

    async init() {
        const codes = ["txsn_api", "audit_type", 'txid', "m_type", "tx_date", "client_id", "r_code", "r_msg", "m_body", "user", "cip", 'module_name', 'api_name', 'api_key', 'timestamp'];
        const dict = await this.tool.getDict(codes);
        this.cols = [
            { field: 'txsn', header: dict['txsn_api'] },
            { field: 'type', header: dict['audit_type'] },
            { field: 'txId', header: dict['txid'] },
            { field: 'mType', header: dict['m_type'] },
            { field: 'txDate', header: dict['tx_date'] },
            { field: 'cId', header: dict['client_id'] },
            { field: 'rCode', header: dict['r_code'] },
            { field: 'rMsg', header: dict['r_msg'] },
            { field: 'user', header: dict['user'] },
            { field: 'cip', header: dict['cip'] },
            { field: 'ts', header: dict['timestamp'] }
        ];
        this.selectedColumns = this.cols;
        // API列表 cols
        this.apiListCols = [
            { field: 'moduleName', header: dict['module_name'] },
            { field: 'apiName', header: dict['api_name'] },
            { field: 'apiKey', header: dict['api_key'] }
        ];
        this.ngxService.start();
        this.auditLogList = [];
        this.rowcount = this.auditLogList.length;
        let ReqBody = {
            from: 0,
            timeStart: dayjs(this.timeStart!.value).format('YYYY-MM-DD HH:mm'),
            timeEnd: dayjs(this.timeEnd!.value).format('YYYY-MM-DD HH:mm'),
            txID: this.txID!.value,
            keyword: this.keyword!.value
        } as AA0507Req;
        this.util.queryAuditLog_ignore1298(ReqBody).subscribe(res => {
            this.ngxService.stop();
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.collectionDate = res.RespBody.collectionDate;
                this.auditLogList = res.RespBody.auditLogList;
                this.rowcount = this.auditLogList.length;
            }
        });
    }

    queryApiList() {
        this.selectedApi = {} as AA0301Item;
        this.apiList = [];
        this.apiListRowcount = this.apiList.length;
        let ReqBody = {
            keyword: this.a_keyword!.value,
            apiSrc: [],
            paging: 'Y'
        } as AA0301Req;
        this.apiService.queryAPIList_v3(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.apiList = res.RespBody.dataList;
                this.apiListRowcount = this.apiList.length;
            }
        });
    }

    moreApiList() {
        let _moduleName = this.apiList[this.apiList.length - 1].moduleName;
        let _apiKey = this.apiList[this.apiList.length - 1].apiKey;
        let ReqBody = {
            moduleName: _moduleName.t ? _moduleName.ori : _moduleName.val,
            apiKey: _apiKey.t ? _apiKey.ori : _apiKey.val,
            keyword: this.a_keyword!.value,
            apiSrc: [],
            paging: 'Y'
        } as AA0301Req;
        this.apiService.queryAPIList_v3(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.apiList = this.apiList.concat(res.RespBody.dataList);
                this.apiListRowcount = this.apiList.length;
            }
        });
    }

    submitForm() {
        this.ngxService.start();
        this.auditLogList = [];
        this.rowcount = this.auditLogList.length;
        let ReqBody = {
            from: 0,
            timeStart: dayjs(this.timeStart!.value).format('YYYY-MM-DD HH:mm'),
            timeEnd: dayjs(this.timeEnd!.value).format('YYYY-MM-DD HH:mm'),
            txID: this.txID!.value,
            keyword: this.keyword!.value
        } as AA0507Req;
        this.util.queryAuditLog(ReqBody).subscribe(res => {
            this.ngxService.stop();
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.collectionDate = res.RespBody.collectionDate;
                this.auditLogList = res.RespBody.auditLogList;
                this.rowcount = this.auditLogList.length;
            }
        });
    }

    moreAuditLogData() {
        this.ngxService.start();
        let ReqBody = {
            from: this.auditLogList.length,
            collectionDate: this.collectionDate,
            id: this.auditLogList[this.auditLogList.length - 1].id,
            timeStart: dayjs(this.timeStart!.value).format('YYYY-MM-DD HH:mm'),
            timeEnd: dayjs(this.timeEnd!.value).format('YYYY-MM-DD HH:mm'),
            txID: this.txID!.value,
            keyword: this.keyword!.value
        } as AA0507Req;
        this.util.queryAuditLog(ReqBody).subscribe(res => {
            this.ngxService.stop();
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.collectionDate = res.RespBody.collectionDate;
                this.auditLogList = this.auditLogList.concat(res.RespBody.auditLogList);
                this.rowcount = this.auditLogList.length;
            }
        });
    }

    chooseApi() {
        if (JSON.stringify(this.selectedApi) != '{}') {
            this.apiAlias!.setValue(`${this.selectedApi?.moduleName.t ? this.selectedApi.moduleName.ori : this.selectedApi?.moduleName.val}-${this.selectedApi?.apiName.t ? this.selectedApi?.apiName.ori : this.selectedApi?.apiName.val}(${this.selectedApi?.apiKey.t ? this.selectedApi?.apiKey.ori : this.selectedApi?.apiKey.val})`);
            this.txID!.setValue(this.selectedApi?.apiKey.t ? this.selectedApi?.apiKey.ori : this.selectedApi?.apiKey.val);
            this.changePage('query');
        }
    }

    cancelApi() {
        this.apiAlias!.setValue('');
        this.txID!.setValue('');
        this.changePage('query');
    }

    exportFile(isExcel: boolean): void {
        this.exportService.exportFile(this.auditLogList, 'AuditLog', isExcel);
    }

    async copyData(data: string) {
        const code = ['copy', 'data', 'message.success'];
        const dict = await this.tool.getDict(code);
        let selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = data;
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
        this.messageService.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
    }

    async changePage(action: string, rowData?: AA0507AuditLog) {
        const code = ['api_list', 'button.detail'];
        const dict = await this.tool.getDict(code);
        switch (action) {
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                break;
            case 'api_list':
                this.currentTitle = `${this.title} > ${dict['api_list']}`;
                this.pageNum = 2;
                this.queryApiList();
                break;
            case 'detail':
                this.currentTitle = `${this.title} > ${dict['button.detail']}`;
                this.pageNum = 3;
                this.auditLogDetail = rowData;
                break;
        }
    }

    public get timeStart() { return this.form.get('timeStart'); }
    public get timeEnd() { return this.form.get('timeEnd'); }
    public get apiAlias() { return this.form.get('apiAlias'); }
    public get txID() { return this.form.get('txID'); }
    public get keyword() { return this.form.get('keyword'); }
    public get a_keyword() { return this.apiForm.get('keyword'); }
}
