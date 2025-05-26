import { DPB9904Req } from './../../../models/api/ServerService/dpb9904.interface';
import { DPB9902Req } from './../../../models/api/ServerService/dpb9902.interface';
import { ServerService } from './../../../shared/services/api-server.service';
import { ApiBaseService } from './../../../shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB9900Item, DPB9900Req } from './../../../models/api/ServerService/dpb9900.interface';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { FileService } from 'src/app/shared/services/api-file.service';

import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { combineLatest, forkJoin, Observable, of } from 'rxjs';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
// import { ModuleService } from 'srcAngular/app/shared/services/api-module.service';
import { concatMap, flatMap, map } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import * as dayjs from 'dayjs';
import { generate } from 'generate-password';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { DPB9901Req } from 'src/app/models/api/ServerService/dpb9901.interface';
import { DPB9903Req } from 'src/app/models/api/ServerService/dpb9903.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB9915Item, DPB9915Req } from 'src/app/models/api/ServerService/dpb9915.interface';
import { DPB9919Item, DPB9919Req } from 'src/app/models/api/ServerService/dpb9919.interface';
import { DPB9920Req } from 'src/app/models/api/ServerService/dpb9920.interface';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { CheckboxModule } from 'primeng/checkbox';
import { DPB9917Req } from 'src/app/models/api/ServerService/dpb9917.interface';
import { DPB9916Req } from 'src/app/models/api/ServerService/dpb9916.interface';
import { DPB9918Req } from 'src/app/models/api/ServerService/dpb9918.interface';
import { HttpHeaders } from '@angular/common/http';
import { AlertType } from 'src/app/models/common.enum';

@Component({
  selector: 'app-tsmpsetting',
  templateUrl: './tsmpdpfile.component.html',
  styleUrls: ['./tsmpdpfile.component.css'],
  providers: [FileService, ApiService, ConfirmationService]
})
export class TsmpdpFileComponent extends BaseComponent implements OnInit {

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  formAdd!: FormGroup;
  formBucket!: FormGroup;
  formUpdate!: FormGroup;
  toastValue: any;
  cols: { field: string; header: string }[] = [];
  tableData: Array<DPB9915Item> = [];
  tableDataRowcount: number = 0;
  tableDataBucket: Array<DPB9915Item> = [];
  tableDataRowcountBucket: number = 0;
  delData?: DPB9900Item;
  currentAction: string = '';
  btnName: string = '';

  dateSmaxDate: Date = new Date();
  dateEminDate: Date = new Date();
  fileClassifications: Array<{ label: string, value: string }> = new Array();
  fileClassificationsDict: { [key: string]: number } = {};
  selected: Array<DPB9915Item> = new Array();
  selectedItemMoveToBucket: DPB9915Item = {} as DPB9915Item;
  _fileData?: File | null;
  _fileName: string | null = null;
  isDeleteFile: boolean = false;
  dateSmaxDate_formBucket: Date = new Date();
  dateEminDate_formBucket: Date = new Date();
  bucketDeadline: string = '';
  isDisplayPreviewBlock: boolean = true;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private translateService: TranslateService,
    private messageService: MessageService,
    private tool: ToolService,
    private fileService: FileService,
    private api: ApiBaseService,
    private apiService: ApiService,
    private serverService: ServerService,
    private list: ListService,
    private router: Router,
    private message: MessageService,
    private translate: TranslateService,
    private file: FileService,
    private alert: AlertService,
    private confirmationService: ConfirmationService
  ) {
    super(route, tr);
  }

  ngOnInit() {

    // this.checkOrgId();

    this.form = this.fb.group({
      startDate: new FormControl('', [ValidatorFns.requiredValidator()]),
      endDate: new FormControl('', [ValidatorFns.requiredValidator()]),
      fileClassification: new FormControl(''),
      refId: new FormControl(''),
      keyword: new FormControl('')
    });

    this.formAdd = this.fb.group({
      refFileCateCode: new FormControl(''),
      refId: new FormControl(''),
      isTmpfile: new FormControl(''),
      fileName: new FormControl(''),
      tmpfileName: new FormControl(''),
    });

    this.formBucket = this.fb.group({
      startDate: new FormControl('', [ValidatorFns.requiredValidator()]),
      endDate: new FormControl('', [ValidatorFns.requiredValidator()]),
      fileClassification: new FormControl(''),
      refId: new FormControl(''),
      keyword: new FormControl('')
    });

    this.formUpdate = this.fb.group({
      fileId: new FormControl(''),
      fileName: new FormControl(''),
      refFileCateCode: new FormControl(''),
      refId: new FormControl(''),
      filePath: new FormControl(''),
      isBlob: new FormControl(''),
      createDateTime: new FormControl(''),
      createUser: new FormControl(''),
      updateUser: new FormControl(''),
      updateDateTime: new FormControl(''),
      blobData: new FormControl(''),
      version: new FormControl(''),
      tmpfileName: new FormControl(''),
    });

    this.converDateInit();
    this.startDate.valueChanges.subscribe(time => {
      this.dateEminDate = new Date(time);
    });
    this.endDate.valueChanges.subscribe(time => {
      this.dateSmaxDate = new Date(time);
    });

    this.dateS_formBucket.valueChanges.subscribe(time => {
      this.dateEminDate_formBucket = new Date(time);
    });
    this.dateE_formBucket.valueChanges.subscribe(time => {
      this.dateSmaxDate_formBucket = new Date(time);
    });

    combineLatest(this.queryDPBAPI0047()).subscribe((v) => {
      this.init();
    });

  }

  queryDPBAPI0047(): Observable<string> {
    let ReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('FILE_CATE_CODE')) + ',' + 55,
      isDefault: 'N'
    } as DPB0047Req;
    return this.list.querySubItemsByItemNo(ReqBody).pipe(map(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _fileClassifications: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            _fileClassifications.push({ label: item.subitemName, value: item.subitemNo });
          }
        }
        this.fileClassifications = _fileClassifications;
        //this.fileClassification.setValue(this.fileClassifications[0].value);
        let index: number = 0;
        this.fileClassifications.forEach((value: { label: string; value: string; }) => {
          this.fileClassificationsDict[value.value] = index++;
        });
      }
      return "success";
    }));
  }

  converDateInit() {
    let dateS = new Date();
    dateS.setHours(0, 0, 0, 0);
    this.startDate.setValue(dateS);
    this.endDate.setValue(this.tool.addDay(dateS, 1));
  }


  checkOrgId() {
    const tokenPool = this.toolService.getToken().split('.');
    const token = this.toolService.Base64Decoder(tokenPool[1]);
    const tokenParse = JSON.parse(token);

    if (tokenParse.org_id !== "100000") {
      this.router.navigateByUrl('/about');
    }
  }

  queryTsmpdpFileList() {
    let fileClassificationValue = this.fileClassification.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];

    let ReqBody = {
      startDate: dayjs(this.startDate.value).format('YYYY/MM/DD HH:mm'),
      endDate: dayjs(this.endDate.value).format('YYYY/MM/DD HH:mm'),
      refFileCateCode: (fileClassificationValue && fileClassificationValue !== '') ? this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex : null,
      refId: this.refId.value,
      keyword: this.keyword.value,
    } as DPB9915Req;
    this.serverService.queryTsmpdpFileList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
        this.tableDataRowcount = this.tableData.length;
      }
      else {
        this.tableData = [];
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  async init() {
    const code = ['tsmpdpfile_serial_number', 'file_name', 'refFileCate', 'refId', "lastUpdDateTime", "lastUpdUser"];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'fileId', header: dict['tsmpdpfile_serial_number'] },
      { field: 'fileName', header: dict['file_name'] },
      { field: 'refFileCateCode', header: dict['refFileCate'] },
      { field: 'refId', header: dict['refId'] },
      { field: 'lastUpdDateTime', header: dict['lastUpdDateTime'] },
      { field: 'lastUpdUser', header: dict['lastUpdUser'] },
    ]
    this.tableData = [];
    this.tableDataRowcount = this.tableData.length;

    let fileClassificationValue = this.fileClassification.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];
    let ReqBody = {
      startDate: dayjs(this.startDate.value).format('YYYY/MM/DD HH:mm'),
      endDate: dayjs(this.endDate.value).format('YYYY/MM/DD HH:mm'),
      //refFileCateCode: this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex,
      keyword: this.keyword.value,
    } as DPB9915Req;
    this.serverService.queryTsmpdpFileList_ignore1298(ReqBody).subscribe(res => {

      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
        this.tableDataRowcount = this.tableData.length;
      }

      this.serverService.queryTsmpdpFile_before().subscribe(res => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.addFormValidator(this.form, res.RespBody.constraints);
        }
      });

    });


  }

  async copyOriData(identifData: string) {
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

  async changePage(action: string, rowData?: DPB9915Item) {
    this.currentAction = action;
    const code = ['button.create', 'button.update', 'button.delete', 'button.resource_recycling_bucket'];
    const dict = await this.toolService.getDict(code);

    this.resetFormValidator(this.formAdd);

    this.resetFormValidator(this.formUpdate);

    this._fileName = null;
    this._fileData = null;
    this.isDisplayPreviewBlock = true;
    this.isDeleteFile = false;
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        this.queryTsmpdpFileList();
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];
        this.serverService.addTsmpdpFile_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.formAdd, res.RespBody.constraints);
          }
        });
        break;
      case 'update':
        let ReqBodyU = {
          fileId: rowData?.fileId,
        } as DPB9916Req;
        this.serverService.queryTsmpdpFileDetail(ReqBodyU).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.serverService.updateTsmpdpFile_before().subscribe(_res => {
              if (this.toolService.checkDpSuccess(_res.ResHeader)) {
                this.addFormValidator(this.formUpdate, _res.RespBody.constraints);
              }
            });

            this.currentTitle = `${this.title} > ${dict['button.update']}`;
            this.pageNum = 3;
            this.btnName = dict['button.update'];

            this.formUpdate.get("fileId")!.setValue(res.RespBody.fileId);
            this.formUpdate.get("fileName")!.setValue(res.RespBody.fileName);

            this.formUpdate.get("refFileCateCode")!.setValue(res.RespBody.refFileCateCode);
            this.formUpdate.get("refId")!.setValue(res.RespBody.refId);
            this.formUpdate.get('filePath')!.setValue(res.RespBody.filePath);
            this.formUpdate.get('isBlob')!.setValue(res.RespBody.isBlob ? res.RespBody.isBlob : 'N');
            this.formUpdate.get('createDateTime')!.setValue(res.RespBody.createDateTime);
            this.formUpdate.get('createUser')!.setValue(res.RespBody.createUser);
            this.formUpdate.get('updateUser')!.setValue(res.RespBody.updateUser);
            this.formUpdate.get('updateDateTime')!.setValue(res.RespBody.updateDateTime);

            if (res.RespBody.blobData) {
              try {
                this.formUpdate.get('blobData')!.setValue(this.toolService.Base64Decoder(res.RespBody.blobData));
              } catch (error) {
                this.translate.get('cant_preview', {}).subscribe(i18n => {
                  this.formUpdate.get('blobData')!.setValue(i18n);
                });
              }
              this._fileName = res.RespBody.fileName;
            } else {
              this.translate.get('cant_preview', {}).subscribe(i18n => {
                this.formUpdate.get('blobData')!.setValue(i18n);
              });
            }
            if (!res.RespBody.isBlob && res.RespBody.isBlob !== 'Y') {
              this.isDisplayPreviewBlock = false;
            }

            this.formUpdate.get('version')!.setValue(res.RespBody.version);

          }
        });
        break;
      case 'delete':
        let ReqBodyD = {
          id: '',
        } as DPB9901Req;
        this.serverService.queryTsmpSettingDetail(ReqBodyD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle = `${this.title} > ${dict['button.delete']}`;
            this.pageNum = 4;
            this.btnName = dict['button.delete'];

          }
        });
        break;
      case 'resource_recycling_bucket':
        this.currentTitle = `${this.title} > ${dict['button.resource_recycling_bucket']}`;
        this.pageNum = 5;
        this.passMainPageParameter();
        this.initResourceRecyclingBucket();
        break;
    }
  }

  getMoreData() {
    let fileClassificationValue = this.fileClassification.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];
    let ReqBody = {
      fileId: this.tableData[this.tableData.length - 1].fileId,
      startDate: dayjs(this.startDate.value).format('YYYY/MM/DD HH:mm'),
      endDate: dayjs(this.endDate.value).format('YYYY/MM/DD HH:mm'),
      refFileCateCode: fileClassificationValue ? this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex : null,
      refId: this.refId.value,
      keyword: this.keyword.value,
    } as DPB9915Req;
    this.serverService.queryTsmpdpFileList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.dataList);
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  onToastClose(event) {
    this.toastValue = '';
  }

  async procData() {
    // console.log(this.currentAction)
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);
    switch (this.currentAction) {
      case 'create':
        if (this._fileData) {
          window.setTimeout(() => {
            this.file.uploadFile2(this._fileData!).subscribe(res => {
              if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.createAPI(code, dict, res.RespBody.tempFileName);
              }
            });
          });
        } else {
          this.createAPI(code, dict, null);
        }

        break;
      case 'update':
        if (this._fileData) {
          window.setTimeout(() => {
            this.file.uploadFile2(this._fileData!).subscribe(res => {
              if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.udpateAPI(code, dict, res.RespBody.tempFileName);
              }
            });
          });
        } else {
          this.udpateAPI(code, dict, null);
        }
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
  }

  async deleteConfirm() {
    let fileIdList = this.transformFileIdList(this.selected);
    this.deletePermanently(fileIdList);
    this.messageService.clear();
  }

  onReject() {
    this.messageService.clear();
  }

  public get keyword() { return this.form.get('keyword')!; }
  public get startDate() { return this.form.get('startDate')!; }
  public get endDate() { return this.form.get('endDate')!; }
  public get fileClassification() { return this.form.get('fileClassification')!; }
  public get refId() { return this.form.get('refId')!; }

  public get isTmpfile_add() { return this.formAdd.get('isTmpfile')!; }
  public get refFileCateCode_add() { return this.formAdd.get('refFileCateCode')!; }
  public get refId_add() { return this.formAdd.get('refId')!; };
  public get fileName_add() { return this.formAdd.get('fileName')!; };

  public get keyword_formBucket() { return this.formBucket.get('keyword')!; }
  public get dateS_formBucket() { return this.formBucket.get('startDate')!; }
  public get dateE_formBucket() { return this.formBucket.get('endDate')!; }
  public get fileClassification_formBucket() { return this.formBucket.get('fileClassification')!; }
  public get refId_formBucket() { return this.formBucket.get('refId')!; }
  public get refId_formUpdate() { return this.formUpdate.get('refId')!; }

  onClearClick(ctrlname: string) {
    this.form.get(ctrlname)!.setErrors({ error: 'required' });
  }

  onTodayClick(ctrlname: string) {
    this.form.get(ctrlname)!.setValue(new Date());
  }

  passMainPageParameter() {
    this.dateS_formBucket.setValue(this.startDate.value);
    this.dateE_formBucket.setValue(this.endDate.value);
    this.fileClassification_formBucket.setValue(this.fileClassification.value);
    this.refId_formBucket.setValue(this.refId.value);
    this.keyword_formBucket.setValue(this.keyword.value);
  }

  initResourceRecyclingBucket() {
    this.tableDataBucket = [];
    this.tableDataRowcountBucket = this.tableDataBucket.length;

    let fileClassificationValue = this.fileClassification_formBucket.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];
    let ReqBody = {
      startDate: dayjs(this.dateS_formBucket.value).format('YYYY/MM/DD HH:mm'),
      endDate: dayjs(this.dateE_formBucket.value).format('YYYY/MM/DD HH:mm'),
      refFileCateCode: fileClassificationValue ? this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex : null,
      refId: this.refId_formBucket.value,
      keyword: this.keyword_formBucket.value,
      isTmpfile: "Y",
    } as DPB9915Req;
    this.serverService.queryTsmpdpFileList_ignore1298(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableDataBucket = res.RespBody.dataList;
        this.tableDataRowcountBucket = this.tableDataBucket.length;
        this.translate.get('resource_recycling_bucket_deadline', { N: res.RespBody.autoDeleteDays }).subscribe(i18n => {
          this.bucketDeadline = i18n;
        });
      }
    });

    this.serverService.queryTsmpdpFile_before().subscribe(_res => {
      if (this.toolService.checkDpSuccess(_res.ResHeader)) {
        this.addFormValidator(this.formBucket, _res.RespBody.constraints);
      }
    });
  }

  queryResourceRecyclingBucket() {
    this.tableDataBucket = [];
    this.tableDataRowcountBucket = this.tableDataBucket.length;

    let fileClassificationValue = this.fileClassification_formBucket.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];
    let ReqBody = {
      startDate: dayjs(this.dateS_formBucket.value).format('YYYY/MM/DD HH:mm'),
      endDate: dayjs(this.dateE_formBucket.value).format('YYYY/MM/DD HH:mm'),
      refFileCateCode: fileClassificationValue ? this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex : null,
      refId: this.refId_formBucket.value,
      keyword: this.keyword_formBucket.value,
      isTmpfile: "Y",
    } as DPB9915Req;
    this.serverService.queryTsmpdpFileList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableDataBucket = res.RespBody.dataList;
        this.tableDataRowcountBucket = this.tableDataBucket.length;
      }
    });
  }

  getMoreDataBucket() {
    let fileClassificationValue = this.fileClassification_formBucket.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];

    let ReqBody = {
      fileId: this.tableDataBucket[this.tableDataBucket.length - 1].fileId,
      startDate: dayjs(this.dateS_formBucket.value).format('YYYY/MM/DD HH:mm'),
      endDate: dayjs(this.dateE_formBucket.value).format('YYYY/MM/DD HH:mm'),
      refFileCateCode: fileClassificationValue ? this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex : null,
      refId: this.refId_formBucket.value,
      keyword: this.keyword_formBucket.value,
      isTmpfile: "Y",
    } as DPB9915Req;
    this.serverService.queryTsmpdpFileList(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableDataBucket = this.tableDataBucket.concat(res.RespBody.dataList);
        this.tableDataRowcountBucket = this.tableDataBucket.length;
      }
    });
  }

  async multiReductionPopup(): Promise<void> {
    // this.translate.get('cfm_reduction_tsmpdpFile', { count: this.selected.length }).subscribe(i18n => {
    //     this.messageService.add({ key: 'reductionPopup', sticky: true, severity: 'warn', summary: i18n });
    // });
    const code = ['cfm_reduction_tsmpdpFile', 'system_alert'];
    const dict = await this.toolService.getDict(code, { count: this.selected.length });

    this.confirmationService.confirm({
      header: dict['system_alert'],
      message: dict['cfm_reduction_tsmpdpFile'],
      accept: () => {
        this.reductionConfirm();
      }
    });


  }

  reductionConfirm() {
    let fileList = this.transformDPB9919Item(this.selected);
    this.reduction(fileList);
    this.messageService.clear();
  }

  private reduction(fileList: DPB9919Item[]): void {
    let ReqBody = {
      fileList: fileList,
      newIsTmpfile: 'N'
    } as DPB9919Req;
    this.serverService.reductionTsmpdpFile(ReqBody).subscribe(res => {
      this.queryResourceRecyclingBucket();
    });
  }

  transformDPB9919Item(rowData: Array<DPB9915Item>): Array<DPB9919Item> {
    let reductionList = new Array<DPB9919Item>();
    rowData.map(item => {
      let dpb9919Item = {} as DPB9919Item;
      dpb9919Item.fileId = item.fileId;
      dpb9919Item.oriIsTmpFile = 'Y';
      reductionList.push(dpb9919Item)
    });
    this.selected = [];
    return reductionList;
  }

  async multiDeletePopup(): Promise<void> {

    // this.translate.get('cfm_deletePermanently_tsmpdpFile', { count: this.selected.length }).subscribe(i18n => {
    //   this.messageService.add({ key: 'deletePopup', sticky: true, severity: 'warn', summary: i18n });
    // });

    const code = ['cfm_deletePermanently_tsmpdpFile', 'system_alert'];
    const dict = await this.toolService.getDict(code, { count: this.selected.length });

    this.confirmationService.confirm({
      header: dict['system_alert'],
      message: dict['cfm_deletePermanently_tsmpdpFile'],
      accept: () => {
        this.deleteConfirm();
      }
    });

  }

  private deletePermanently(fileIdList: number[]): void {
    let ReqBody = {
      fileIdList: fileIdList
    } as DPB9920Req;
    this.serverService.deletePermanentlyTsmpdpFile(ReqBody).subscribe(async res => {

      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'key', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.delete']} ${dict['key']}`,
          detail: `${dict['message.delete']} ${dict['message.success']}!`
        });
        this.queryResourceRecyclingBucket();
      }

    });
  }

  transformFileIdList(rowData: Array<DPB9915Item>): Array<number> {
    let fileIdList = new Array<number>();
    rowData.map(item => {
      fileIdList.push(item.fileId)
    });
    this.selected = [];
    return fileIdList;
  }

  singleDeletePops(item: DPB9915Item) {
    this.selected = [];
    this.selected.push(item);
    this.multiDeletePopup();
  }

  singleReductionPopup(item: DPB9915Item) {
    this.selected = [];
    this.selected.push(item);
    this.multiReductionPopup();
  }

  downloadFile(filePath: string, fileName: string) {

    let ReqBody = {
      filePath: filePath
    } as DPB0078Req;

    this.file.downloadFile(ReqBody).subscribe( async res => {
      const isJsonBlob = (data) => data instanceof Blob && data.type === "application/json";


      if (!isJsonBlob(res)) {
        const reader = new FileReader();
        reader.onloadend = function () {
          // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
          //     window.navigator.msSaveBlob(res, fileName)
          // }
          // else {
          const file = new File([res], fileName);
          const url = window.URL.createObjectURL(file);
          const a = document.createElement('a');
          document.body.appendChild(a);
          a.setAttribute('style', 'display: none');
          a.href = url;
          a.download = fileName;
          a.click();
          window.URL.revokeObjectURL(url);
          a.remove();
          // }
        }
        reader.readAsText(res);
      }
      else {
        const responseData = isJsonBlob(res) ? (await res.text()) : res || {};
        const responseJson = (typeof responseData === "string") ? JSON.parse(responseData) : responseData;
        this.alert.ok(responseJson.ResHeader.rtnMsg, '', AlertType.warning, responseJson.ResHeader.txDate + '<br>' + responseJson.ResHeader.txID); // 判斷不為1100 , show rtnMsg
      }
    });
  }


  async moveItemToBucket(item: DPB9915Item) {
    this.selectedItemMoveToBucket = item;
    // this.translate.get('cfm_del', {}).subscribe(i18n => {
    //   this.messageService.add({ key: 'moveItemToBucket', sticky: true, severity: 'warn', summary: i18n });
    // });

    const code = ['cfm_del', 'system_alert'];
    const dict = await this.toolService.getDict(code);

    this.confirmationService.confirm({
      header: dict['system_alert'],
      message: dict['cfm_del'],
      accept: () => {
        this.moveItemToBucketConfirm();
      }
    });

  }

  moveItemToBucketConfirm() {
    let fileItem: DPB9919Item = {} as DPB9919Item;
    let fileList: DPB9919Item[] = [] as DPB9919Item[];
    fileItem.fileId = this.selectedItemMoveToBucket.fileId;
    fileItem.oriIsTmpFile = 'N';
    fileList.push(fileItem);
    let ReqBody = {
      fileList: fileList,
      newIsTmpfile: 'Y'
    } as DPB9919Req;
    this.serverService.reductionTsmpdpFile(ReqBody).subscribe(res => {
      this.queryTsmpdpFileList();
    });
    this.messageService.clear();
  }

  async fileChange(files: FileList) {
    const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
    const dict = await this.tool.getDict(code);
    if (files.length != 0) {
      this.message.add({ severity: 'success', summary: dict['uploading'], detail: `${dict['waiting']}!` });
      let fileReader = new FileReader();
      fileReader.onloadend = () => {
        this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
        this._fileData = files.item(0);
        this._fileName = files.item(0)!.name;
      }
      fileReader.readAsDataURL(files.item(0)!);
    }
    else {
      this._fileName = null;
      this._fileData = null;
    }
  }

  openFileBrowser() {
    $('#fileName').click();
  }

  private createAPI(code, dict, tmpfileName: string | null) {
    let fileClassificationValue = this.refFileCateCode_add.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];
    let reqBody = {
      refFileCateCode: this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex,
      refId: this.refId_add.value,
      isTmpfile: this.isTmpfile_add.value ? 'Y' : null,
      fileName: this.fileName_add.value,
      tmpfileName: tmpfileName ? tmpfileName : null
    } as DPB9917Req;
    this.serverService.addTsmpdpFile(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['key']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
        this.queryTsmpdpFileList();
        this.changePage('query');
      }
    });
  }

  public clearFile() {
    this._fileName = null;
    this._fileData = null;
    $('#fileName').val('');
  }

  public removeFile() {
    // this._fileData = null;
    this.isDeleteFile = true;
    this._fileName = null;
    this.isDisplayPreviewBlock = false;
  }

  public recoveryFile() {
    // this._fileData = this.formUpdate.get('fileName').value;
    this._fileName = this.formUpdate.get('fileName')!.value;
    this.isDisplayPreviewBlock = true;
  }

  public openFileBrowser_update() {
    this.isDisplayPreviewBlock = false;
    setTimeout(() => {
      $('#fileName_update').click();
    }, 500);
  }

  async fileChange_update(files: FileList) {
    const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
    const dict = await this.tool.getDict(code);
    if (files.length != 0) {
      this.message.add({ severity: 'success', summary: dict['uploading'], detail: `${dict['waiting']}!` });
      let fileReader = new FileReader();
      fileReader.onloadend = () => {
        this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
        this._fileData = files.item(0);
        this._fileName = files.item(0)!.name;
      }
      fileReader.readAsDataURL(files.item(0)!);
    }
    else {
      // this._fileData = null;
      // this._fileName = null;
      // this.isDisplayPreviewBlock=true;
    }
  }

  private udpateAPI(code, dict, tmpfileName: string | null) {
    let fileClassificationValue = this.formUpdate.get('refFileCateCode')!.value;
    let fileClassificationIndex = this.fileClassificationsDict[fileClassificationValue];

    let reqBodyU = {
      fileId: this.formUpdate.get("fileId")!.value,
      refFileCateCode: this.tool.Base64Encoder(this.tool.BcryptEncoder(fileClassificationValue)) + ',' + fileClassificationIndex,
      refId: this.formUpdate.get("refId")!.value,
      fileName: this.formUpdate.get("fileName")!.value,
      isBlob: this._fileName ? 'Y' : this.isDeleteFile ? 'N' : 'Y',
      tmpFileName: tmpfileName ? tmpfileName : null,
      version: this.formUpdate.get("version")!.value
    } as DPB9918Req;
    this.serverService.updateTsmpdpFile(reqBodyU).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} ${dict['key']}`, detail: `${dict['message.update']} ${dict['message.success']}!`
        });
        this.queryTsmpdpFileList();
        this.changePage('query');
      }
    });
  }

  originStringTable(item: any) {
    return !item.ori ? item.val : item.t ? item.val : item.ori;
  }

  switchOri(item: any) {
    item.t = !item.t;
  }

  headerReturn() {
    this.changePage('query');
  }

}
