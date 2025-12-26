import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { TranslateService } from '@ngx-translate/core';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0042Req } from 'src/app/models/api/NewsService/dpb0042.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { NewsService } from 'src/app/shared/services/api-news.service';
import { DPB0045Resp } from 'src/app/models/api/NewsService/dpb0045.interface';
import { DPB0043Req } from 'src/app/models/api/NewsService/dpb0043.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import * as dayjs from 'dayjs';

@Component({
    selector: 'app-announcement',
    templateUrl: './announcement.component.html',
    styleUrls: ['./announcement.component.css']
})
export class AnnouncementComponent implements OnInit {

    @Input() data!: FormParams;
    @Input() close!: Function;

    formOperate = FormOperate;
    form: FormGroup;
    submitBtnName: string = '';
    types: { label: string; value: string; }[] = [];
    minDateValue: Date = new Date();
    newTitleLimitChar = { value: 50 };
    newContentLimitChar = { value: 1024 };
    newContent: string = '';
    showMaxLengthError: boolean = false;
    canNotSend: boolean = true;

    constructor(
        private fb: FormBuilder,
        private translate: TranslateService,
        private tool: ToolService,
        private news: NewsService,
        private list: ListService
    ) {
        this.form = this.fb.group(this.resetFormGroup()!);
    }

    ngOnInit() {
        // this.minDateValue = this.tool.addDay(this.minDateValue, +1);
        let ReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('NEWS_TYPE')) + ',' + 2,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _types:{ label:string, value:string }[] = [];
                res.RespBody.subItems?.map(item => {
                    _types.push({ label: item.subitemName, value: item.subitemNo })
                });
                this.types = _types;
            }
        });
        const codes = ['button.create', 'button.update'];
        this.translate.get(codes).subscribe(i18n => this.init(i18n));
    }

    init(i18n) {
        switch (this.data && this.data.operate) {
            case FormOperate.create:
                this.canNotSend = true;
                this.submitBtnName = i18n['button.create'];
                this.form = this.fb.group(this.resetFormGroup(FormOperate.create)!);
                break;
            case FormOperate.update:
                this.canNotSend = false;
                this.submitBtnName = i18n['button.update'];
                this.form = this.fb.group(this.resetFormGroup(FormOperate.update)!);
                break;
        }
    }

    textChanged(event) {
        // console.log('html editor text change event :', event)
        // console.log('text length :', event.textValue.length)
        if (event.textValue.length > 2000) {
            this.canNotSend = true;
            this.showMaxLengthError = true;
        }
        else {
            if (event.textValue.length == 0) {
                this.canNotSend = true;
            }
            else {
                this.canNotSend = false;
            }
            this.showMaxLengthError = false;
        }
    }

    submitForm() {
        switch (this.data && this.data.operate) {
            case FormOperate.create:
                let ReqBody = {
                    newTitle: this.newTitle!.value,
                    newContent: encodeURIComponent(this.newContent),
                    postDateTime: dayjs(this.postDateTime!.value).format('YYYY/MM/DD'),
                    typeItemNo: this.typeItemNo!.value
                } as DPB0042Req;
                let createObservable = this.news.createNews_v3_4(ReqBody);
                this.form.reset();
                if (this.close) this.close(createObservable);
                break;
            case FormOperate.update:
                let updateReqBody = {
                    newsId: this.data.data.newsId,
                    lv: this.data.data.lv,
                    newTitle: this.newTitle!.value,
                    newContent: encodeURIComponent(this.newContent),
                    postDateTime: dayjs(this.postDateTime!.value).format('YYYY/MM/DD'),
                    typeItemNo: this.typeItemNo!.value
                } as DPB0043Req;
                let updateObservable = this.news.updateNews(updateReqBody);
                if (this.close) this.close(updateObservable);
                break;
        }
    }

    private resetFormGroup(formOperate?: FormOperate) {
        //初始化
        if (!formOperate) return {
            postDateTime: new FormControl('', ValidatorFns.requiredValidator()),
            // newContent: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringLengthValidator(this.newContentLimitChar.value)]),
            newTitle: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.newTitleLimitChar.value)]),
            typeItemNo: new FormControl('', ValidatorFns.requiredValidator())
        };
        switch (formOperate) {
            case FormOperate.create:
                return {
                    postDateTime: new FormControl('', ValidatorFns.requiredValidator()),
                    // newContent: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringLengthValidator(this.newContentLimitChar.value)]),
                    newTitle: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.newTitleLimitChar.value)]),
                    typeItemNo: new FormControl('', ValidatorFns.requiredValidator())
                };
            case FormOperate.update:
                // let updateFormData = this.data.data as TUser;
                let updateFormData = this.data.data as DPB0045Resp;
                this.newContent = decodeURIComponent(updateFormData.newContent);
                return {
                    postDateTime: new FormControl(updateFormData.postDateTime),
                    // newContent: new FormControl(decodeURIComponent(updateFormData.newContent), ValidatorFns.stringLengthValidator(this.newContentLimitChar.value)),
                    newTitle: new FormControl(updateFormData.newTitle, ValidatorFns.maxLengthValidator(this.newTitleLimitChar.value)),
                    typeItemNo: new FormControl(updateFormData.typeItemNo)
                };
            default:
              return {
                postDateTime: new FormControl('', ValidatorFns.requiredValidator()),
                // newContent: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringLengthValidator(this.newContentLimitChar.value)]),
                newTitle: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.newTitleLimitChar.value)]),
                typeItemNo: new FormControl('', ValidatorFns.requiredValidator())
            }
        }
    }

    public get postDateTime() { return this.form.get('postDateTime'); };
    public get newTitle() { return this.form.get('newTitle'); };
    // public get newContent() { return this.form.get('newContent'); };
    public get typeItemNo() { return this.form.get('typeItemNo'); };
}
