import { DPB0144Resp, DPB0144RespLiveNode, DPB0144RespLostNode, DPB0144RespComposerNode } from './../../../models/api/ServerService/dpb0144.interface';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { AA0501Item, AA0501Req } from 'src/app/models/api/UtilService/aa0501.interface';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { MessageService } from 'primeng/api';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';

import * as ValidatorFns from '../../../shared/validator-functions';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { AboutService } from 'src/app/shared/services/api-about.service';
import {  DPB0118Resp } from 'src/app/models/api/AboutService/dpb0118.interface';
import * as dayjs from 'dayjs';

@Component({
  selector: 'app-ac0501',
  templateUrl: './ac0501.component.html',
  styleUrls: ['./ac0501.component.scss']
})
export class Ac0501Component extends BaseComponent implements OnInit {

  utc = require('dayjs/plugin/utc');
  timezone = require('dayjs/plugin/timezone');

  // form: FormGroup;
  // dataList: Array<AA0501Item> = new Array<AA0501Item>();
  // dbCacheNameOpt: { label: string; value: string; }[] = [];
  // pageNum: number = 1; // 1: 查詢、2: 清除快取
  currentTitle: string = this.title;
  // canRefresh: boolean = false;

  verInfo?:DPB0118Resp;
  monitorInfo?:DPB0144Resp;
  liveNodeList:Array<DPB0144RespLiveNode> = new Array();
  lostNodeList: Array<DPB0144RespLostNode> = new Array();
  composerNodeList: Array<DPB0144RespComposerNode> = new Array();
  period:string = "";

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private util: UtilService,
    private tool: ToolService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private roleService: RoleService,
    private list: ListService,
    private severService: ServerService,
    private aboutService:AboutService,
  ) {
    super(route, tr);

    dayjs.extend(this.utc)
    dayjs.extend(this.timezone)
    // this.form = this.fb.group({
    //   taskType: new FormControl('0'),
    //   cacheName: new FormControl(''),
    //   tableNames: new FormControl('')
    // });

    for(let i=0;i<3;i++){
      let tmp = {};
      this.liveNodeList.push(tmp);
    }
  }

  ngOnInit() {


    // let ReqBody = {
    //   encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('DB_CACHE_NAME')) + ',' + 27,
    //   isDefault: 'N'
    // } as DPB0047Req;
    // this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
    //   if (this.tool.checkDpSuccess(res.ResHeader)) {
    //     let _dbCacheNameOpt: { label: string, value: string }[] = [];
    //     if (res.RespBody.subItems) {
    //       for (let item of res.RespBody.subItems) {
    //         _dbCacheNameOpt.push({ label: item.subitemName, value: item.subitemNo });
    //       }
    //     }
    //     this.dbCacheNameOpt = _dbCacheNameOpt;
    //   }
    // });
    // this.roleService.queryRTMapByUk({ txIdList: ['AA0509'] } as DPB0115Req).subscribe(res => {
    //   if (this.tool.checkDpSuccess(res.ResHeader)) {
    //     this.canRefresh = res.RespBody.dataList.find(item => item.txId === 'AA0509') ? res.RespBody.dataList.find(item => item.txId === 'AA0509')!.available : false;
    //   }
    // });
    // let ReqBpody = {
    //   pastHours: 1
    // } as AA0501Req;
    // this.util.queryNodeList_ignore1298(ReqBpody).subscribe(res => {
    //   if (this.tool.checkDpSuccess(res.ResHeader)) {
    //     this.dataList = res.RespBody.dataList;
    //   }
    // });
    // this.taskType!.valueChanges.subscribe(value => {
    //   this.cacheName!.setValue('');
    //   this.tableNames!.setValue('');
    //   switch (value) {
    //     case '0':
    //       this.cacheName!.disable();
    //       $('#cacheName_label').removeClass('required');
    //       this.cacheName!.clearValidators();
    //       this.cacheName!.updateValueAndValidity();
    //       this.tableNames!.disable();
    //       $('#tableNames_label').removeClass('required');
    //       this.tableNames!.clearValidators();
    //       this.tableNames!.updateValueAndValidity();
    //       break;
    //     case '1':
    //       this.cacheName!.enable();
    //       $('#cacheName_label').addClass('required');
    //       this.cacheName!.setValidators(ValidatorFns.requiredValidator());
    //       this.cacheName!.updateValueAndValidity();
    //       this.tableNames!.disable();
    //       $('#tableNames_label').removeClass('required');
    //       this.tableNames!.clearValidators();
    //       this.tableNames!.updateValueAndValidity();
    //       break;
    //     case '2':
    //       this.cacheName!.disable();
    //       $('#cacheName_label').removeClass('required');
    //       this.cacheName!.clearValidators();
    //       this.cacheName!.updateValueAndValidity();
    //       this.tableNames!.enable();
    //       $('#tableNames_label').addClass('required');
    //       this.tableNames!.setValidators(ValidatorFns.requiredValidator());
    //       this.tableNames!.updateValueAndValidity();
    //       break;
    //   }
    // });

    this.severService.queryMonitor().subscribe(res=>{
        if(this.tool.checkDpSuccess(res.ResHeader)){
          this.monitorInfo = res.RespBody;
          if(res.RespBody.liveNodeList) this.liveNodeList = res.RespBody.liveNodeList;
          if(res.RespBody.lostNodeList) this.lostNodeList = res.RespBody.lostNodeList;
          if(res.RespBody.liveComposerList) this.composerNodeList = res.RespBody.liveComposerList;
          this.period = res.RespBody.period;
        }
    })

    this.aboutService.queryModuleVersion().subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.verInfo = res.RespBody;
      }

    });

  }

  formateDate(date:Date){

    return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
  }
  // queryNodeStatus() {
  //   let ReqBpody = {
  //     pastHours: 1
  //   } as AA0501Req;
  //   this.util.queryNodeList(ReqBpody).subscribe(res => {
  //     if (this.tool.checkDpSuccess(res.ResHeader)) {
  //       this.dataList = res.RespBody.dataList;
  //     }
  //   });
  // }

  // refresh() {
  //   let ReqBody = {
  //     taskType: this.taskType!.value
  //   } as AA0509Req;
  //   if (this.taskType!.value == '1') {
  //     ReqBody.cacheName = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.cacheName!.value)) + ',' + this.convertCacheNameIndex(this.cacheName!.value)
  //   }
  //   if (this.taskType!.value == '2') {
  //     ReqBody.tableNames = this.tableNames!.value;
  //   }
  //   // console.log('AA0509 Req:', ReqBody)
  //   this.util.clearDBCache(ReqBody).subscribe(async res => {
  //     if (this.tool.checkDpSuccess(res.ResHeader)) {
  //       const codes = ['clear_db_cache', 'message.clear', 'message.success'];
  //       const dict = await this.tool.getDict(codes);
  //       this.messageService.add({ severity: 'success', summary: dict['clear_db_cache'], detail: `${dict['message.clear']} ${dict['message.success']}!` });
  //       this.changePage('query');
  //     }
  //   });
  // }

  // convertCacheNameIndex(cacheName: string): number {
  //   switch (cacheName) {
  //     case 'userrolfunc':
  //       return 0;
  //     case 'clientgroupapi':
  //       return 1;
  //     case 'systemothers':
  //       return 2;
  //     default:
  //       return -1;
  //   }
  // }

  // async changePage(action: string) {
  //   const code = ['clear_db_cache'];
  //   const dict = await this.tool.getDict(code);
  //   this.resetFormValidator(this.form);
  //   switch (action) {
  //     case 'query':
  //       this.currentTitle = this.title;
  //       this.pageNum = 1;
  //       break;
  //     case 'refresh':
  //       this.currentTitle = `${this.title} > ${dict['clear_db_cache']}`;
  //       this.pageNum = 2;
  //       this.taskType!.setValue('0');
  //       break;
  //   }
  // }

  // public get taskType() { return this.form.get('taskType'); };
  // public get cacheName() { return this.form.get('cacheName'); };
  // public get tableNames() { return this.form.get('tableNames'); };

}
