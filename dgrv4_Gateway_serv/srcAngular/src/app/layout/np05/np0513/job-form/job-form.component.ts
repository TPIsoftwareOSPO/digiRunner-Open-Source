import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0062Req } from 'src/app/models/api/JobService/dpb0062.interface';
import * as dayjs from 'dayjs';
import { JobService } from 'src/app/shared/services/api-job.service';

@Component({
    selector: 'app-job-form',
    templateUrl: './job-form.component.html',
    styleUrls: ['./job-form.component.css']
})
export class JobFormComponent implements OnInit {

    @Input() data!: FormParams;
    @Output() close?: Function;
    @Output() changePage:EventEmitter<any> = new EventEmitter();

    form?: FormGroup;
    refItemNos: { label: string; value: object; }[] = [];
    refSubitemNos: { label: string; value: string; }[] = [];

    constructor(
        private fb: FormBuilder,
        private list: ListService,
        private tool: ToolService,
        private job: JobService
    ) {

     }

    ngOnInit() {
      this.form = this.fb.group(this.resetFormGroup(this.data.operate)!);
        // 大分類
        let ReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('SCHED_CATE1')) + ',' + 18,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _refItemNos:{label:string, value:{ subitemNo: string, subitemName:string, param1:string|undefined }}[] = [];
                if(res.RespBody.subItems){
                  for (let item of res.RespBody.subItems) {
                      _refItemNos.push({ label: item.subitemName, value: { subitemNo: item.subitemNo, subitemName: item.subitemName, param1: item.param1 } });
                  }
                }
                this.refItemNos = _refItemNos;
            }
        });
        this.refItemNo!.valueChanges.subscribe(opt => {

            if (opt &&opt.param1 != '-1') {
                let ReqBody = {
                    isDefault: 'N'
                } as DPB0047Req;
                ReqBody.encodeItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder(opt.subitemNo)) + ',' + opt.param1
                this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        let _refSubitemNos:{label:string, value: string}[] = [];
                        if(res.RespBody.subItems){
                        for (let item of res.RespBody.subItems) {
                            _refSubitemNos.push({ label: item.subitemName, value: item.subitemNo });
                        }
                        }
                        this.refSubitemNos = _refSubitemNos;
                        document.getElementById('refSubitemNo_label')?.classList.add('required');
                        this.refSubitemNo!.setValidators(ValidatorFns.requiredValidator());
                        this.refSubitemNo!.updateValueAndValidity();
                        this.refSubitemNo!.setValue('');
                        this.refSubitemNo!.enable();
                    }
                });
            }
            else {
                this.refSubitemNos = [];
                document.getElementById('refSubitemNo_label')?.classList.remove('required');
                this.refSubitemNo!.clearValidators();
                this.refSubitemNo!.updateValueAndValidity();
                this.refSubitemNo!.setValue(null);
                this.refSubitemNo!.disable();
            }
        });
    }

    submitForm() {
        let ReqBody = {
            refItemNo: this.refItemNo!.value.subitemNo,
            refSubitemNo: this.refSubitemNo!.value,
            startDateTime: dayjs(this.startDateTime!.value).format('YYYY/MM/DD HH:mm'),
            inParams: this.tool.Base64Encoder(this.inParams!.value),
            identifData: this.tool.Base64Encoder(this.identifData!.value)
        } as DPB0062Req;
        // console.log('ReqBody :', ReqBody)
        let createObservable = this.job.createOneJob(ReqBody);
        createObservable.subscribe(r => {
            this.changePageHandler(null);
        });
        // if (this.close) this.close(createObservable);
    }
    changePageHandler(evt){
        this.changePage.emit(0);
    }
    private resetFormGroup(formOperate?: FormOperate) {
        //初始化
        if (!formOperate) return {
            refItemNo: '',
            refSubitemNo: '',
            startDateTime: '',
            inParams: '',
            identifData: ''
        };
        switch (formOperate) {
            case FormOperate.create:
                return {
                    refItemNo: new FormControl('', ValidatorFns.requiredValidator()),
                    refSubitemNo: new FormControl({ value: '', disabled: true }),
                    startDateTime: new FormControl('', ValidatorFns.requiredValidator()),
                    inParams: new FormControl(''),
                    identifData: new FormControl('')
                }
                default:
                  return {
                    refItemNo: '',
                    refSubitemNo: '',
                    startDateTime: '',
                    inParams: '',
                    identifData: ''
                };

        }
    }

    public get refItemNo() { return this.form!.get('refItemNo'); };
    public get refSubitemNo() { return this.form!.get('refSubitemNo'); };
    public get startDateTime() { return this.form!.get('startDateTime'); };
    public get inParams() { return this.form!.get('inParams'); };
    public get identifData() { return this.form!.get('identifData'); };

}
