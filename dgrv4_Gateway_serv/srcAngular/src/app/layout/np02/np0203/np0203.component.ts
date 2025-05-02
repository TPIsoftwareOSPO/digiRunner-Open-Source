import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DPB0088certItem, DPB0088Req } from 'src/app/models/api/CertificateAuthorityService/dpb0088.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as dayjs from 'dayjs';
import { ClientCAService } from 'src/app/shared/services/api-certificate-authority.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0087Req } from 'src/app/models/api/CertificateAuthorityService/dpb0087.interface';
import { DPB0089Req } from 'src/app/models/api/CertificateAuthorityService/dpb0089.interface';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0086Req } from 'src/app/models/api/CertificateAuthorityService/dpb0086.interface';

@Component({
    selector: 'app-np0203',
    templateUrl: './np0203.component.html',
    styleUrls: ['./np0203.component.css'],
    providers: [ConfirmationService]

})
export class Np0203Component extends BaseComponent implements OnInit {

    form: FormGroup;
    cols: { field: string; header: string; }[] = [];
    certList: Array<DPB0088certItem> = new Array<DPB0088certItem>();
    selected: Array<DPB0088certItem> = new Array<DPB0088certItem>();
    currentDate = new Date();
    rowcount: number = 0;
    today: Date = new Date();
    delCert?: DPB0088certItem;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private clientCA: ClientCAService,
        private alert: AlertService,
        private message: MessageService,
        private confirmationService: ConfirmationService
    ) {
        super(route, tr);
        this.form = this.fb.group({
          startDate: new FormControl(''),
          endDate: new FormControl('')
      });
    }

    ngOnInit() {

        this.init();
    }

    async init() {
        this.converDateInit();
        const code = ['client_id', 'client_name', 'client_alias', 'ca_file_name', 'ca_version', 'ca_serial_number', 'signature_algorithm', 'encryption_algorithm', 'ca_create_time', 'ca_expired_time', 'ca_upload_date', 'ca_update_date'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'clientId', header: dict['client_id'] },
            { field: 'clientName', header: dict['client_name'] },
            { field: 'clientAlias', header: dict['client_alias'] },
            { field: 'certFileName', header: dict['ca_file_name'] },
            { field: 'certVersion', header: dict['ca_version'] },
            { field: 'createAt', header: dict['ca_create_time'] },
            { field: 'expiredAt', header: dict['ca_expired_time'] },
            { field: 'createDateTime', header: dict['ca_upload_date'] },
            { field: 'updateDateTime', header: dict['ca_update_date'] },
        ];
        this.certList = [];
        this.rowcount = this.certList.length;
        this.selected = [];
        let ReqBody = {
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
        } as DPB0088Req;
        this.clientCA.queryCaListByDate_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.certList = res.RespBody.certList;
                this.rowcount = this.certList.length;
            }
        });
    }

    converDateInit() {
        let date = new Date();
        this.startDate!.setValue(this.tool.addDay(date, -6));
        this.endDate!.setValue(date);
    }

    preMonth() {
        let sDate = new Date(this.currentDate);
        sDate.setMonth(sDate.getMonth() - 1);
        sDate.setDate(1);
        this.startDate!.setValue(sDate);
        let eDate = new Date(this.startDate!.value);
        eDate.setMonth(sDate.getMonth() + 1);
        eDate.setDate(eDate.getDate() - 1);
        this.endDate!.setValue(eDate);
        this.currentDate.setMonth(this.currentDate.getMonth() - 1);
        this.submitForm();
    }

    nextMonth() {
        let sDate = new Date(this.currentDate);
        sDate.setMonth(sDate.getMonth() + 1);
        sDate.setDate(1);
        this.startDate!.setValue(sDate);
        let eDate = new Date(this.startDate!.value);
        eDate.setMonth(sDate.getMonth() + 1);
        eDate.setDate(eDate.getDate() - 1);
        this.endDate!.setValue(eDate);
        this.currentDate.setMonth(this.currentDate.getMonth() + 1);
        this.submitForm();
    }

    highlight(value: string): string {
        if (this.today.getTime() - dayjs(value).toDate().getTime() > 2592000000) { // 超過30天
            return '#FF0000';
        }
        else if (this.today.getTime() - dayjs(value).toDate().getTime() <= 2592000000 && 0 <= this.today.getTime() - dayjs(value).toDate().getTime()) { // 30天內
            return '#FF8000';
        }
        else {
            return 'unset';
        }
    }

    submitForm() {
        this.certList = [];
        this.rowcount = this.certList.length;
        this.selected = [];
        let ReqBody = {
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
        } as DPB0088Req;
        this.clientCA.queryCaListByDate(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.certList = res.RespBody.certList;
                this.rowcount = this.certList.length;
            }
        });
    }

    moreData() {
        let ReqBody = {
            clientCert2Id: this.certList[this.certList.length - 1].clientCert2Id,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
        } as DPB0088Req;
        this.clientCA.queryCaListByDate(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.certList = this.certList.concat(res.RespBody.certList);
                this.rowcount = this.certList.length;
            }
        });
    }

    async downloadFile(mode: string, type: string, data: any) { // rowData: DPB0088certItem | selected: Array<DPB0088certItem>
        const code = ['download_fail', 'least_one', 'data'];
        const dict = await this.tool.getDict(code);
        if (Array.isArray(data)) {
            if (data.length == 0) {
                this.alert.ok(dict['download_fail'], `${dict['least_one']}${dict['data']}`, undefined);
                return;
            }
        }
        switch (type) {
            case 'zip':
                let zipReqBody = {
                    encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
                } as DPB0087Req;
                switch (mode) {
                    case 'multiple':
                        let _ids:any[] = [];
                        data.map(item => {
                            _ids.push(item.clientCert2Id);
                        });
                        zipReqBody.ids = _ids;
                        break;
                    case 'single':
                        zipReqBody.ids = [data.clientCert2Id];
                        break;
                }
                let zipFileName = 'pem.zip';
                this.clientCA.downLoadPEMFile(zipReqBody).subscribe(blob => {
                    const reader = new FileReader();
                    reader.onloadend = function () {
                        // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
                        //     window.navigator.msSaveBlob(blob, zipFileName);
                        // }
                        // else {
                            // const file = new File([blob], zipFileName, { type: blob.type });
                            const file = new File([blob], zipFileName);
                            const url = window.URL.createObjectURL(file);
                            const a = document.createElement('a');
                            document.body.appendChild(a);
                            a.setAttribute('style', 'display: none');
                            a.href = url;
                            a.download = zipFileName;
                            a.click();
                            window.URL.revokeObjectURL(url);
                            a.remove();
                        // }
                    }
                    reader.readAsText(blob);
                });
                break;
            case 'txt':
                let txtReqBody = {
                    encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
                } as DPB0089Req;
                switch (mode) {
                    case 'multiple':
                        let _ids:any[] = [];
                        data.map(item => {
                            _ids.push(item.clientCert2Id);
                        });
                        txtReqBody.ids = _ids;
                        break;
                    case 'single':
                        txtReqBody.ids = [data.clientCert2Id];
                        break;
                }
                let txtFileName = 'pem.txt';
                this.clientCA.returnTextFIle(txtReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
                        //     window.navigator.msSaveBlob(res.RespBody.fileContentStr, txtFileName);
                        // }
                        // else {
                            const blob = new Blob([res.RespBody.fileContentStr]);
                            const url = window.URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            document.body.appendChild(a);
                            a.setAttribute('style', 'display: none');
                            a.href = url;
                            a.download = txtFileName;
                            a.click();
                            window.URL.revokeObjectURL(url);
                            a.remove();
                        // }
                    }
                });
                break;
        }
    }

    async showDialog(rowData: DPB0088certItem) {
        const codes = ['dialog.detail_query', 'cfm_del','system_alert'];
        const dicts = await this.tool.getDict(codes);
        this.delCert = rowData;
        this.message.clear();
        // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: dicts['cfm_del'] });
        this.confirmationService.confirm({
          header: dicts['system_alert'],
          message: dicts['cfm_del'],
          accept: () => {
              this.onDeleteConfirm();
          }
        });
    }

    async onDeleteConfirm() {
        this.message.clear();
        let ReqBody = {
            clientId: this.delCert!.clientId,
            clientCert2Id: this.delCert!.clientCert2Id,
            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
        } as DPB0086Req;
        this.clientCA.deleteClientCA(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.delete', 'certificate_authority', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.clear();
                this.message.add({ severity: 'success', summary: `${dicts['message.delete']} ${dicts['certificate_authority']}`, detail: `${dicts['message.delete']} ${dicts['message.success']}!` });
                this.submitForm();
            }
        });
    }

    onReject() {
        this.message.clear();
    }

    public get startDate() { return this.form.get("startDate"); }
    public get endDate() { return this.form.get("endDate"); }

}
