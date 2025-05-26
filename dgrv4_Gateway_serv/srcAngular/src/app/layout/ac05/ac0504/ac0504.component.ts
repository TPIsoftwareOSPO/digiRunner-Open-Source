import { UtilService } from 'src/app/shared/services/api-util.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AA0525Item, AA0525Req } from 'src/app/models/api/UtilService/aa0525.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import * as dayjs from 'dayjs';
import { AA0505Item, AA0505Req, AA0505Resp } from 'src/app/models/api/UtilService/aa0505.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-ac0504',
  templateUrl: './ac0504.component.html',
  styleUrls: ['./ac0504.component.css']
})
export class Ac0504Component extends BaseComponent implements OnInit {

  form: FormGroup;
  cols: { field: string; header: string; }[] = [];
  dataList: Array<AA0525Item> = [];
  rowcount: number = 0;
  pageNum: number = 1;
  currentTitle: string = this.title; // 1: 查詢、2: 詳細資料
  canQuery: boolean = false;
  canStatus: boolean = false;
  tabTitles: { label: string; value: string; }[] = [];
  currentTaskStatus?: AA0505Resp;
  successOption: { label: string; value: string }[] = [];
  taskStatusCols: { field: string; header: string; }[] = [];
  taskStatusList: Array<AA0505Item> = [];
  taskStatusRowcount: number = 0;
  currentTaskSrc: string = 'Java';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tool: ToolService,
    private util: UtilService,
    private roleService: RoleService,
    private list: ListService,
    private messageService: MessageService
  ) {
    super(route, tr);

    const midnight = new Date((new Date).setHours(0, 0, 0, 0));
    this.form = this.fb.group({
      keyword: new FormControl(''),
      startTime: new FormControl(midnight),
      endTime: new FormControl(new Date()),
      taskSrc: new FormControl(this.currentTaskSrc),
      success: new FormControl('all')
    });

    this.tabTitles = [
      { label: 'Java', value: 'Java' },
      { label: '.NET', value: '.NET' }
    ];

  }

  ngOnInit() {


    // this.util.queryTaskList_1_before().subscribe(res => {
    //     if (this.tool.checkDpSuccess(res.ResHeader)) {
    //         this.addFormValidator(this.form, res.RespBody.constraints);
    //     }
    // });
    this.roleService.queryRTMapByUk({ txIdList: ['AA0525', 'AA0505'] } as DPB0115Req).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.canQuery = res.RespBody.dataList.find(item => item.txId === 'AA0525') ? res.RespBody.dataList.find(item => item.txId === 'AA0525')!.available : false;
        this.canStatus = res.RespBody.dataList.find(item => item.txId === 'AA0505') ? res.RespBody.dataList.find(item => item.txId === 'AA0505')!.available : false;
      }
    });
    let ReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('JOB_STATUS')) + ',' + 10,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['all'];
        const dict = await this.tool.getDict(code);
        let _success = [
          { label: dict['all'], value: 'all' }
        ];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            if (item.subitemNo == 'E' || item.subitemNo == 'D') {
              _success.push({ label: item.subitemName, value: item.subitemNo });
            }
          }
        }
        this.successOption = _success;
      }
    });
    this.init();
  }

  async init() {
    const codes = ['serial_no', 'task_id', 'task_name', 'task_type', 'notice_node', 'notice_time', 'comp_id', 'comp_time', 'node_name', 'update_time', 'taks_status', 'exec_msg'];
    const dict = await this.tool.getDict(codes);
    this.cols = [
      { field: 'id', header: dict['serial_no'] },
      { field: 'taskId', header: dict['task_id'] },
      { field: 'taskSignature', header: dict['task_name'] },
      { field: 'coordination', header: dict['task_type'] },
      { field: 'noticeNode', header: dict['notice_node'] },
      { field: 'noticeTime', header: dict['notice_time'] },
    ];
    this.taskStatusCols = [
      { field: 'competitiveId', header: dict['comp_id'] },
      { field: 'competitiveTime', header: dict['comp_time'] },
      { field: 'node', header: dict['node_name'] },
      { field: 'updateTime', header: dict['update_time'] },
      { field: 'successName', header: dict['taks_status'] },
      { field: 'errorMsg', header: dict['exec_msg'] }
    ];

    this.dataList = [];
    this.rowcount = this.dataList.length;
    let ReqBody = {
      keyword: this.keyword!.value,
      startTime: dayjs(this.startTime!.value).format('YYYY-MM-DD HH:mm'),
      endTime: dayjs(this.endTime!.value).format('YYYY-MM-DD HH:mm'),
      taskSrc: this.taskSrc!.value
    } as AA0525Req;
    this.util.queryTaskList_1_ignore1298(ReqBody).subscribe(res => {
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
      keyword: this.keyword!.value,
      startTime: dayjs(this.startTime!.value).format('YYYY-MM-DD HH:mm'),
      endTime: dayjs(this.endTime!.value).format('YYYY-MM-DD HH:mm'),
      taskSrc: this.taskSrc!.value
    } as AA0525Req;
    this.util.queryTaskList_1(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.rowcount = this.dataList.length;
      }
    });
  }

  moreTaskList() {
    let ReqBody = {
      id: this.dataList[this.dataList.length - 1].id,
      startTime: dayjs(this.startTime!.value).format('YYYY-MM-DD HH:mm'),
      endTime: dayjs(this.endTime!.value).format('YYYY-MM-DD HH:mm'),
      taskSrc: this.taskSrc!.value
    } as AA0525Req;
    this.util.queryTaskList_1(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = this.dataList.concat(res.RespBody.dataList);
        this.rowcount = this.dataList.length;
      }
    });
  }

  queryTaskStatus() {
    this.taskStatusList = [];
    this.taskStatusRowcount = this.taskStatusList.length;
    let ReqBody = {
      nodeTaskId: this.currentTaskStatus!.nodeTaskId,
      taskSrc: this.currentTaskStatus!.taskSrc,
      keyword: this.keyword!.value,
      success: this.success!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.success!.value)) + ',' + this.convertSuccessIndex(this.success!.value) : ''
    } as AA0505Req;
    this.util.queryTaskStatus(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.currentTaskStatus = res.RespBody;
        this.taskStatusList = this.currentTaskStatus.dataList;
        this.taskStatusRowcount = this.taskStatusList.length;
      }
    });
  }

  moreTaskStatus() {
    let ReqBody = {
      id: this.taskStatusList[this.taskStatusList.length - 1].id,
      nodeTaskId: this.currentTaskStatus?.nodeTaskId,
      taskSrc: this.currentTaskStatus?.taskSrc,
      keyword: this.keyword!.value,
      success: this.success!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.success!.value)) + ',' + this.convertSuccessIndex(this.success!.value) : ''
    } as AA0505Req;
    this.util.queryTaskStatus(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.taskStatusList = this.taskStatusList.concat(res.RespBody.dataList);
        this.taskStatusRowcount = this.taskStatusList.length;
      }
    });
  }

  async copyData(data: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.tool.getDict(code);
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

  convertSuccessIndex(success: string): number {
    switch (success) {
      case 'E':
        return 2;
      case 'D':
        return 3;
      default:
        return -1;
    }
  }

  changeTab(evn) {
    this.dataList = [];
    this.rowcount = this.dataList.length;
    this.currentTaskSrc = this.tabTitles[evn.index].value;
    this.taskSrc!.setValue(this.currentTaskSrc);
    this.submitForm();
  }

  async changePage(action: string, rowData?: AA0525Item) {
    const code = ['status'];
    const dict = await this.tool.getDict(code);
    switch (action) {
      case 'query':
        this.resetFormValidator(this.form);
        // this.util.queryTaskList_1_before().subscribe(res => {
        //     if (this.tool.checkDpSuccess(res.ResHeader)) {
        //         this.addFormValidator(this.form, res.RespBody.constraints);
        this.currentTitle = this.title;
        this.pageNum = 1;
        let midnight = new Date((new Date).setHours(0, 0, 0, 0));
        this.startTime!.setValue(midnight);
        this.endTime!.setValue(new Date());
        this.taskSrc!.setValue(this.currentTaskSrc);
        //     }
        // });
        break;
      case 'status':
        this.resetFormValidator(this.form);
        this.taskSrc!.setValue(this.currentTaskSrc);
        let ReqBody = {
          nodeTaskId: rowData!.id,
          taskSrc: this.taskSrc!.value,
          success: ''
        } as AA0505Req;
        this.util.queryTaskStatus(ReqBody).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.currentTaskStatus = res.RespBody;
            this.taskStatusList = this.currentTaskStatus.dataList;
            this.taskStatusRowcount = this.taskStatusList.length;
            this.currentTitle = `${this.title} > ${dict['status']}`;
            this.pageNum = 2;
            this.success!.setValue('all');
          }
        });
        break;
    }
  }

  public get keyword() { return this.form.get('keyword'); }
  public get startTime() { return this.form.get('startTime'); }
  public get endTime() { return this.form.get('endTime'); }
  public get taskSrc() { return this.form.get('taskSrc'); }
  public get success() { return this.form.get('success'); }

}
