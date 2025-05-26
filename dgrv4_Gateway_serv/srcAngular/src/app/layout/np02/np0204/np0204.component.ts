import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0083RespItem, DPB0083Req } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { ClientCAService } from 'src/app/shared/services/api-certificate-authority.service';
import { DPB0084Req, DPB0084certItem } from 'src/app/models/api/CertificateAuthorityService/dpb0084.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { ClientService } from 'src/app/shared/services/api-client.service';

@Component({
    selector: 'app-np0204',
    templateUrl: './np0204.component.html',
    styleUrls: ['./np0204.component.css'],
    providers: [ClientService]

})
export class Np0204Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    form: FormGroup;
    dialogTitle: string = '';
    cols: { field: string; header: string; }[] = [];
    dataList: Array<DPB0083RespItem> = new Array<DPB0083RespItem>();
    rowcount: number = 0;
    formOperate = FormOperate;
    emPageBlock = EmPageBlock;
    pageNum = EmPageBlock.QUERYPAGE;
    clientData?: FormParams;
    caData?: DPB0084certItem;
    cusTitle: string[] = [];
    currentTitle = this.title;
    isCusEnable: string = 'N';

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private clientCA: ClientCAService,
        private clientService: ClientService
    ) {
        super(route, tr);
        this.form = this.fb.group({
          keyword: new FormControl('')
      });
    }

    ngOnInit() {

        this.init();
    }

    async init() {
        const codes = ['client_id', 'client_name', 'client_alias','client_cert_list','cert_detail'];
        const dict = await this.tool.getDict(codes);
        this.cusTitle = [
            this.currentTitle,
            `${this.currentTitle} > ${dict['client_cert_list']}`,
            `${this.currentTitle} > ${dict['client_cert_list']} > ${dict['cert_detail']}`
        ]
        this.cols = [
            { field: 'clientId', header: dict['client_id'] },
            { field: 'clientName', header: dict['client_name'] },
            { field: 'clientAlias', header: dict['client_alias'] }
        ];
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as DPB0083Req;
        this.clientService.queryClientListByLike_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    submitForm() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as DPB0083Req;
        this.clientService.queryClientListByLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    async showDialog(rowData: DPB0083RespItem) {
        const codes = ['client_cert_list'];
        const dicts = await this.tool.getDict(codes);
        let ReqBody = {
            clientId: rowData.clientId,
            encodeCertType: this.tool.Base64Encoder(this.tool.BcryptEncoder('TLS')) + ',' + 1
        } as DPB0084Req;
        // this.clientCA.queryClientByCid(ReqBody).subscribe(res => {
        //     if (this.tool.checkDpSuccess(res.ResHeader)) {
        //         this.dialogTitle = dicts['dialog.detail_query'];
        //         let data: FormParams = {
        //             data: { type: 'TLS', clientId: rowData.clientId, detail: res.RespBody.certList },
        //             displayInDialog: true
        //         }
        //         this._dialog.open(ClientCAComponent, data);
        //     }
        //     else {
        //         this.dialogTitle = dicts['dialog.detail_query'];
        //         let data: FormParams = {
        //             data: { type: 'TLS', clientId: rowData.clientId, detail: new Array<DPB0084certItem>() },
        //             displayInDialog: true
        //         }
        //         this._dialog.open(ClientCAComponent, data);
        //     }
        // });
        this.pageNum = this.emPageBlock.CLIENT_DETAIL;
        this.currentTitle = this.cusTitle[this.pageNum];
        // this.dialogTitle = dicts['dialog.detail_query'];
        let data: FormParams = {
            data: { type: 'TLS', clientId: rowData.clientId, rowData: rowData,ReqBody:ReqBody },
            displayInDialog: true
        }
        this.clientData = data;
    }

    redirectPrev() {
        this.pageNum = this.emPageBlock.CLIENT_DETAIL;
        this.currentTitle = this.cusTitle[this.pageNum];
    }
    redirectList() {
        this.pageNum = this.emPageBlock.QUERYPAGE;
        this.currentTitle = this.cusTitle[this.pageNum];
    }

    headerReturn() {
        // console.log(this.pageNum === EmPageBlock.CLIENT_DETAIL)
        if(this.pageNum === EmPageBlock.CLIENT_DETAIL)
        {
          this.redirectList();
        }
        else if(this.pageNum === EmPageBlock.CA_DETAIL)
        {
          this.redirectPrev();
        }
      }

      rowDataHandler(res: DPB0084certItem) {
        this.pageNum = this.emPageBlock.CA_DETAIL;
        this.currentTitle = this.cusTitle[this.pageNum];
        this.caData = res;
    }


    moreData() {
        let ReqBody = {
            clientId: this.dataList[this.dataList.length - 1].clientId,
            keyword: this.keyword!.value
        } as DPB0083Req;
        this.clientService.queryClientListByLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }





    public get keyword() { return this.form.get('keyword'); };

}

enum EmPageBlock {
    QUERYPAGE, CLIENT_DETAIL, CA_DETAIL
}
