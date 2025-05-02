import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { DPB0068D2, DPB0068D1 } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from '../../../../models/common.enum';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0069Items, DPB0069Req } from 'src/app/models/api/RequisitionService/dpb0069.interface';
import { RequisitionService } from 'src/app/shared/services/api-requisition.service';

@Component({
    selector: 'app-requisition-form',
    templateUrl: './requisition-form.component.html',
    styleUrls: ['./requisition-form.component.css']
})
export class RequisitionFormComponent implements OnInit {

    @Input() data!: FormParams;
    @Output() change: EventEmitter<string> = new EventEmitter();

    detailCols: { field: string; header: string; width?: string; }[] = [];
    trakerCols: { field: string; header: string; width?: string; }[] = [];
    detailDataList: Array<DPB0068D2> | Array<DPB0068D1> = [];
    trakerDataList: Array<DPB0069Items> = [];
    formOperate = FormOperate;
    form!: FormGroup;
    rowcount: number = 0;

    constructor(
        private fb: FormBuilder,
        private file: FileService,
        private tool: ToolService,
        private requisition: RequisitionService
    ) { }

    async ngOnInit() {
        this.form = this.fb.group({
            reqComment: new FormControl('')
        });
        const code = ['review_update_date', 'review_update_user', 'sign_off_layer', 'sign_off_status', 'sign_off_comment', 'api_name', 'api_desc', 'public_flag', 'dept', 'theme_name', 'api_doc'];
        const dict = await this.tool.getDict(code);
        this.trakerCols = [
            { field: 'chkCreateDateTime', header: dict['review_update_date'] },
            { field: 'chkCreateUser', header: dict['review_update_user'] },
            { field: 'chkLayerName', header: dict['sign_off_layer'] },
            { field: 'reviewStatusName', header: dict['sign_off_status'] },
            { field: 'reqComment', header: dict['sign_off_comment'] }
        ];
        switch (this.data.data.detailData.reqType) {
            case 'API_ON_OFF':
                this.detailCols = [
                    { field: 'apiName', header: dict['api_name'] },
                    { field: 'apiDesc', header: dict['api_desc'] },
                    { field: 'publicFlagName', header: dict['public_flag'] },
                    { field: 'orgName', header: dict['dept'] },
                    // { field: 'themeList', header: dict['theme_name'] },
                    // { field: 'docFileInfo', header: dict['api_doc'] }
                ];
                this.detailDataList = this.data.data.detailData.apiOnOff.apiOnOffList;
                break;
            case 'API_APPLICATION':
                this.detailCols = [
                    { field: 'apiName', header: dict['api_name'] },
                    { field: 'apiDesc', header: dict['api_desc'] },
                    { field: 'publicFlagName', header: dict['public_flag'] },
                    { field: 'orgName', header: dict['dept'] },
                    // { field: 'themeList', header: dict['theme_name'] },
                    // { field: 'docFileInfo', header: dict['api_doc'] }
                ];
                this.detailDataList = this.data.data.detailData.apiUserApply.apiList;
                break;
            case 'OPEN_API_KEY':
                this.detailCols = [
                    { field: 'apiName', header: dict['api_name'] },
                    { field: 'apiDesc', header: dict['api_desc'] },
                    { field: 'orgName', header: dict['dept'] },
                    // { field: 'themeList', header: dict['theme_name'] },
                    // { field: 'docFileInfo', header: dict['api_doc'] }
                ];
                this.detailDataList = this.data.data.detailData.openApiKey.apiDatas;
                break;
            default:
                this.detailDataList = [];
                break;
        }
        if (this.data.operate == FormOperate.traker) {
            this.trakerDataList = this.data.data.trakerData.dataList;
            this.rowcount = this.trakerDataList.length;
        }
        this.form.valueChanges.subscribe(res => {
            this.change.emit(res.reqComment);
        });
    }

    checkLength(obj: object, index: number): boolean {
        if (Object.keys(obj).length > 1 && (index + 1) != Object.keys(obj).length)
            return true;
        else
            return false;
    }

    moreData() {
        let ReqBody = {
            reqOrdermId: this.data.data.trakerData.reqOrdermId,
            chkLogId: this.trakerDataList[this.trakerDataList.length - 1].chkLogId
        } as DPB0069Req;
        this.requisition.queryHistoryByPk(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.trakerDataList = this.trakerDataList.concat(res.RespBody.dataList);
                this.rowcount = this.trakerDataList.length;
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

}
