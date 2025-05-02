import { Component, OnInit, ViewChild, ElementRef, ViewContainerRef } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0102Req, DPB0102Items } from 'src/app/models/api/CycleScheduleService/dpb0102.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { CycleScheduleService } from 'src/app/shared/services/api-cycle-schedule.service';
import { DPB0104Req, DPB0104Resp } from 'src/app/models/api/CycleScheduleService/dpb0104.interface';
import { DPB0101Req, DPB0101Cron } from 'src/app/models/api/CycleScheduleService/dpb0101.interface';
import * as dayjs from 'dayjs';
import { DPB0103Req, DPB0103Resp } from 'src/app/models/api/CycleScheduleService/dpb0103.interface';
import { DPB0105Req, DPB0105Cron } from 'src/app/models/api/CycleScheduleService/dpb0105.interface';
import { MessageService, ConfirmationService, MenuItem } from 'primeng/api';
import * as ValidatorFns from '../../../shared/validator-functions';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';




@Component({
    selector: 'app-np0514',
    templateUrl: './np0514.component.html',
    styleUrls: ['./np0514.component.scss'],
    providers: [ConfirmationService]
})
export class Np0514Component extends BaseComponent implements OnInit {
    @ViewChild('op') op;

    form: FormGroup;
    statusOption: { label: string; value: string; }[] = [];
    scheduleCols: { field: string; header: string; }[] = [];
    scheduleList: Array<DPB0102Items> = [];
    scheduleListRowcount: number = 0;
    pageNum: number = 1; // 1：查詢、2：建立(明細、更新、暫停、啟動、略過一次、作廢)、3：歷程
    currentTitle: string = this.title;
    btnName: string = '';
    dayRangeOption: { label: string; value: number }[] = [];
    weekRangeOption: { label: string; value: number }[] = [];
    hourOption: { label: string; value: number }[] = [];
    minuteOption: { label: string; value: number }[] = [];
    currentAction: string = 'query';
    scheduleHistoryCols: { field: string; header: string; }[] = [];
    scheduleHistoryData?: DPB0104Resp;
    scheduleHistoryDataRowcount: number = 0;
    scheduleDetailData?: DPB0103Resp;
    minDate: Date = new Date();
    canCreate: boolean = false;
    canUpdate: boolean = false;
    isInvalid:boolean = false;

    btnData:MenuItem[] = [];
    // items!: MenuItem[];

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private list: ListService,
        private scheduleService: CycleScheduleService,
        private message: MessageService,
        private roleService: RoleService,
        private confirmationService:ConfirmationService
    ) {
        super(route, tr);

        this.form = this.fb.group({
          keyword: new FormControl(''),
          status: new FormControl('all'),
          rjobName: new FormControl(''),
          remark: new FormControl(''),
          frequency: new FormControl('0'),
          dayRange: new FormControl({ value: [], disabled: true }),
          weekRange: new FormControl({ value: [], disabled: true }),
          hour: new FormControl(0, ValidatorFns.requiredValidator()),
          minute: new FormControl(0, ValidatorFns.requiredValidator()),
          effDateTime: new FormControl(''),
          invDateTime: new FormControl(''),
          rjobItems: new FormControl([]),
          orirjobItems: new FormControl([])
      });
    }

    logDay(){
      // console.log(this.dayRange?.value)
      console.log('week',this.weekRange?.value);
      console.log('day',this.dayRange?.value);
    }

    ngOnInit() {

    //   this.items = [{
    //     label: 'File',
    //     items: [
    //         {label: 'New', icon: 'pi pi-fw pi-plus'},
    //         {label: 'Download', icon: 'pi pi-fw pi-download'}
    //     ]
    // },
    // {
    //     label: 'Edit',
    //     items: [
    //         {label: 'Add User', icon: 'pi pi-fw pi-user-plus'},
    //         {label: 'Remove User', icon: 'pi pi-fw pi-user-minus'}
    //     ]
    // }];

        let ReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('RJOB_STATUS')) + ',' + 17,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const code = ['all'];
                const dict = await this.tool.getDict(code);
                let _statusOption = [
                    { label: dict['all'], value: 'all' }
                ];
                res.RespBody.subItems?.map(item => {
                    _statusOption.push({ label: item.subitemName, value: item.subitemNo });
                });
                this.statusOption = _statusOption;
            }
        });

        this.rjobItems!.valueChanges.subscribe(value => {
            if(value){
                this.isInvalid = value.every(item => item.isValid)
            }
            else{
                this.isInvalid = false;
            }
        });

        this.frequency!.valueChanges.subscribe(value => {
            if (value == '1') {
                this.dayRange!.enable();
                this.weekRange!.disable();
            }
            else if (value == '2') {
                this.weekRange!.enable();
                this.dayRange!.disable();
            }
            else {
                this.dayRange!.disable();
                this.weekRange!.disable();
            }
            this.dayRange!.reset('');
            this.weekRange!.reset('');
        });
        this.roleService.queryRTMapByUk({ txIdList: ['DPB0101', 'DPB0105'] } as DPB0115Req).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.canCreate = res.RespBody.dataList.find(item => item.txId === 'DPB0101') ? res.RespBody.dataList.find(item => item.txId === 'DPB0101')!.available : false;
                this.canUpdate = res.RespBody.dataList.find(item => item.txId === 'DPB0105') ? res.RespBody.dataList.find(item => item.txId === 'DPB0105')!.available : false;
            }
        });
        this.init();

    }

    async init() {
        const code = ['task', 'status', 'repeating_time', 'next_execute_time', 'end_time', 'memo', 'add_job_id', 'item_no', 'subitem_no', 'schedule_sort', 'schedule_next_time', 'update_time', 'status', 'exec_result', 'week_option.sunday', 'week_option.monday', 'week_option.tuesday', 'week_option.wednesday', 'week_option.thursday', 'week_option.friday', 'week_option.saturday'];
        const dict = await this.tool.getDict(code);
        for (let i = 1; i < 32; i++) {
            this.dayRangeOption.push({ label: i.toString(), value: i });
        }
        for (let i = 0; i < 60; i++) {
            if (i < 10) {
                this.hourOption.push({ label: '0' + i, value: i });
                this.minuteOption.push({ label: '0' + i, value: i });
            }
            else {
                if (i < 24) {
                    this.hourOption.push({ label: i.toString(), value: i });
                }
                this.minuteOption.push({ label: i.toString(), value: i });
            }
        }
        this.weekRangeOption = [
            { label: dict['week_option.sunday'], value: 0 },
            { label: dict['week_option.monday'], value: 1 },
            { label: dict['week_option.tuesday'], value: 2 },
            { label: dict['week_option.wednesday'], value: 3 },
            { label: dict['week_option.thursday'], value: 4 },
            { label: dict['week_option.friday'], value: 5 },
            { label: dict['week_option.saturday'], value: 6 }
        ];
        this.scheduleCols = [
            { field: 'rjobName', header: dict['task'] },
            { field: 'statusName', header: dict['status'] },
            { field: 'cronDesc', header: dict['repeating_time'] },
            { field: 'nextDateTime', header: dict['next_execute_time'] },
            { field: 'effPeriod', header: dict['end_time'] },
            { field: 'remark', header: dict['memo'] }
        ];
        this.scheduleHistoryCols = [
            { field: 'apptJobId', header: dict['add_job_id'] },
            { field: 'refItemName', header: dict['item_no'] },
            { field: 'refSubitemName', header: dict['subitem_no'] },
            { field: 'sortBy', header: dict['schedule_sort'] },
            { field: 'periodNexttime', header: dict['schedule_next_time'] },
            { field: 'updateDateTime', header: dict['update_time'] },
            { field: 'statusName', header: dict['status'] },
            { field: 'execResult', header: dict['exec_result'] },
        ];
        this.scheduleList = [];
        this.scheduleListRowcount = this.scheduleList.length;
        let ReqBody = {
            status: this.status!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.status!.value)) + ',' + this.status!.value : '',
            keyword: this.keyword!.value
        } as DPB0102Req;
        this.scheduleService.queryRjobList_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.scheduleList = res.RespBody.dataList;
                this.scheduleListRowcount = this.scheduleList.length;
            }
        });
    }

    queryScheduleList() {
        this.scheduleList = [];
        this.scheduleListRowcount = this.scheduleList.length;
        let ReqBody = {
            status: this.status!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.status!.value)) + ',' + this.status!.value : '',
            keyword: this.keyword!.value
        } as DPB0102Req;
        this.scheduleService.queryRjobList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.scheduleList = res.RespBody.dataList;
                this.scheduleListRowcount = this.scheduleList.length;
            }
        });
    }

    moreScheduleList() {
        let ReqBody = {
            apptRjobId: this.scheduleList[this.scheduleList.length - 1].apptRjobId,
            status: this.status!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.status!.value)) + ',' + this.status!.value : '',
            keyword: this.keyword!.value
        } as DPB0102Req;
        this.scheduleService.queryRjobList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.scheduleList = this.scheduleList.concat(res.RespBody.dataList);
                this.scheduleListRowcount = this.scheduleList.length;
            }
        });
    }

    saveScheduleSetting() {
        this.message.clear();
        let _rjobItems:any[] = [];
        switch (this.currentAction) {
            case 'create':
                let createReqBody = {
                    rjobName: this.rjobName!.value,
                    remark: this.remark!.value,
                    cronJson: {
                        frequency: parseInt(this.frequency!.value),
                        hour: this.hour!.value,
                        minute: this.minute!.value
                    } as DPB0101Cron,
                    effDateTime: this.effDateTime!.value != '' ? dayjs(this.transformTimeSecond(this.effDateTime!.value)).format('YYYY/MM/DD HH:mm:ss') : '',
                    invDateTime: this.invDateTime!.value != '' ? dayjs(this.transformTimeSecond(this.invDateTime!.value)).format('YYYY/MM/DD HH:mm:ss') : ''
                } as DPB0101Req;
                if (this.frequency!.value == '1') {
                    createReqBody.cronJson.dayRange = this.dayRange!.value;
                }
                if (this.frequency!.value == '2') {
                    createReqBody.cronJson.weekRange = this.weekRange!.value;
                }
                this.rjobItems!.value.map(item => {
                    _rjobItems.push({ refItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder(item.refItemNo.subitemNo)) + ',' + item.refItemNo.index, refSubitemNo: (item.refSubitemNo && item.refSubitemNo != '') ? this.tool.Base64Encoder(this.tool.BcryptEncoder(item.refSubitemNo.subitemNo)) + ',' + item.refSubitemNo.index : '', inParams: item.inParams, identifData: item.identifData, sortBy: item.sortBy })
                });
                createReqBody.rjobItems = _rjobItems;
                // console.log('createReqBody :', createReqBody)
                this.scheduleService.createRjob(createReqBody).subscribe(async res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        const code = ['button.create', 'schedule_job', 'message.success'];
                        const dict = await this.tool.getDict(code);
                        this.message.add({ severity: 'success', summary: `${dict['button.create']} ${dict['schedule_job']}`, detail: `${dict['button.create']} ${dict['message.success']}` });
                        this.queryScheduleList();
                        this.changePage('query');
                    }
                });
                break;
            case 'update':
                let updateReqBody = {
                    act: 'U',
                    apptRjobId: this.scheduleDetailData?.apptRjobId,
                    lv: this.scheduleDetailData?.lv,
                    rjobName: this.rjobName!.value,
                    remark: this.remark!.value,
                    cronJson: {
                        frequency: parseInt(this.frequency!.value),
                        hour: this.hour!.value,
                        minute: this.minute!.value
                    } as DPB0105Cron,
                    effDateTime: this.effDateTime!.value != '' ? dayjs(this.transformTimeSecond(this.effDateTime!.value)).format('YYYY/MM/DD HH:mm:ss') : '',
                    invDateTime: this.invDateTime!.value != '' ? dayjs(this.transformTimeSecond(this.invDateTime!.value)).format('YYYY/MM/DD HH:mm:ss') : ''
                } as DPB0105Req;
                if (this.frequency!.value == '1') {
                    updateReqBody.cronJson.dayRange = this.dayRange!.value;
                }
                if (this.frequency!.value == '2') {
                    updateReqBody.cronJson.weekRange = this.weekRange!.value;
                }
                this.rjobItems!.value.map(item => {
                    _rjobItems.push({ refItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder(item.refItemNo.subitemNo)) + ',' + item.refItemNo.index, refSubitemNo: (item.refSubitemNo && item.refSubitemNo != '') ? this.tool.Base64Encoder(this.tool.BcryptEncoder(item.refSubitemNo.subitemNo)) + ',' + item.refSubitemNo.index : '', inParams: item.inParams, identifData: item.identifData, sortBy: item.sortBy })
                });
                updateReqBody.newDataList = _rjobItems;
                let _orirjobItems:any[] = [];
                this.orirjobItems!.value.map(item => {
                    _orirjobItems.push({ apptRjobDId: item.apptRjobDId, lv: item.lv, refItemNo: item.refItemNo, refSubitemNo: item.refSubitemNo, inParams: item.inParams, identifData: item.identifData, sortBy: item.sortBy });
                });
                updateReqBody.oriDataList = _orirjobItems;
                // console.log('update req :', updateReqBody)
                this.scheduleService.updateRjob(updateReqBody).subscribe(async res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        const code = ['button.update', 'schedule_job', 'message.success'];
                        const dict = await this.tool.getDict(code);
                        this.message.add({ severity: 'success', summary: `${dict['button.update']} ${dict['schedule_job']}`, detail: `${dict['button.update']} ${dict['message.success']}` });
                        this.queryScheduleList();
                        this.changePage('query');
                    }
                });
                break;
            case 'pause':
                let pauseReqBody = {
                    act: 'P',
                    apptRjobId: this.scheduleDetailData?.apptRjobId,
                    lv: this.scheduleDetailData?.lv,
                } as DPB0105Req;
                this.scheduleService.updateRjob(pauseReqBody).subscribe(async res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        const code = ['button.pause', 'schedule_job', 'message.success'];
                        const dict = await this.tool.getDict(code);
                        this.message.clear();
                        this.message.add({ severity: 'success', summary: `${dict['button.pause']} ${dict['schedule_job']}`, detail: `${dict['button.pause']} ${dict['message.success']}` });
                        this.queryScheduleList();
                    }
                });
                break;
            case 'active':
                let activeReqBody = {
                    act: 'A',
                    apptRjobId: this.scheduleDetailData?.apptRjobId,
                    lv: this.scheduleDetailData?.lv,
                } as DPB0105Req;
                this.scheduleService.updateRjob(activeReqBody).subscribe(async res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        const code = ['button.active', 'schedule_job', 'message.success'];
                        const dict = await this.tool.getDict(code);
                        this.message.clear();
                        this.message.add({ severity: 'success', summary: `${dict['button.active']} ${dict['schedule_job']}`, detail: `${dict['button.active']} ${dict['message.success']}` });
                        this.queryScheduleList();
                    }
                });
                break;
            case 'skip':
                let skipReqBody = {
                    act: 'i',
                    apptRjobId: this.scheduleDetailData?.apptRjobId,
                    lv: this.scheduleDetailData?.lv,
                } as DPB0105Req;
                this.scheduleService.updateRjob(skipReqBody).subscribe(async res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        const code = ['button.skip_one', 'schedule_job', 'message.success', 'next_execute_time'];
                        const dict = await this.tool.getDict(code);
                        this.message.clear();
                        this.message.add({ severity: 'success', summary: `${dict['button.skip_one']} ${dict['schedule_job']}`, detail: `${dict['button.skip_one']} ${dict['message.success']}` });
                        this.message.add({ severity: 'success', summary: `${dict['next_execute_time']}：${res.RespBody.nextDateTime}` });
                        this.queryScheduleList();
                    }
                });
                break;
            case 'invalid':
                let invalidReqBody = {
                    act: 'S',
                    apptRjobId: this.scheduleDetailData?.apptRjobId,
                    lv: this.scheduleDetailData?.lv,
                } as DPB0105Req;
                this.scheduleService.updateRjob(invalidReqBody).subscribe(async res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        const code = ['button.invalid', 'schedule_job', 'message.success'];
                        const dict = await this.tool.getDict(code);
                        this.message.clear();
                        this.message.add({ severity: 'success', summary: `${dict['button.invalid']} ${dict['schedule_job']}`, detail: `${dict['button.invalid']} ${dict['message.success']}` });
                        this.queryScheduleList();
                    }
                });
                break;
        }
    }

    async changePage(action: string, rowData?: DPB0102Items) {
        const code = ['button.create', 'button.detail', 'button.confirm', 'cfm_pause_schedule', 'button.tracker', 'cfm_active_schedule', 'cfm_skip_schedule', 'cfm_invalid_schedule', 'button.edit','system_alert'];
        const dict = await this.tool.getDict(code);
        this.currentAction = action;
        // console.log(action);
        switch (action) {
            case 'detail':
                let detailReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0103Req;
                this.scheduleService.queryRjobByPk(detailReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
                        this.btnName = dict['button.detail'];
                        this.pageNum = 2;
                        this.setScheduleValue(res.RespBody, true);
                    }
                });
                break;
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                this.resetFormValidator(this.form);
                this.form.enable();
                this.status!.setValue('all');
                break;
            case 'create':

                this.scheduleDetailData = {} as DPB0103Resp;


                // 檢核欄位 任務名稱
                this.scheduleService.createRjob_before().subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.addFormValidator(this.form, res.RespBody.constraints);
                    }
                });

                // 檢核欄位 備註
                this.scheduleService.createRjob_before2().subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        let filterResult = res.RespBody.constraints.filter(item=>{
                            return item.field == 'remark'
                        })
                        this.addFormValidator(this.form, filterResult);
                    }
                });

                this.frequency!.setValue('0');
                this.dayRange?.reset([]);
                this.weekRange?.reset([]);

                this.currentTitle = `${this.title} > ${dict['button.create']}`;
                this.btnName = dict['button.create'];
                this.pageNum = 2;

                break;
            case 'update':
                let updateReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0103Req;
                this.scheduleService.queryRjobByPk(updateReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.currentTitle = `${this.title} > ${dict['button.edit']}`;
                        this.btnName = dict['button.confirm'];
                        this.pageNum = 2;


                        // 檢核欄位 任務名稱/備註
                        this.scheduleService.updateRjob_before2().subscribe(res => {
                            if (this.tool.checkDpSuccess(res.ResHeader)) {
                                let filterResult = res.RespBody.constraints.filter(item=>{
                                    return (item.field == 'remark' || item.field == 'rjobName')
                                })
                                this.addFormValidator(this.form, filterResult);
                            }
                        });

                        this.setScheduleValue(res.RespBody, false);
                    }
                });


                break;
            case 'pause':
                let pauseReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0103Req;
                this.scheduleService.queryRjobByPk(pauseReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.scheduleDetailData = res.RespBody;
                        // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_pause_schedule'] });
                        this.confirmationService.confirm({
                          header: dict['system_alert'],
                          message: dict['cfm_pause_schedule'],
                          accept: () => {
                              this.saveScheduleSetting();
                          }
                        });
                    }
                });
                break;
            case 'active':
                let activeReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0103Req;
                this.scheduleService.queryRjobByPk(activeReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.scheduleDetailData = res.RespBody;
                        // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_active_schedule'] });
                        this.confirmationService.confirm({
                          header: dict['system_alert'],
                          message: dict['cfm_active_schedule'],
                          accept: () => {
                              this.saveScheduleSetting();
                          }
                        });
                    }
                });
                break;
            case 'skip':
                let skipReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0103Req;
                this.scheduleService.queryRjobByPk(skipReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.scheduleDetailData = res.RespBody;
                        // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_skip_schedule'] });
                        this.confirmationService.confirm({
                          header: dict['system_alert'],
                          message: dict['cfm_skip_schedule'],
                          accept: () => {
                              this.saveScheduleSetting();
                          }
                        });
                    }
                });
                break;
            case 'invalid':
                let invalidReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0103Req;
                this.scheduleService.queryRjobByPk(invalidReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.scheduleDetailData = res.RespBody;
                        // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_invalid_schedule'] });
                        this.confirmationService.confirm({
                          header: dict['system_alert'],
                          message: dict['cfm_invalid_schedule'],
                          accept: () => {
                              this.saveScheduleSetting();
                          }
                        });
                    }
                });
                break;
            case 'traker':
                let historyReqBody = {
                    apptRjobId: rowData?.apptRjobId
                } as DPB0104Req;
                this.scheduleService.queryRjobHistory(historyReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.currentTitle = `${this.title} > ${dict['button.tracker']}`;
                        this.pageNum = 3;
                        this.scheduleHistoryData = res.RespBody;
                        this.scheduleHistoryDataRowcount = this.scheduleHistoryData.historyList.length;
                    }
                });
                break;
        }
    }

    headerReturn(){
        this.changePage('query');

    }

    moreScheduleHistoryData() {
        let ReqBody = {
            apptJobId: this.scheduleHistoryData?.historyList[this.scheduleHistoryData.historyList.length - 1].apptJobId,
            apptRjobId: this.scheduleHistoryData?.apptRjobId
        } as DPB0104Req;
        this.scheduleService.queryRjobHistory(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.scheduleHistoryData!.historyList = this.scheduleHistoryData!.historyList.concat(res.RespBody.historyList);
                this.scheduleHistoryDataRowcount = this.scheduleHistoryData!.historyList.length;
            }
        });
    }

    onReject() {
        this.message.clear();
    }

    transformTimeSecond(date: Date): Date {
        let _date = new Date(date);
        _date.setSeconds(0);
        return _date;
    }

    setScheduleValue(RespBody: DPB0103Resp, isDisabled: boolean) {
        if (isDisabled) {
          this.form.disable();
        } else {
          this.form.enable();
        }

        this.scheduleDetailData = RespBody;
        this.frequency!.setValue(RespBody.cronJson.frequency.toString());
        this.rjobName!.setValue(RespBody.rjobName);
        this.remark!.setValue(RespBody.remark);

          this.dayRange!.setValue(RespBody.cronJson.dayRange);
          isDisabled ? this.dayRange?.disable() : this.dayRange?.enable();
          this.weekRange!.setValue(RespBody.cronJson.weekRange);
          isDisabled ? this.weekRange?.disable() : this.weekRange?.enable();

        this.hour!.setValue(RespBody.cronJson.hour);
        this.minute!.setValue(RespBody.cronJson.minute);
        this.effDateTime!.setValue(RespBody.effDateTime != '' ? new Date(RespBody.effDateTime!) : '');
        this.invDateTime!.setValue(RespBody.invDateTime != '' ? new Date(RespBody.invDateTime!) : '');
        this.orirjobItems!.setValue(RespBody.oriDataList);
        let _rjobItems: any[] = [];
        this.orirjobItems!.value.map(item => {
            _rjobItems.push({ refItemNo: item.refItemNo, refSubitemNo: item.refSubitemNo, inParams: item.inParams, identifData: item.identifData, sortBy: item.sortBy });
        });
        this.rjobItems!.setValue(_rjobItems);

    }

     async toggleBtnMenu(evt, rowData: DPB0102Items) {

      this.btnData = [];
      const code = ['button.pause', 'button.pause_once','button.active', 'button.disable','button.history'];
      const dict = await this.tool.getDict(code);

      // 暫停
      if(rowData['pauseVisible']){
        let item = {
          label: dict['button.pause'],
          disabled: rowData['pauseFlag'] == false || this.canUpdate == false,
          command: () => {
            this.op.hide();
            this.changePage('pause',rowData)
          }
        }
        this.btnData?.push(item);
      }
      // 啟動
      if(rowData['activeVisible']){
        let item = {
          label: dict['button.active'],
          disabled: rowData['activeFlag'] == false || this.canUpdate == false,
          command: () => {
            this.op.hide();
            this.changePage('active',rowData)
          }
        }
        this.btnData?.push(item);
      }

      //暫停一次
      let pauseonce = {
        label: dict['button.pause_once'],
        disabled: rowData['skipFlag'] == false || this.canUpdate == false,
        command: () => {
          this.op.hide();
          this.changePage('skip',rowData)
         }
      }
      this.btnData?.push(pauseonce);

      // 停用
      let disable = {
        label: dict['button.disable'],
        disabled: rowData['inactiveFlag'] == false || this.canUpdate == false,
        command: () => {
          this.op.hide();
          this.changePage('invalid',rowData)
         }
      }
      this.btnData?.push(disable);
      // 歷程
      let history = {
        label: dict['button.history'],
        disabled: rowData['historyFlag'] == false || this.canUpdate == false,
        command: () => {
          this.op.hide();
          this.changePage('traker',rowData)
        }
      }
      this.btnData?.push(history);

      this.op.toggle(evt);

    }

    public get keyword() { return this.form.get('keyword'); }
    public get status() { return this.form.get('status'); }
    public get rjobName() { return this.form.get('rjobName'); }
    public get remark() { return this.form.get('remark'); }
    public get frequency() { return this.form.get('frequency'); }
    public get dayRange() { return this.form.get('dayRange'); }
    public get weekRange() { return this.form.get('weekRange'); }
    public get hour() { return this.form.get('hour'); }
    public get minute() { return this.form.get('minute'); }
    public get effDateTime() { return this.form.get('effDateTime'); }
    public get invDateTime() { return this.form.get('invDateTime'); }
    public get rjobItems() { return this.form.get('rjobItems'); }
    public get orirjobItems() { return this.form.get('orirjobItems'); }

}
