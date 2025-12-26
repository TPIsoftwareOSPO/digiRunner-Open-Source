
import { SelectItem } from 'primeng/api';
import { ServerService } from './../../../shared/services/api-server.service';
import { ReqDPB0127, DPB0127Req, DPB0127RespItem } from './../../../models/api/ServerService/dpb0127.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormGroup, FormControl, FormBuilder } from '@angular/forms';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit, ViewChild, ViewContainerRef, ComponentFactoryResolver } from '@angular/core';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as dayjs from 'dayjs';
import { DPB0128Req, DPB0128RespItem } from 'src/app/models/api/ServerService/dpb0128.interface';
import { ParamItemComponent } from './param-item/param-item.component';

@Component({
  selector: 'app-ac0509',
  templateUrl: './ac0509.component.html',
  styleUrls: ['./ac0509.component.css']
})
export class Ac0509Component extends BaseComponent implements OnInit {
  @ViewChild('paramitem', { read: ViewContainerRef }) paramitemRef!: ViewContainerRef;

  currentTitle: string = this.title;
  pageType: string = 'default'
  form: FormGroup;
  dateSmaxDate: Date = new Date();
  dateEminDate: Date = new Date();
  currentDate: Date = new Date();
  cols: { field: string; header: string; width?: string; }[] = [];
  tableData: Array<DPB0127RespItem> = [];
  // = [
  //   {
  //     auditLongId: 1001,
  //     txnUid: 'txnUid1',
  //     userName: 'string',
  //     clientId: 'string',
  //     apiUrl: 'string',
  //     eventNo: 'string',
  //     eventName: 'string',
  //     userIp: 'string',
  //     userHostname: 'string',
  //     userRole: 'string',
  //     param1: 'string',
  //     param2: 'string',
  //     param3: 'string',
  //     param4: 'string',
  //     param5: 'string',
  //     stackTrace: 'string',
  //     isUserRoleTruncated: true,
  //     truncatedUserRole: 'string',
  //     createDateTime: '2022/02/01 13:13',
  //   }
  // ];

  selectItem: DPB0127RespItem = {
    auditLongId: '',
    txnUid: '',
    userName: '',
    clientId: '',
    apiUrl: '',
    origApiUrl: '',
    eventNo: '',
    eventName: '',
    userIp: '',
    userHostname: '',
    userRole: '',
    param1: '',
    param2: '',
    param3: '',
    param4: '',
    param5: '',
    stackTrace: '',
    isUserRoleTruncated: null,
    truncatedUserRole: '',
    createDateTime: '',
    auditExtId: ''
  };

  // dpp0128Data:Array<DPB0128RespItem> = [ {
  //   "auditLongId" : 10002,
  //   "txnUid" : "txnUid",
  //   "entityName" : "entityName2",
  //   "cud" : "D",
  //   "cudName" : "刪除",
  //   "param1" : null,
  //   "param2" : null,
  //   "param3" : null,
  //   "param4" : null,
  //   "param5" : null,
  //   "stackTrace" : null,
  //   "oldRowContent" : "oldRow2",
  //   "oldRowBlob" : "b2xkUm93Mg==",
  //   "newRowContent" : "newRow2",
  //   "newRowBlob" : "bmV3Um93Mg=="
  // }, {
  //   "auditLongId" : 10001,
  //   "txnUid" : "txnUid",
  //   "entityName" : "entityName1",
  //   "cud" : "C",
  //   "cudName" : "新增",
  //   "param1" : null,
  //   "param2" : null,
  //   "param3" : null,
  //   "param4" : null,
  //   "param5" : null,
  //   "stackTrace" : null,
  //   "oldRowContent" : "oldRow1",
  //   "oldRowBlob" : "b2xkUm93MQ==",
  //   "newRowContent" : "newRow2",
  //   "newRowBlob" : "bmV3Um93Mg=="
  // } ];

  generated: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private ngxService: NgxUiLoaderService,
    private serverService: ServerService,
    private factoryResolver: ComponentFactoryResolver,
  ) {
    super(route, tr);
    this.form = this.fb.group({
      startDate: new FormControl(''),
      endDate: new FormControl(''),
      keywords: new FormControl('')
    });

  }
  async ngOnInit() {



    this.serverService.querySALMaster_before().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.addFormValidator(this.form, res.RespBody.constraints);
      }
    });

    this.startDate!.valueChanges.subscribe(res => {
      if (res == null) this.startDate!.setValue('');
    })

    this.endDate!.valueChanges.subscribe(res => {
      if (res == null) this.endDate!.setValue('');
    })

    let dateS = new Date();
    dateS.setHours(0, 0, 0, 0);
    this.startDate!.setValue(this.toolService.addDay(dateS, -1));
    this.endDate!.setValue(this.toolService.addDay(dateS, 1));

    const code = ['audit_id', 'client_id', 'api_url', 'api_event', 'user_ip', 'user_role', 'event_create_datetime'];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'auditLongId', header: dict['audit_id'], width: '15%' },
      { field: 'clientId', header: dict['client_id'], width: '10%' },
      { field: 'apiUrl', header: dict['api_url'], width: '20%' },
      { field: 'eventNo', header: dict['api_event'], width: '20%' },
      { field: 'userIp', header: dict['user_ip'], width: '15%' },
      { field: 'userRole', header: dict['user_role'], width: '10%' },
      { field: 'createDateTime', header: dict['event_create_datetime'], width: '10%' },
    ];

    // this.form.valueChanges.subscribe(() => {
    //   this.tableData = [];
    // })

    let reqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
    } as DPB0127Req;
    this.serverService.querySALMaster_ignore1298(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
      }
      else {
        this.tableData = [];
      }
    });

  }

  submitForm() {
    this.ngxService.start();
    this.tableData = [];
    let reqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      keywords: this.keywords!.value,
    } as DPB0127Req;

    this.serverService.querySALMaster(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
      }
      this.ngxService.stop();
    });
  }

  moreData() {
    this.ngxService.start();
    let reqBody = {
      startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
      endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
      auditLogId: this.tableData[this.tableData.length - 1].auditLongId,
      auditExtId: this.tableData[this.tableData.length - 1].auditExtId,
      keywords: this.keywords!.value
    } as DPB0127Req;

    this.serverService.querySALMaster(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.dataList);
      }
      this.ngxService.stop();
    });
  }

  async changePage(action: string, rowData?: DPB0127RespItem) {

    const codes = ['detail'];
    const dict = await this.toolService.getDict(codes);

    this.currentTitle = action == 'default' ? this.title : `${this.title} > ${dict['detail']}`;


    this.paramitemRef.clear();
    this.generated = false;
    this.pageType = action;
    if (rowData) {
      this.selectItem = rowData;
    }
  }

  getDetailData() {
    let reqBody = {
      txnUid: this.selectItem.txnUid
    } as DPB0128Req;
    this.serverService.querySALDetail(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        res.RespBody.dataList.forEach(item => {
          this.generateParamContent(item)
          this.generated = true;
        });
      }
    })
  }

  generateParamContent(item: DPB0128RespItem) {
    // var componentFactory = this.factoryResolver.resolveComponentFactory(ParamItemComponent);
    let componentRef = this.paramitemRef.createComponent(ParamItemComponent);
    componentRef.instance.paramItem = item;
  }

  onTodayClick(ctrlname: string) {
    this.form.get(ctrlname)!.setValue(new Date());
  }

  preMonth() {
    let sDate = new Date(this.currentDate);
    sDate.setMonth(sDate.getMonth() - 1, 1);
    sDate.setHours(0, 0, 0, 0);
    this.startDate!.setValue(sDate);
    let eDate = new Date(this.startDate!.value);
    eDate.setMonth(sDate.getMonth() + 1, eDate.getDate() - 1);
    eDate.setHours(23, 59, 59, 999)
    this.endDate!.setValue(eDate);
    this.currentDate.setMonth(this.currentDate.getMonth() - 1);
    this.submitForm();
  }

  nextMonth() {
    let sDate = new Date(this.currentDate);
    sDate.setMonth(sDate.getMonth() + 1, 1);
    sDate.setHours(0, 0, 0, 0);
    this.startDate!.setValue(sDate);
    let eDate = new Date(this.startDate!.value);
    eDate.setMonth(sDate.getMonth() + 1, eDate.getDate() - 1);
    eDate.setHours(23, 59, 59, 999)
    this.endDate!.setValue(eDate);
    this.currentDate.setMonth(this.currentDate.getMonth() + 1);
    this.submitForm();
  }

  headerReturn() {
    this.changePage('default');
  }

  public get startDate() { return this.form.get('startDate'); }
  public get endDate() { return this.form.get('endDate'); }
  public get keywords() { return this.form.get('keywords'); }

}
