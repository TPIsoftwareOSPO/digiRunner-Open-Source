import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { DPB0116Data, DPB0116Req } from 'src/app/models/api/MailService/dpb0116.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ListService } from 'src/app/shared/services/api-list.service';
import { MailService } from 'src/app/shared/services/api-mail.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { BaseComponent } from '../../base-component';
import * as dayjs from 'dayjs';
import { MessageService } from 'primeng/api';
import { DPB0117Req, DPB0117Resp } from 'src/app/models/api/MailService/dpb0117.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-np0516',
  templateUrl: './np0516.component.html',
  styleUrls: ['./np0516.component.css']
})
export class Np0516Component extends BaseComponent implements OnInit {

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：open api key 列表、2：open api key detail
  form: FormGroup;
  resultOption: { label: string; value: string; }[] = [];
  cols: { field: string; header: string; width: string; }[] = [];
  dataList: Array<DPB0116Data> = [];
  rowcount: number = 0;
  currentMailDetail?: DPB0117Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private listService: ListService,
    private toolService: ToolService,
    private mailService: MailService,
    private messageService: MessageService,
    private ngxService: NgxUiLoaderService,
  ) {
    super(route, tr);

    this.form = this.fb.group({
      startDate: new FormControl(''),
      endDate: new FormControl(''),
      keyword: new FormControl(''),
      result: new FormControl('-1')
    });
  }


  ngOnInit() {

    // 寄件結果
    let ReqBody = {
      encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('RESULT_FLAG')) + ',' + 39,
      isDefault: 'N'
    } as DPB0047Req;

    this.listService.querySubItemsByItemNo(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        let _resultOption: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            _resultOption.push({ label: item.subitemName, value: item.subitemNo });
          }
        }
        this.resultOption = _resultOption;
        this.axios_queryMailLogList_ignore1298();
      }
    });
    this.init();
  }

  axios_queryMailLogList_ignore1298(){
    this.ngxService.start();
    let ReqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      result: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.result!.value)) + ',' + this.resultOption.findIndex(item => item.value == this.result!.value),
      keyword: this.keyword!.value
    } as DPB0116Req;

    this.mailService.queryMailLogList_ignore1298(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList.map(item => {
          return {
            maillogId: item.maillogId,
            recipients: item.recipients,
            subject: item.subject,
            createDate: dayjs(item.createDate).format('YYYY-MM-DD HH:mm:ss'),
            result: item.result
          } as DPB0116Data;
        });
        this.rowcount = this.dataList.length;
      }
      this.ngxService.stopAll();
    });
  }

  async init() {
    this.converDateInit();
    const code = ['create_date', 'recipient', 'subject', 'mail_result', 'mail_log_id'];
    const dict = await this.toolService.getDict(code);

    this.cols = [
      { field: 'maillogId', header: dict['mail_log_id'], width: '10%' },
      { field: 'recipients', header: dict['recipient'], width: '25%' },
      { field: 'subject', header: dict['subject'], width: '40%' },
      { field: 'createDate', header: dict['create_date'], width: '15%' },
      { field: 'result', header: dict['mail_result'], width: '10%' }
    ];
  }

  converDateInit() {
    let date = new Date();
    this.startDate!.setValue(this.toolService.addDay(date, -6));
    this.endDate!.setValue(date);
  }

  submitForm() {
    this.dataList = [];
    this.rowcount = this.dataList.length;
    let ReqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      result: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.result!.value)) + ',' + this.resultOption.findIndex(item => item.value == this.result!.value),
      keyword: this.keyword!.value
    } as DPB0116Req;
    this.mailService.queryMailLogList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList.map(item => {
          return {
            maillogId: item.maillogId,
            recipients: item.recipients,
            subject: item.subject,
            createDate: dayjs(item.createDate).format('YYYY-MM-DD HH:mm:ss'),
            result: item.result
          } as DPB0116Data;
        });
        this.rowcount = this.dataList.length;
      }
    });
  }

  moreData() {
    let ReqBody = {
      id: this.dataList[this.dataList.length - 1].maillogId,
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      result: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.result!.value)) + ',' + this.resultOption.findIndex(item => item.value == this.result!.value),
      keyword: this.keyword!.value
    } as DPB0116Req;
    this.mailService.queryMailLogList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = this.dataList.concat(
          res.RespBody.dataList.map(item => {
            return {
              maillogId: item.maillogId,
              recipients: item.recipients,
              subject: item.subject,
              createDate: dayjs(item.createDate).format('YYYY-MM-DD HH:mm:ss'),
              result: item.result
            } as DPB0116Data;
          })
        );
        this.rowcount = this.dataList.length;
      }
    });
  }

  async copyData(data: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.toolService.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = data;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.messageService.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  async changePage(action: string, rowData?: DPB0116Data) {
    const code = ['button.detail'];
    const dict = await this.toolService.getDict(code);
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'detail':
        let ReqBody = {
          id: rowData!.maillogId,
          recipients: rowData!.recipients
        } as DPB0117Req;
        this.mailService.queryMailLogDetail(ReqBody).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentMailDetail = {
              mailLogId: res.RespBody.mailLogId,
              recipients: res.RespBody.recipients,
              subject: res.RespBody.subject,
              content: res.RespBody.content,
              result: res.RespBody.result,
              refCode: res.RespBody.refCode,
              createDate: dayjs(res.RespBody.createDate).format('YYYY-MM-DD HH:mm:ss'),
              createUser: res.RespBody.createUser,
              errorMsg : res.RespBody.errorMsg
            } as DPB0117Resp;
            this.currentTitle = `${this.title} > ${dict['button.detail']}`;
            this.pageNum = 2;
            console.log(this.currentMailDetail.errorMsg)
          }
        });
        break;
    }
  }

  headerReturn(){
    this.changePage('query');
  }

  originStringTable(item:any) {
    return !item.ori ? item.val : (item.t ? item.val : item.ori);
  }

  switchOri(item:any){
    item.t = !item.t;
  }

  public get startDate() { return this.form.get('startDate'); }
  public get endDate() { return this.form.get('endDate'); }
  public get keyword() { return this.form.get('keyword'); }
  public get result() { return this.form.get('result'); }

}
