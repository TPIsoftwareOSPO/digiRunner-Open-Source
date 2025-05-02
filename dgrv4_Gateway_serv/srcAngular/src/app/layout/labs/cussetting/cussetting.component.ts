import { DPB9914Req } from './../../../models/api/ServerService/dpb9914.interface';
import { DPB9913Req } from './../../../models/api/ServerService/dpb9913.interface';
import { DPB9911Req } from './../../../models/api/ServerService/dpb9911.interface';
import { DPB9912Req } from './../../../models/api/ServerService/dpb9912.interface';
import { DPB9910Req, DPB9910Item } from './../../../models/api/ServerService/dpb9910.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { FileService } from 'src/app/shared/services/api-file.service';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
// import { ModuleService } from 'srcAngular/app/shared/services/api-module.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
  selector: 'app-cussetting',
  templateUrl: './cussetting.component.html',
  styleUrls: ['./cussetting.component.css'],
  providers: [FileService, ApiService, ConfirmationService]
})
export class CussettingComponent extends BaseComponent implements OnInit {

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  formUpdate!: FormGroup;
  toastValue: any;
  cols: { field: string; header: string }[] = [];
  tableData: Array<DPB9910Item> = [];
  tableDataRowcount: number = 0;
  delData?: DPB9910Item;
  currentAction: string = '';
  btnName: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private translateService: TranslateService,
    private messageService: MessageService,
    private alertService: AlertService,
    // private moduleService: ModuleService,
    private fileService: FileService,
    private api: ApiBaseService,
    private apiService: ApiService,
    private serverService: ServerService,
    private router:Router,
    private confirmationService: ConfirmationService
  ) {
    super(route, tr);
  }

  ngOnInit() {
    // this.checkOrgId();

    this.form = this.fb.group({
      keyword: new FormControl(''),
      settingNo: new FormControl(''),
      settingName: new FormControl(''),
      subsettingNo: new FormControl(''),
      subsettingName: new FormControl(''),
      sortBy: new FormControl(''),
      isDefault: new FormControl(''),
      param1: new FormControl(''),
      param2: new FormControl(''),
      param3: new FormControl(''),
      param4: new FormControl(''),
      param5: new FormControl(''),
      version: new FormControl(''),
    });

    this.init();
  }

  queryCusSettingList() {
    let ReqBody = {
      keyword: this.keyword.value,
    } as DPB9910Req;
    this.serverService.queryCusSettingList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.itemList;
        this.tableDataRowcount = this.tableData.length;
      }
      else {
        this.tableData = [];
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  async init() {
    const code = ['cate_no', 'cate_name', 'subitem_seqno', 'subitem_name', 'sort'];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'settingNo', header: dict['cate_no'] },
      { field: 'settingName', header: dict['cate_name'] },
      { field: 'subsettingNo', header: dict['subitem_seqno'] },
      { field: 'subsettingName', header: dict['subitem_name'] },
      { field: 'sortBy', header: dict['sort'] },
    ]
    this.tableData = [];
    this.tableDataRowcount = this.tableData.length;
    let ReqBody = {
      keyword: this.keyword.value,
    } as DPB9910Req;
    this.serverService.queryCusSettingList_ignore1298(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.itemList;
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  checkOrgId()
  {
    const tokenPool = this.toolService.getToken().split('.');
    const token = this.toolService.Base64Decoder(tokenPool[1]);
    const tokenParse = JSON.parse(token);

    if(tokenParse.org_id !== "100000")
    {
      this.router.navigateByUrl('/about');
    }
  }

  async copyOriData(identifData: string, target:any) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.toolService.getDict(code);
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
    this.messageService.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  toggleOriData(row:DPB9910Item,idx:number){
    console.log(idx)

  }

  async changePage(action: string, rowData?: DPB9910Item) {
    this.currentAction = action;
    const code = ['button.create', 'button.update', 'button.delete', 'button.detail'];
    const dict = await this.toolService.getDict(code);
    this.isDefault.enable()
    this.resetFormValidator(this.form);

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];

        this.serverService.addCusSetting_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
          }
        });
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 2;
        this.btnName = dict['button.update'];

        let reqBodyU = {
          settingNo: rowData?.settingNo,
          subsettingNo: rowData?.subsettingNo
        } as DPB9911Req;
        this.serverService.queryCusSettingDetail(reqBodyU).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {

            this.settingNo.setValue(res.RespBody.settingNo);
            this.settingName.setValue(res.RespBody.settingName);
            this.subsettingNo.setValue(res.RespBody.subsettingNo);
            this.subsettingName.setValue(res.RespBody.subsettingName);
            this.sortBy.setValue(res.RespBody.sortBy);
            this.isDefault.setValue(res.RespBody.isDefault == 'V' ? true : false);
            this.param1.setValue(res.RespBody.param1);
            this.param2.setValue(res.RespBody.param2);
            this.param3.setValue(res.RespBody.param3);
            this.param4.setValue(res.RespBody.param4);
            this.param5.setValue(res.RespBody.param5);
            this.version.setValue(res.RespBody.version);

            this.serverService.updateCusSetting_before().subscribe(res => {
              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
              }
            });
          }
        });
        break;
      case 'delete':
        this.currentTitle = `${this.title} > ${dict['button.delete']}`;
        this.pageNum = 3;
        this.btnName = dict['button.delete'];

        let reqBodyD = {
          settingNo: rowData?.settingNo,
          subsettingNo: rowData?.subsettingNo
        } as DPB9911Req;
        this.serverService.queryCusSettingDetail(reqBodyD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.settingNo.setValue(res.RespBody.settingNo);
            this.settingName.setValue(res.RespBody.settingName);
            this.subsettingNo.setValue(res.RespBody.subsettingNo);
            this.subsettingName.setValue(res.RespBody.subsettingName);
            this.sortBy.setValue(res.RespBody.sortBy);
            this.isDefault.setValue(res.RespBody.isDefault == 'V' ? true : false);
            this.isDefault.disable();
            this.param1.setValue(res.RespBody.param1);
            this.param2.setValue(res.RespBody.param2);
            this.param3.setValue(res.RespBody.param3);
            this.param4.setValue(res.RespBody.param4);
            this.param5.setValue(res.RespBody.param5);
          }
        });

        break;

      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.pageNum = 3;
        this.btnName = dict['button.detail'];

        let reqBody = {
          settingNo: rowData?.settingNo,
          subsettingNo: rowData?.subsettingNo
        } as DPB9911Req;
        this.serverService.queryCusSettingDetail(reqBody).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.settingNo.setValue(res.RespBody.settingNo);
            this.settingName.setValue(res.RespBody.settingName);
            this.subsettingNo.setValue(res.RespBody.subsettingNo);
            this.subsettingName.setValue(res.RespBody.subsettingName);
            this.sortBy.setValue(res.RespBody.sortBy);
            this.isDefault.setValue(res.RespBody.isDefault == 'V' ? true : false);
            this.isDefault.disable();
            this.param1.setValue(res.RespBody.param1);
            this.param2.setValue(res.RespBody.param2);
            this.param3.setValue(res.RespBody.param3);
            this.param4.setValue(res.RespBody.param4);
            this.param5.setValue(res.RespBody.param5);
          }
        });

        break;
    }
  }

  getMoreData() {
    let ReqBody = {
      sortBy: this.tableData[this.tableData.length - 1].sortBy,
      keyword: this.keyword.value,
    } as DPB9910Req;
    this.serverService.queryCusSettingList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.itemList);
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  onToastClose(event) {
    this.toastValue = '';
  }

  async procData() {
    console.log(this.currentAction)
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);
    switch (this.currentAction) {
      case 'create':
        let reqBodyC = {
          settingNo: this.settingNo.value,
          settingName: this.settingName.value,
          subsettingNo: this.subsettingNo.value,
          subsettingName: this.subsettingName.value,
          sortBy: this.sortBy.value,
          isDefault: this.isDefault.value ? 'V' : null,
          param1: this.param1.value,
          param2: this.param2.value,
          param3: this.param3.value,
          param4: this.param4.value,
          param5: this.param5.value,
        } as DPB9912Req;
        // console.log(reqBodyC)
        this.serverService.addCusSetting(reqBodyC).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({ severity: 'success', summary: `${dict['message.create']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
            this.queryCusSettingList();
            this.changePage('query');
          }
        });
        break;
      case 'update':
        let reqBodyU = {
          oriSettingNo: this.settingNo.value,
          settingName: this.settingName.value,
          oriSubsettingNo: this.subsettingNo.value,
          subsettingName: this.subsettingName.value,
          sortBy: this.sortBy.value,
          isDefault: this.isDefault.value ? 'V' : null,
          param1: this.param1.value,
          param2: this.param2.value,
          param3: this.param3.value,
          param4: this.param4.value,
          param5: this.param5.value,
          version: this.version.value,
        } as DPB9913Req;
        this.serverService.updateCusSetting(reqBodyU).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success', summary: `${dict['message.update']}`, detail: `${dict['message.update']} ${dict['message.success']}!`
            });
            this.queryCusSettingList();
            this.changePage('query');
          }
        });
        break;
      case 'delete':
        this.deleteUser();
        break;
    }

  }

  async deleteUser() {
    const code = ['cfm_del'];
    const dict = await this.toolService.getDict(code);
    // this.messageService.add({ key: 'deleteMsg', sticky: true, severity: 'error', summary: dict['cfm_del'] });
    this.confirmationService.confirm({
      header: ' ',
      message: dict['cfm_del'],
      accept: () => {
          this.deleteConfirm();
      }
    });

  }

  deleteConfirm() {
    this.messageService.clear();
    let ReqBody = {
      settingNo: this.settingNo.value,
      subsettingNo: this.subsettingNo.value
    } as DPB9914Req;

    this.serverService.deleteCusSetting(ReqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete',  'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.delete']} `,
          detail: `${dict['message.delete']} ${dict['message.success']}!`
        });
        this.queryCusSettingList();
        this.changePage('query');
      }
    });
  }





  onReject() {
    this.messageService.clear();
  }

  originStringTable(item: any) {
    return !item.ori ? item.val : item.t ? item.val : item.ori;
  }

  switchOri(item: any) {
    item.t = !item.t;
  }

  headerReturn(){
    this.changePage('query');
  }

  public get keyword() { return this.form.get('keyword')!; }
  public get id() { return this.form.get('id')!; }
  public get value() { return this.form.get('value')!; }
  public get newVal() { return this.form.get('newVal')!; }
  public get memo() { return this.form.get('memo')!; }

  public get settingNo() { return this.form.get('settingNo')!; }
  public get settingName() { return this.form.get('settingName')!; }
  public get subsettingNo() { return this.form.get('subsettingNo')!; }
  public get subsettingName() { return this.form.get('subsettingName')!; }
  public get sortBy() { return this.form.get('sortBy')!; }
  public get isDefault() { return this.form.get('isDefault')!; }
  public get param1() { return this.form.get('param1')!; }
  public get param2() { return this.form.get('param2')!; }
  public get param3() { return this.form.get('param3')!; }
  public get param4() { return this.form.get('param4')!; }
  public get param5() { return this.form.get('param5')!; }
  public get version() { return this.form.get('version')!; }

}
