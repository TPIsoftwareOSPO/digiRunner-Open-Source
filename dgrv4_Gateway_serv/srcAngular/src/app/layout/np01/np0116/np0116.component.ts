import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { AnnouncementComponent } from './announcement/announcement.component';
import { DPB0044NewsItem, DPB0044Req } from 'src/app/models/api/NewsService/dpb0044.interface';
import { NewsService } from 'src/app/shared/services/api-news.service';
import { DPB0043Req } from 'src/app/models/api/NewsService/dpb0043.interface';
import { DPB0045Req } from 'src/app/models/api/NewsService/dpb0045.interface';
import { DPB0046Req } from 'src/app/models/api/NewsService/dpb0046.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import * as ValidatorFns from '../../../shared/validator-functions';
import * as dayjs from 'dayjs';
import { StringLengthPipe } from 'src/app/shared/pipes/string-length.pipe';

@Component({
    selector: 'app-np0116',
    templateUrl: './np0116.component.html',
    styleUrls: ['./np0116.component.css'],
    providers: [MessageService, StringLengthPipe, ConfirmationService]
})
export class Np0116Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    form!: FormGroup;
    formOperate = FormOperate;
    cols: { field: string; header: string; type?: StringLengthPipe; }[] = [];
    rowcount: number = 0;
    dialogTitle: string = '';
    dataList: Array<DPB0044NewsItem> = new Array();
    delList: Array<number> = new Array();
    loading: boolean = false;
    newsItemData?: DPB0044NewsItem;
    types: { label: string; value: string; }[] = [];
    status: { label: string; value: string; }[] = [];

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private message: MessageService,
        private tool: ToolService,
        private news: NewsService,
        private listService: ListService,
        private string_length_pipe: StringLengthPipe,
        private confirmationService:ConfirmationService
    ) {
        super(route, tr);
    }

    async ngOnInit() {
        this.form = this.fb.group({
            queryStartDate: new FormControl('', ValidatorFns.requiredValidator()),
            queryEndDate: new FormControl('', ValidatorFns.requiredValidator()),
            typeItemNo: new FormControl(''),
            keyword: new FormControl(''),
            enFlagEncode: new FormControl('1', ValidatorFns.requiredValidator())
        });
        this.converDateInit();
        const codes = ['news_id', 'news_title', 'news_content', 'news_post_org', 'news_post_date', 'news_type_itme'];
        const dict = await this.tool.getDict(codes);
        this.cols = [
            { field: 'newsId', header: dict['news_id'] },
            { field: 'newTitle', header: dict['news_title'] },
            { field: 'newContentstr', header: dict['news_content'], type: this.string_length_pipe },
            { field: 'orgName', header: dict['news_post_org'] },
            { field: 'postDateTime', header: dict['news_post_date'] },
            { field: 'typeItemNoName', header: dict['news_type_itme'] }
        ];
        let typeReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('NEWS_TYPE')) + ',' + 2,
            isDefault: 'N'
        } as DPB0047Req;
        this.listService.querySubItemsByItemNo(typeReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _types:{ label:string, value:string }[] = [];
                res.RespBody.subItems?.map(item => {
                    if (item.subitemNo != '2') {
                        _types.push({ label: item.subitemName, value: item.subitemNo })
                    }
                });
                this.types = _types;
            }
        });
        let flagReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('ENABLE_FLAG')) + ',' + 9,
            isDefault: 'N'
        } as DPB0047Req;
        this.listService.queryNewsStatusList(flagReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                res.RespBody.subItems?.map(item => {
                    this.status.push({ label: item.subitemName, value: item.subitemNo })
                });
            }
        });
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.form.get('keyword')!.value,
            queryStartDate: dayjs(this.form.get('queryStartDate')!.value).format('YYYY/MM/DD'),
            queryEndDate: dayjs(this.form.get('queryEndDate')!.value).format('YYYY/MM/DD'),
            typeItemNo: this.form.get('typeItemNo')!.value,
            enFlagEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.form.get('enFlagEncode')!.value)) + ',' + this.convertStatusIndex(),
            fbTypeEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder('BACK')) + ',' + 1
        } as DPB0044Req;
        this.news.queryNewsLike_v3_4_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                res.RespBody.dataList.map(data => {
                    data.newContentstr = this.newContentTransform(data.newContent);
                });
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    converDateInit() {
        let date = new Date();
        // let startDate = new Date();
        // startDate.setHours(0, 0);
        this.queryStartDate!.setValue(this.tool.addMonth(date, -1));
        // let endDate = new Date();
        // endDate.setHours(23, 59);
        this.queryEndDate!.setValue(date);
    }

    async create() {
        const codes = ['message.create', 'message.news', 'message.success'];
        const dicts = await this.tool.getDict(codes);
        this.dialogTitle = dicts['message.create'];
        let data: FormParams = {
            operate: FormOperate.create,
            displayInDialog: true,
            afterCloseCallback: (r) => {
                if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                    this.submitForm();
                    this.message.add({ severity: 'success', summary: `${dicts['message.create']} ${dicts['message.news']}`, detail: `${dicts['message.create']} ${dicts['message.success']}!` });
                }
            }
        }
        this._dialog.open(AnnouncementComponent, data);
    }

    submitForm() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.form.get('keyword')!.value,
            queryStartDate: dayjs(this.form.get('queryStartDate')!.value).format('YYYY/MM/DD'),
            queryEndDate: dayjs(this.form.get('queryEndDate')!.value).format('YYYY/MM/DD'),
            typeItemNo: this.form.get('typeItemNo')!.value,
            enFlagEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.form.get('enFlagEncode')!.value)) + ',' + this.convertStatusIndex(),
            fbTypeEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder('BACK')) + ',' + 1
        } as DPB0044Req;
        this.news.queryNewsLike_v3_4(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                res.RespBody.dataList.map(data => {
                    data.newContentstr = this.newContentTransform(data.newContent);
                });
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    async showDialog(rowData: DPB0044NewsItem, operation: FormOperate) {
        const codes = ['dialog.update', 'active', 'current_status', 'message.update', 'message.news', 'message.success'];
        const dicts = await this.tool.getDict(codes);
        switch (operation) {
            case FormOperate.update:
                this.dialogTitle = dicts['dialog.update'];
                let ReqBody = {
                    newsId: rowData.newsId,
                    fbTypeEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder('BACK')) + ',' + 1
                } as DPB0045Req;
                this.news.queryNewsById(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        let data: FormParams = {
                            operate: FormOperate.update,
                            data: res.RespBody,
                            displayInDialog: true,
                            afterCloseCallback: (r) => {
                                if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                                    this.submitForm();
                                    this.message.add({ severity: 'success', summary: `${dicts['message.update']} ${dicts['message.news']}`, detail: `${dicts['message.update']} ${dicts['message.success']}!` });
                                }
                            }
                        }
                        this._dialog.open(AnnouncementComponent, data)
                    }
                });
                break;
            case FormOperate.switch:
                this.newsItemData = rowData;
                this.message.clear();
                let action = dicts['active'];
                let dict = await this.tool.getDict(['cfm_news'], { value: action });
                // this.message.add({ key: 'confirm', sticky: true, severity: 'info', summary: dict['cfm_news'], detail: `${dicts['current_status']}：${rowData.statusName}` });

                this.confirmationService.confirm({
                  header: dict['cfm_news'],
                  message: `${dicts['current_status']}：${rowData.statusName}`,
                  accept: () => {
                      this.onSwitchConfirm();
                  }
                });
                break;
        }
    }

    moreData() {
        this.loading = true;
        let ReqBody = {
            newsId: this.dataList[this.dataList.length - 1].newsId,
            keyword: this.form.get('keyword')!.value,
            queryStartDate: dayjs(this.form.get('queryStartDate')!.value).format('YYYY/MM/DD'),
            queryEndDate: dayjs(this.form.get('queryEndDate')!.value).format('YYYY/MM/DD'),
            typeItemNo: this.form.get('typeItemNo')!.value,
            enFlagEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.form.get('enFlagEncode')!.value)) + ',' + this.convertStatusIndex(),
            fbTypeEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder('BACK')) + ',' + 1
        } as DPB0044Req;
        this.news.queryNewsLike_v3_4(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.loading = false;
                res.RespBody.dataList.map(data => {
                    data.newContentstr = this.newContentTransform(data.newContent);
                });
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
            else {
                this.loading = false;
            }
        });
    }

    convertStatusIndex(): number {
        switch (this.form.get('enFlagEncode')!.value) {
            case '1':
                return 0;
            case '0':
                return 1;
            case '-1':
                return 2;
            default:
              return -1;
        }
    }

    newContentTransform(content: string) {
        return decodeURIComponent(content);
    }

    onSwitchConfirm() {
        this.message.clear();
        let ReqBody = {
            newsId: this.newsItemData?.newsId,
            lv: this.newsItemData?.lv,
            // statusEncode: this.newsItemData.status === '1' ? this.tool.Base64Encoder(this.tool.BcryptEncoder('0')) + ',' + 1 : this.tool.Base64Encoder(this.tool.BcryptEncoder('1')) + ',' + 0,
            statusEncode: this.tool.Base64Encoder(this.tool.BcryptEncoder('1')) + ',' + 0,
            postDateTime: dayjs(this.newsItemData?.postDateTime).format('YYYY/MM/DD')
        } as DPB0043Req;
        this.news.updateNews(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.update', 'message.status', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dicts['message.update']} ${dicts['message.status']}`, detail: `${dicts['message.update']} ${dicts['message.success']}!` });
                this.submitForm();
            }
        });
    }

    async delete(rowData: DPB0044NewsItem) {
        this.newsItemData = rowData;
        const code = ['inactive', 'current_status'];
        const dict = await this.tool.getDict(code);
        let action = dict['inactive'];
        let staValue = await this.tool.getDict(['cfm_news'], { value: action });
        this.message.clear();
        // this.message.add({ key: 'delete', sticky: true, severity: 'warn', summary: staValue['cfm_news'], detail: `${dict['current_status']}：${this.newsItemData.statusName}` });
        this.confirmationService.confirm({
          header: staValue['cfm_news'],
          message: `${dict['current_status']}：${this.newsItemData.statusName}`,
          accept: () => {
              this.onDeleteConfirm();
          }
        });
    }

    onDeleteConfirm() {
        this.message.clear();
        let ReqBody = {
            delList: [this.newsItemData?.newsId]
        } as DPB0046Req;
        this.news.deleteNews_v3_4(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.update', 'message.status', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dicts['message.update']} ${dicts['message.status']}`, detail: `${dicts['message.update']} ${dicts['message.success']}!` });
                this.submitForm();
            }
        });
    }

    onReject() {
        this.message.clear();
    }

    async copyNewContent(newContent: string) {
        const code = ['copy', 'data', 'message.success'];
        const dict = await this.tool.getDict(code);
        let selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = this.newContentTransform(newContent);
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
        this.message.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
    }

    public get queryStartDate() { return this.form.get('queryStartDate'); };
    public get queryEndDate() { return this.form.get('queryEndDate'); };
    public get enFlagEncode() { return this.form.get('enFlagEncode'); };

}
