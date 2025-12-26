import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DPB0058RespItem, DPB0058Req } from 'src/app/models/api/JobService/dpb0058.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { JobService } from 'src/app/shared/services/api-job.service';
import * as dayjs from 'dayjs';
import * as ValidatorFns from '../../../shared/validator-functions';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0060Req } from 'src/app/models/api/JobService/dpb0060.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { JobDetailComponent } from './job-detail/job-detail.component';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from '../../base-component';
import { StringLengthPipe } from 'src/app/shared/pipes/string-length.pipe';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { JobFormComponent } from './job-form/job-form.component';
import { TranslateService } from '@ngx-translate/core';
import { MemListService } from 'src/app/shared/services/api-memlist.service';
import { environment as env } from './../../../../environments/environment';
import { forkJoin, from, Observable, of, pipe } from 'rxjs';
import { DPB0061Resp } from 'src/app/models/api/JobService/dpb0061.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { catchError, concatMap, finalize, map, mergeMap, reduce, scan, switchMap, takeLast, tap, toArray, zip, zipAll, } from 'rxjs/operators';
// import { last } from '@angular/router/src/utils/collection';
import { combineLatest } from 'rxjs';
import { AjaxResponse } from 'rxjs/ajax';
import { AA0206Req } from 'src/app/models/api/ClientService/aa0206.interface';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
// import { AjaxResponse } from 'rxjs/internal/observable/dom/AjaxObservable';

@Component({
  selector: 'app-np0513',
  templateUrl: './np0513.component.html',
  styleUrls: ['./np0513.component.css'],
  providers: [StringLengthPipe, ConfirmationService]
})
export class Np0513Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;

  form: FormGroup;
  cols: { field: string; header: string; width?: string; type?: StringLengthPipe; }[] = [];
  dataList: Array<DPB0058RespItem> = [];
  rowcount: number = 0;
  jobData?: DPB0058RespItem;
  dialogTitle: string = '';
  formOperate = FormOperate;
  statusOption: { label: string; value: string; }[] = [];
  currentDate = new Date();
  rerfeshMemListUrls: string[] = [];
  PageNum: EmPageBlock = EmPageBlock.QUERYPAGE;
  EmPageBlock = EmPageBlock;
  createData?: FormParams;
  detailData?: DPB0061Resp;
  sourceTitle: string;
  reDoUrls: string[] = [];
  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tool: ToolService,
    private job: JobService,
    private message: MessageService,
    private list: ListService,
    private string_length_pipe: StringLengthPipe,
    private translate: TranslateService,
    private memlist: MemListService,
    private alert: AlertService,
    private confirmationService: ConfirmationService,
    private api: ApiBaseService,
  ) {
    super(route, tr);
    this.sourceTitle = this.title;

    this.form = this.fb.group({
      startDate: new FormControl('', ValidatorFns.requiredValidator()),
      endDate: new FormControl('', ValidatorFns.requiredValidator()),
      keyword: new FormControl(''),
      status: new FormControl('')
    });
  }

  async ngOnInit() {

    this.converDateInit();
    const code = ['add_job_id', 'item_no', 'subitem_no', 'identif_data', 'status', 'job_start_date', 'job_step', 'exec_result', 'create_date', 'update_date', 'type', 'task', 'init_time'];
    const dict = await this.tool.getDict(code);
    this.cols = [
      { field: 'apptJobId', header: dict['add_job_id'], width: '5%' },
      { field: 'refItemNo', header: dict['type'], width: '20%' },
      // { field: 'refSubitemNo', header: dict['subitem_no'], width: '10%' },
      { field: 'identifData', header: dict['identif_data'], width: '30%', type: this.string_length_pipe },
      { field: 'status', header: dict['status'], width: '50%' },
      { field: 'startDateTime', header: dict['job_start_date'], width: '10%' },
      { field: 'jobStep', header: dict['job_step'], width: '10%' },
      // { field: 'execResult', header: dict['exec_result'], width: '10%' },
      { field: 'createDateTime', header: dict['create_date'], width: '10%' },
      // { field: 'updateDateTime', header: dict['update_date'], width: '10%' }
    ];
    let ReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('JOB_STATUS')) + ',' + 10,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _status: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            _status.push({ label: item.subitemName, value: item.subitemNo });
          }
        }
        this.statusOption = _status;
      }
    });
    this.dataList = [];
    this.rowcount = this.dataList.length;
    let queryReqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      keyword: this.keyword!.value,
      status: this.status!.value
    } as DPB0058Req;
    this.job.queryJobLikeList_ignore1298(queryReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.rowcount = this.dataList.length;
      }
    });

    //移除此流程
    // this.job.queryRefeshMemListUrls().subscribe(res => {
    //   this.rerfeshMemListUrls = this.analysisMemListUrls(res.RespBody.refreshMemListUrls);
    //   this.reDoUrls = this.analysisMemListUrls(res.RespBody.redoUrls);
    // })

  }
  private analysisMemListUrls(urls: Array<string>): Array<string> {
    const url = new URL(env.apiUrl);
    const ip = url.hostname;
    const port = url.port;
    const newUrls = urls.map(url => {
      url = url.replace('{{ip}}', ip).replace('{{port}}', port);
      return url
    });
    return newUrls;
  }

  converDateInit() {
    let date = new Date();
    this.startDate!.setValue(this.tool.addDay(date, -6));
    this.endDate!.setValue(date);
    // this.currentMonth = this.startDate.value.getMonth() + 1;
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

  create() {
    const codes = ['dialog.create', 'message.job', 'message.create', 'message.success'];
    this.translate.get(codes).subscribe(dict => {
      this.dialogTitle = dict['dialog.create'];
      this.createData = {
        operate: FormOperate.create,
        displayInDialog: true,
        afterCloseCallback: (res) => {
          if (res && this.tool.checkDpSuccess(res.ResHeader)) {
            this.message.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.job']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
            this.submitForm();
          }
        }
      }
      // this._dialog.open(JobFormComponent, data);
      this.PageNum = EmPageBlock.CREATE;
      this.title = this.title + ' > ' + this.dialogTitle;
    });
  }

  submitForm() {
    this.dataList = [];
    this.rowcount = this.dataList.length;
    let ReqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      keyword: this.keyword!.value,
      status: this.status!.value
    } as DPB0058Req;
    this.job.queryJobLikeList(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.rowcount = this.dataList.length;
        // this.callUrls();
      }
    });

  }
  /**
   *https://docs.google.com/spreadsheets/d/1Y6zdsAOyYR5CCTWkXbJY06Y9q4I-tKYisG8CHzMaLuM/edit#gid=56203550&range=B27
    在按下 [搜尋] 或是 [執行]、[取消] 而刷新資料清單時，需逐筆呼叫 refreshMemListUrls
   */
  callUrls(): void {
    const obsRerfeshMemListUrls = this.rerfeshMemListUrls.map(rerfeshMemListUrl => {
      return this.job.jobPost(rerfeshMemListUrl);
    })
    forkJoin(Array.from(obsRerfeshMemListUrls)).subscribe();
  }

  async showDialog(rowData: DPB0058RespItem, operation: FormOperate) {
    const codes = ['dialog.detail_query', 'cfm_run_job', 'cfm_cancel_job', 'cfm_redo_job'];
    let dict = await this.tool.getDict(codes);
    this.jobData = rowData;
    switch (operation) {
      case FormOperate.detail:
        this.dialogTitle = dict['dialog.detail_query'];
        this.job.queryByPk({ apptJobId: this.jobData.apptJobId }).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            // this._dialog.open(JobDetailComponent, res.RespBody);
            this.detailData = res.RespBody;
            this.title = this.title + ' > ' + this.dialogTitle;
            this.PageNum = EmPageBlock.DETAIL;
          }
        });
        break;
      case FormOperate.run:
        // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_run_job'], detail: `Job ID ：${this.jobData.apptJobId}` });
        this.confirmationService.confirm({
          header: dict['cfm_run_job'],
          message: `Job ID ：${this.jobData.apptJobId}`,
          accept: () => {
              this.onConfirm('do');
          }
        });
        break;
      case FormOperate.cancel:
        // this.message.add({ key: 'delete', sticky: true, severity: 'warn', summary: dict['cfm_cancel_job'], detail: `Job ID ：${this.jobData.apptJobId}` });
        this.confirmationService.confirm({
          header: dict['cfm_cancel_job'],
          message: `Job ID ：${this.jobData.apptJobId}`,
          accept: () => {
              this.onConfirm('cancel');
          }
        });
        break;
      case FormOperate.redo:
        // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_redo_job'], detail: `Job ID ：${this.jobData.apptJobId}` });
        this.confirmationService.confirm({
          header: dict['cfm_redo_job'],
          message: `Job ID ：${this.jobData.apptJobId}`,
          accept: () => {
              this.onConfirm('do');
          }
        });
        break;
    }
  }
  changePage(val: any) {
    this.PageNum = val;
    this.title = this.sourceTitle;
    this.submitForm();
  }
  headerReturn(){
    this.PageNum = EmPageBlock.QUERYPAGE;
    this.title = this.sourceTitle;
    this.submitForm();
  }


  async onConfirm(toastKey: string) {
    this.message.clear();
    switch (toastKey) {
      case 'do':
        await this.doJob();
        break;
      case 'cancel':
        this.cancelJob();
        break;
    }
  }

  async doJob() {
    const path = `${env.apiUrl}/${this.job.basePath}/DPB0059`;
    this.job.doJobByPk({ apptJobId: this.jobData!.apptJobId }, path).subscribe((ajaxResp: any[]) => {

        const parseObj = JSON.parse(JSON.stringify(ajaxResp));
        let success;
        let fail;
        if(parseObj.response.ResHeader.rtnMsg == null){
          success = parseObj.response;
        }
        if(parseObj.response.ResHeader.rtnCode === "1491" || parseObj.response.ResHeader.rtnCode === "1357"){
          fail = parseObj.response;
        }
        // console.log(parseObj)
        // console.log(fail)
          if (success) {
            let _status = this.jobData!.status;
            const codes = ['message.run', 'message.redo', 'message.job', 'message.success'];
            this.tool.getDict(codes).then(dicts => {
              this.message.add({ severity: 'success', summary: `${_status.includes('W') || _status.includes('C') ? dicts['message.run'] : dicts['message.redo']} ${dicts['message.job']}`, detail: `${_status.includes('W') || _status.includes('C') ? dicts['message.run'] : dicts['message.redo']} ${dicts['message.success']}!` });
              this.submitForm();
            });
          } else if (fail) {
            this.alert.ok(`Return code : ${fail.ResHeader.rtnCode}`, fail.ResHeader.rtnMsg);
            let req_0206 = this.tool.getEventLog('AC2', fail.ResHeader.rtnMsg) as AA0206Req;
            this.api.eventLog(req_0206);
          }
        })
    // if (!this.reDoUrls.includes(path)) {
    //   this.reDoUrls.splice(0, 0, path);
    // }

    // const ajaxs$ = this.reDoUrls.map(reDoUrl => this.job.doJobByPk({ apptJobId: this.jobData!.apptJobId }, reDoUrl).pipe(
    //   catchError(err => of(err))
    // ));
    // forkJoin(Array.from(ajaxs$))
    //   .subscribe((ajaxResp: any[]) => {
    //     console.log(ajaxResp);
    //     const success = ajaxResp.find(a => a.response.ResHeader.rtnMsg == null);
    //     const fail = ajaxResp.find(f => f.response.ResHeader.rtnCode === "1491");
    //     if (success) {
    //       let _status = this.jobData!.status;
    //       const codes = ['message.run', 'message.redo', 'message.job', 'message.success'];
    //       this.tool.getDict(codes).then(dicts => {
    //         this.message.add({ severity: 'success', summary: `${_status.includes('W') || _status.includes('C') ? dicts['message.run'] : dicts['message.redo']} ${dicts['message.job']}`, detail: `${_status.includes('W') || _status.includes('C') ? dicts['message.run'] : dicts['message.redo']} ${dicts['message.success']}!` });
    //         this.submitForm();
    //       });
    //     } else if (fail) {
    //       this.alert.ok(`Return code : ${fail.response.ResHeader.rtnCode}`, fail.response.ResHeader.rtnMsg);
    //     }
    //   })
  }

  cancelJob() {
    this.job.queryByPk({ apptJobId: this.jobData!.apptJobId }).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let ReqBody = {
          apptJobId: res.RespBody.apptJobId,
          lv: res.RespBody.lv
        } as DPB0060Req;
        this.job.cacelJobByPk(ReqBody).subscribe(async res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            const codes = ['message.cancel', 'message.job', 'message.success'];
            const dicts = await this.tool.getDict(codes);
            this.message.add({ severity: 'success', summary: `${dicts['message.cancel']} ${dicts['message.job']}`, detail: `${dicts['message.cancel']} ${dicts['message.success']}!` });
            this.submitForm();
          }
        });
      }
    });
  }

  onReject() {
    this.message.clear();
  }

  moreData() {
    let ReqBody = {
      apptJobId: this.dataList[this.dataList.length - 1].apptJobId,
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      keyword: this.keyword!.value,
      status: this.status!.value
    } as DPB0058Req;
    this.job.queryJobLikeList(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = this.dataList.concat(res.RespBody.dataList);
        this.rowcount = this.dataList.length;
      }
    });
  }

  async copyIdentifData(identifData: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.tool.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = identifData;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.message.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  switchOri(item:any){
    if(!item.t){
      item.t = true
    }
    else{
      item.t = !item.t
    }
  }

  public get startDate() { return this.form.get("startDate"); }
  public get endDate() { return this.form.get("endDate"); }
  public get keyword() { return this.form.get("keyword"); }
  public get status() { return this.form.get("status"); }

}
enum EmPageBlock {
  QUERYPAGE, CREATE, DETAIL
}
