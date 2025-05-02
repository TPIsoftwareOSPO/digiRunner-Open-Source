import { Component, OnInit } from '@angular/core';
import { DPB0072RespItem, DPB0072Req } from 'src/app/models/api/ApiSignOffService/dpb0072.interface';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from '../../../shared/validator-functions';
import { ApiSignOffService } from 'src/app/shared/services/api-api-sign-off.service';
import * as dayjs from 'dayjs';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { DPB0073Req } from 'src/app/models/api/ApiSignOffService/dpb0073.interface';
import { MessageService } from 'primeng/api';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';

@Component({
    selector: 'app-np0512',
    templateUrl: './np0512.component.html',
    styleUrls: ['./np0512.component.css'],
    providers: [MessageService]
})
export class Np0512Component extends BaseComponent implements OnInit {

    form: FormGroup;
    dataList: Array<DPB0072RespItem> = [];
    cols: { field: string; header: string }[] = [];
    rowcount: number = 0;
    apiAuthorities: { label: string; value: string }[] = [];
    selectedApis: Array<DPB0072RespItem> = new Array<DPB0072RespItem>();
    orgFlagType: { label: string; value: string; }[] = [];
    canSetting: boolean = true;

    constructor(
         route: ActivatedRoute,
         tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private signOffService: ApiSignOffService,
        private message: MessageService,
        private file: FileService,
        private list: ListService
    ) {
        super(route, tr);

        this.form = this.fb.group({
          keyword: new FormControl(''),
          startDate: new FormControl('', ValidatorFns.requiredValidator()),
          endDate: new FormControl('', ValidatorFns.requiredValidator()),
          orgFlag: new FormControl('0')
      });
    }

    async ngOnInit() {

        this.converDateInit();
        const codes = ['api_name', 'api_desc', 'theme_name', 'api_doc', 'public_flag', 'dept', 'shelves_date', 'org_query.basic_on_org', 'org_query.non_basic_on_org'];
        const dict = await this.tool.getDict(codes);
        this.cols = [
            { field: 'apiName', header: dict['api_name'] },
            { field: 'apiDesc', header: dict['api_desc'] },
            { field: 'themeName', header: dict['theme_name'] },
            { field: 'fileName', header: dict['api_doc'] },
            { field: 'publicFlagName', header: dict['public_flag'] },
            { field: 'orgName', header: dict['dept'] },
            { field: 'dpStuDateTime', header: dict['shelves_date'] }
        ];
        this.orgFlagType = [
            { label: dict['org_query.basic_on_org'], value: '0' },
            { label: dict['org_query.non_basic_on_org'], value: '1' }
        ];
        let ReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_AUTHORITY')) + ',' + 7,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _subTypes: {label:string, value:string}[] = [];
                res.RespBody.subItems?.map(item => {
                    if (item.subitemNo != '-1') {
                        _subTypes.push({ label: item.subitemName, value: item.subitemNo });
                    }
                });
                this.apiAuthorities = _subTypes;
            }
        });
        if (this.orgFlag!.value == '0') {
            this.canSetting = true;
        }
        else {
            this.canSetting = false;
        }
        this.dataList = [];
        this.selectedApis = [];
        let queryReqBody = {
            keyword: this.keyword!.value,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            orgFlagEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.orgFlag!.value)) + ',' + this.orgFlag!.value
        } as DPB0072Req;
        this.signOffService.queryApiDpStatusLikeList_ignore1298(queryReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    converDateInit() {
        let date = new Date();
        this.startDate!.setValue(this.tool.addDay(date, -6));
        this.endDate!.setValue(date);
    }

    submitForm() {
        if (this.orgFlag!.value == '0') {
            this.canSetting = true;
        }
        else {
            this.canSetting = false;
        }
        this.dataList = [];
        this.selectedApis = [];
        let ReqBody = {
            keyword: this.keyword!.value,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            orgFlagEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.orgFlag!.value)) + ',' + this.orgFlag!.value
        } as DPB0072Req;
        this.signOffService.queryApiDpStatusLikeList(ReqBody).subscribe(res => {
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
            keyword: this.keyword!.value,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            orgFlagEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.orgFlag!.value)) + ',' + this.orgFlag!.value
        } as DPB0072Req;
        this.signOffService.queryApiDpStatusLikeList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    settingPublicStatus(publicFlag: string) {
        let _apiPKs:any[] = [];
        this.selectedApis.map(api => {
            _apiPKs.push({ apiKey: api.apiKey, moduleName: api.moduleName });
        });
        let ReqBody = {
            encodePublicFlag: this.tool.Base64Encoder(this.tool.BcryptEncoder(publicFlag)) + ',' + parseInt(publicFlag),
            apiPKs: _apiPKs
        } as DPB0073Req;
        this.signOffService.setApiPublicFlag(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.setting', 'public_flag', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dicts['message.setting']} ${dicts['public_flag']}`, detail: `${dicts['message.setting']} ${dicts['message.success']}!` });
                this.submitForm();
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

    fileNameConvert(fileName: string): string {
        if (fileName) {
            return decodeURIComponent(fileName);
        }
        return '';
    }

    public get keyword() { return this.form.get("keyword"); }
    public get startDate() { return this.form.get("startDate"); }
    public get endDate() { return this.form.get("endDate"); }
    public get orgFlag() { return this.form.get("orgFlag"); }

}
