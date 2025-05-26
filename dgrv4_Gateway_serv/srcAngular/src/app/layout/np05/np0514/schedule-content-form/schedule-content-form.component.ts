import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DPB0101Items, DPB0101ItemsAddNo } from 'src/app/models/api/CycleScheduleService/dpb0101.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { BaseComponent } from 'src/app/layout/base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { CycleScheduleService } from 'src/app/shared/services/api-cycle-schedule.service';

@Component({
    selector: 'app-schedule-content-form',
    templateUrl: './schedule-content-form.component.html',
    styleUrls: ['./schedule-content-form.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScheduleContentFormComponent extends BaseComponent implements OnInit {

    @Input() rowcount?: number;
    @Input() data: any;
    @Input() _ref: any;
    @Input() _no?: number;
    @Input() action?: string;
    @Output() remove: EventEmitter<number> = new EventEmitter;
    @Output() change: EventEmitter<DPB0101ItemsAddNo> = new EventEmitter;
    // @Output() valueChange = new EventEmitter;

    form: FormGroup;
    refItemNoOption: { label: string; value: {subitemNo:string, subitemName:string, param1:string|undefined, index:number  } }[] = [];
    refSubitemNoOption: { label: string; value: {subitemNo:string, index:number} }[] = [];

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private list: ListService,
        private scheduleService: CycleScheduleService,
    ) {
        super(route, tr);

        this.form = this.fb.group({
          sortBy: new FormControl(0),
          // 0, ValidatorFns.requiredValidator()
          refItemNo: new FormControl('', ValidatorFns.requiredValidator()),
          refSubitemNo: new FormControl({ value: '', disabled: true }),
          inParams: new FormControl(''),
          identifData: new FormControl('')
      });
    }

    ngOnInit() {


        if (this.data) {
            this.sortBy!.setValue(this.data.sortBy);
            this.refItemNo!.setValue(this.data.refItemNo);
            this.inParams!.setValue(this.data.inParams);
            this.identifData!.setValue(this.data.identifData);
            if (this.action == 'detail') {
                this.sortBy!.disable();
                this.inParams!.disable();
                this.identifData!.disable();
            }
            else {
                this.sortBy!.enable();
                this.inParams!.enable();
                this.identifData!.enable();
            }
        }
        this.sortBy!.valueChanges.subscribe( res => {
            if(res == null)
            {
                this.sortBy!.setValue('');
            }

        });
        this.form.valueChanges.subscribe((res: DPB0101Items) => {
            this.change.emit({ sortBy: res.sortBy, refItemNo: res.refItemNo, refSubitemNo: res.refSubitemNo, inParams: res.inParams, identifData: res.identifData, no: this._no, isValid: !this.form.invalid } as DPB0101ItemsAddNo);
        });
        let ReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('SCHED_CATE1')) + ',' + 18,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _refItemNoOption:{label:string, value:{subitemNo:string, subitemName:string, param1:string|undefined, index:number  }}[] = [];
                res.RespBody.subItems?.map((item, index) => {
                    _refItemNoOption.push({ label: item.subitemName, value: { subitemNo: item.subitemNo, subitemName: item.subitemName, param1: item.param1, index: index } });
                });
                this.refItemNoOption = _refItemNoOption;
                if (this.data) {
                    let _oriRefItemNo = this.refItemNoOption.find(item => item.value['subitemNo'] == this.data.refItemNo);
                    this.refItemNo!.setValue(_oriRefItemNo?.value);
                    if (this.action == 'detail') {
                        this.refItemNo!.disable();
                    }
                    else {
                        this.refItemNo!.enable();
                    }
                }
            }
        });
        this.refItemNo!.valueChanges.subscribe(opt => {
            if(opt== null) return;
            if (opt.param1 != '-1') {
                let ReqBody = {
                    isDefault: 'N'
                } as DPB0047Req;
                ReqBody.encodeItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder(opt.subitemNo)) + ',' + opt.param1
                this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        let _refSubitemNoOption:{ label:string, value:{subitemNo:string, index:number}}[] = [];
                        res.RespBody.subItems?.map((item, index) => {
                            _refSubitemNoOption.push({ label: item.subitemName, value: { subitemNo: item.subitemNo, index: index } });
                        });
                        this.refSubitemNoOption = _refSubitemNoOption;
                        if (this.data) {
                            window.setTimeout(() => {
                                let _oriRefSubItemNo = this.refSubitemNoOption.find(item => item.value['subitemNo'] == this.data.refSubitemNo);
                                this.refSubitemNo!.setValue(_oriRefSubItemNo?.value);
                                if (this.action == 'detail') {
                                    this.refSubitemNo!.disable();
                                }
                                else {
                                    this.refSubitemNo!.enable();
                                }
                            }, 100);
                        }
                        document.getElementById('refSubitemNo_label')?.classList.add('required');
                        this.refSubitemNo!.setValidators(ValidatorFns.requiredValidator());
                        this.refSubitemNo!.updateValueAndValidity();
                        this.refSubitemNo!.setValue('');
                        this.refSubitemNo!.enable();
                    }
                });
            }
            else {
                this.refSubitemNoOption = [];
                document.getElementById('refSubitemNo_label')?.classList.remove('required');
                this.refSubitemNo!.clearValidators();
                this.refSubitemNo!.updateValueAndValidity();
                this.refSubitemNo!.setValue(null);
                this.refSubitemNo!.disable();
            }
        });

        if(this.action == 'create') {
            this.scheduleService.createRjob_before2().subscribe(res => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                    let filterResult = res.RespBody.constraints.filter(item=>{
                        return item.field != 'remark'
                    })
                    this.addFormValidator(this.form, filterResult);

                    // this.sortBy.setValidators(ValidatorFns.requiredValidator());
                    // this.sortBy.updateValueAndValidity();
                    // console.log(this.form)
                }
            });

        }
        else if(this.action == 'update')
        {
            this.scheduleService.updateRjob_before2().subscribe(res => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                    let filterResult = res.RespBody.constraints.filter(item=>{
                        return !(item.field == 'remark' || item.field == 'rjobName');
                    })
                    this.addFormValidator(this.form, filterResult);
                }
            });
        }



    }

    disabledForm() {
        this.form.disable();
        this.form.updateValueAndValidity();
    }

    deleteScheduleItems() {
        // this._ref.destroy();
        this.remove.emit(this._no);
    }

    public get sortBy() { return this.form.get('sortBy'); }
    public get refItemNo() { return this.form.get('refItemNo'); }
    public get refSubitemNo() { return this.form.get('refSubitemNo'); }
    public get inParams() { return this.form.get('inParams'); }
    public get identifData() { return this.form.get('identifData'); }

}
