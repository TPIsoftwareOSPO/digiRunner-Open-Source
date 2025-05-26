import { DynamicDialogRef, DynamicDialogConfig, DialogService } from 'primeng/dynamicdialog';
import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormBuilder, NgForm, FormControl, FormGroup } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import * as dayjs from 'dayjs';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0075Req, DPB0075RespItem } from 'src/app/models/api/LovService/dpb0075.interface';
import { LovService } from 'src/app/shared/services/api-lov.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { ApiLovComponent } from '../../np0301/api-lov/api-lov.component';
import { DPB0065Req, DPB0065ApiApplication } from 'src/app/models/api/RequisitionService/dpb0065.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { MessageService } from 'primeng/api';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { DPB0068Resp } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { RequisitionService } from 'src/app/shared/services/api-requisition.service';
import { DPB0066Req, DPB0066ApiApplication } from 'src/app/models/api/RequisitionService/dpb0066.interface';
import { of } from 'rxjs';
import { TOrgService } from 'src/app/shared/services/org.service';
import { BaseComponent } from 'src/app/layout/base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
  selector: 'app-client-authorize-api',
  templateUrl: './client-authorize-api.component.html',
  styleUrls: ['./client-authorize-api.component.css']
})
export class ClientAuthorizeApiComponent extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;
  @Input() data!: FormParams;
  @Input() close!: Function;

  form!: FormGroup;
  minDateValue: Date = new Date();
  createDateTime: string = '';
  btnName: string = '';
  formOperate = FormOperate;
  dialogTitle: string = '';
  selectedApis: Array<DPB0075RespItem> = new Array<DPB0075RespItem>();
  cols: { field: string; header: string; }[] = [];
  authApiList: Array<DPB0075RespItem> = new Array<DPB0075RespItem>();
  rowcount: number = 0;
  orgName: string = '';
  fileName:string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tool: ToolService,
    private lov: LovService,
    private file: FileService,
    private message: MessageService,
    private ngxService: NgxUiLoaderService,
    private requisition: RequisitionService,
    private orgService: TOrgService,
    private ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private dialogService: DialogService,
  ) {
    super(route, tr);
  }

  async ngOnInit() {

    this.form = this.fb.group(this.resetFormGroup(this.config.data.operate)!);
    const code = ['button.save', 'api_desc', 'dept', 'button.update', 'button.resend', 'api_name', 'theme_name', 'api_doc', 'api_audience'];
    const dict = await this.tool.getDict(code);
    this.cols = [
      { field: 'apiName', header: dict['api_name'] },
      { field: 'apiDesc', header: dict['api_desc'] },
      { field: 'publicFlagName', header: dict['api_audience'] },
      { field: 'orgName', header: dict['dept'] },
      { field: 'themeDatas', header: dict['theme_name'] },
      { field: 'fileName', header: dict['api_doc'] }
    ];
    switch (this.config.data.operate) {
      case FormOperate.create:
        this.btnName = dict['button.save'];
        this.createDateTime = dayjs(new Date()).format('YYYY/MM/DD');
        this.orgService.queryTOrgList({ orgID: this.tool.getOrgId() }).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.orgName = res.RespBody.orgList[0].orgName;
          }
        });
        break;
      case FormOperate.update:
      case FormOperate.resend:
        this.btnName = this.config.data.operate == FormOperate.update ? dict['button.update'] : dict['button.resend'];
        this.config.data.data.apiUserApply.apiList.map(api => {
          let _fileName = Object.keys(api.docFileInfo)[0];
          let _filePath = api.docFileInfo[_fileName];
          this.authApiList.push({ apiUid: api.apiUid, apiName: api.apiName, moduleName: api.moduleName, themeDatas: api.themeList, orgName: api.orgName, fileName: _fileName, filePath: _filePath, apiDesc: api.apiDesc, publicFlag: api.publicFlag, publicFlagName: api.publicFlagName });
        });
        this.rowcount = this.authApiList.length;
        break;
    }

    this.requisition.createReq_beforer().subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.addFormValidator(this.form, res.RespBody.constraints);
      }
    });
  }

  async fileChange(file: FileList) {
    const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
    const dict = await this.tool.getDict(code);
    if (file.length != 0) {
      let fileReader = new FileReader();
      fileReader.onloadend = () => {
        this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
        this.fileData!.setValue(file.item(0));
      }
      fileReader.readAsBinaryString(file.item(0)!);
      this.fileName = file[0].name;
    }
    else {
      this.fileData!.setValue(null);
    }
  }

  searchAPI() {
    let ReqBody = {
      keyword: this.keyword!.value,
      dpStatus: '1'
    } as DPB0075Req;
    this.lov.queryApiLov(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['on_shelves_api_search'];
        const dict = await this.tool.getDict(code);
        // let data: FormParams = {
        //   displayInDialog: true,
        //   data: { dpStatus: '1', selectedApis: this.authApiList, apiList: res.RespBody.dataList, selectionMode: 'multiple', keyword: this.keyword!.value },
        //   afterCloseCallback: (_chooseAPI) => {
        //     this.keyword!.setValue('');
        //     // if (this.data.operate == FormOperate.update && window.location.hash == '#/np04/np0401') {
        //     //     this.data.data.apiOnOff.apiOnOffList = _chooseAPI;
        //     // }
        //     this.authApiList = _chooseAPI;
        //     this.rowcount = this.authApiList.length;
        //   }
        // }
        // this._dialog.open(ApiLovComponent, data);

        const ref = this.dialogService.open(ApiLovComponent, {
          data: {
            data:{
            dpStatus: '1',
            selectedApis: this.authApiList,
            apiList: res.RespBody.dataList,
            selectionMode: 'multiple',
            keyword: this.keyword!.value
            }
          }
        })

        ref.onClose.subscribe(_chooseAPI => {
          if(_chooseAPI) {
            this.keyword!.setValue('');
            this.authApiList = _chooseAPI;
            this.rowcount = this.authApiList.length;
          }
        })

      }
    });
  }

  checkLength(obj: object, index: number): boolean {
    if (Object.keys(obj).length > 1 && (index + 1) != Object.keys(obj).length)
      return true;
    else
      return false;
  }

  fileNameConvert(fileName: string): string {
    if (fileName) {
      return decodeURIComponent(fileName);
    }
    return '';
  }

  downloadFile(filePath: string, fileName: string) {
    let ReqBody = {
      filePath: filePath
    } as DPB0078Req;
    this.file.downloadFile(ReqBody).subscribe(res => {
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
    });
  }

  delete() {
    this.selectedApis.map(item => {
      this.authApiList = this.authApiList.filter(list => list.apiUid != item.apiUid);
    });
    this.selectedApis = [];
    this.rowcount = this.authApiList.length;
  }

  submitForm() {
    switch (this.config.data.operate) {
      case FormOperate.create:
        if (this.fileData!.value) {
          window.setTimeout(() => {
            this.file.uploadFile2(this.fileData!.value).subscribe(res => {
              if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.tmpFileName!.setValue(res.RespBody.tempFileName);
                this.createRequisition();
              }
            });
          });
        }
        else {
          this.tmpFileName!.setValue(null);
          this.createRequisition();
        }
        break;
      case FormOperate.update:
      case FormOperate.resend:
        if (this.fileData!.value) {
          window.setTimeout(() => {
            this.file.uploadFile2(this.fileData!.value).subscribe(res => {
              if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.tmpFileName!.setValue(res.RespBody.tempFileName);
                this.updateOrResendRequisition();
              }
            });
          });
        }
        else {
          this.tmpFileName!.setValue(null);
          this.updateOrResendRequisition();
        }
        break;
    }
  }

  createRequisition() {
    let ReqBody = {
      reqType: 'API_APPLICATION',
      effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD'),
      reqDesc: this.reqDesc!.value
    } as DPB0065Req;
    let _apiApplication = {
      clientId: this.config.data.data.clientID,
      apiUids: this.convertApiUids(),
    } as DPB0065ApiApplication;
    if (this.tmpFileName!.value) {
      _apiApplication.tmpFileName = this.tmpFileName!.value
    }
    ReqBody.apiApplicationD = _apiApplication;
    // console.log('ReqBody :', ReqBody)
    this.ngxService.start();
    this.requisition.createReq(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.ngxService.stop();
        const code = ['message.save', 'message.requisition.client_auth_api', 'message.success'];
        const dict = await this.tool.getDict(code);
        this.message.add({ severity: 'success', summary: `${dict['message.save']} ${dict['message.requisition.client_auth_api']}`, detail: `${dict['message.save']} ${dict['message.success']}!` });
        this.form.reset();
        this.form = this.fb.group(this.resetFormGroup(this.config.data.operate));

        const fileInput = document.getElementById("file") as HTMLInputElement;;
        if (fileInput) {
          fileInput.value = ''; // 清空檔案選擇
        }
        this.authApiList = [];
        this.rowcount = this.authApiList.length;
      }
    });
  }

  updateOrResendRequisition() {
    let updateReqBody = {
      reqOrdermId: this.config.data.data.reqOrdermId,
      lv: this.config.data.data.lv,
      act: this.config.data.operate == FormOperate.update ? 'U' : 'R',
      reqDesc: this.reqDesc!.value,
      effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD')
    } as DPB0066Req;
    let _apiApplication = {
      clientId: this.config.data.data.apiUserApply.clientId,
      apiUids: this.convertApiUids(),
      newFileName: this.tmpFileName!.value ? this.tmpFileName!.value : this.oriFileName!.value,
      oriFileName: this.oriFileName!.value
    } as DPB0066ApiApplication;
    updateReqBody.apiApplicationD = _apiApplication;
    // console.log('resend :', updateReqBody)
    this.requisition.resendReq(updateReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        // if (this.close) this.close(of(res.RespBody));
        this.ref.close(res.RespBody);
      }
    });
  }

  convertApiUids(): Array<string> {
    let _apiUids: any[] = [];
    this.authApiList.map(item => {
      _apiUids.push(item.apiUid);
    });
    return _apiUids;
  }

  private resetFormGroup(formOperate?: FormOperate) {
    //初始化
    if (!formOperate) return {
      reqDesc: '',
      effectiveDate: '',
      fileData: '',
      oriFileName: '', // original file name
      tmpFileName: '' // temp file name
    };
    switch (formOperate) {
      case FormOperate.create:
        return {
          reqDesc: new FormControl('', ValidatorFns.requiredValidator()),
          effectiveDate: new FormControl(dayjs(new Date()).format('YYYY/MM/DD'), ValidatorFns.requiredValidator()),
          fileData: new FormControl(null),
          oriFileName: new FormControl(''),
          tmpFileName: new FormControl(''),
          keyword: new FormControl('')
        }
      case FormOperate.update:
      case FormOperate.resend:
        let detailData = this.config.data.data as DPB0068Resp;
        return {
          reqDesc: new FormControl(detailData.reqDesc, ValidatorFns.requiredValidator()),
          effectiveDate: new FormControl(detailData.effectiveDate, ValidatorFns.requiredValidator()),
          fileData: new FormControl(null),
          oriFileName: new FormControl(detailData.apiUserApply!.fileName),
          tmpFileName: new FormControl(''),
          keyword: new FormControl('')
        };
      default:
        return {
          reqDesc: '',
          effectiveDate: '',
          fileData: '',
          oriFileName: '', // original file name
          tmpFileName: '' // temp file name
        }
    }
  }

  openFileBrowser() {
    $('#file').click();
}

  public get effectiveDate() { return this.form.get('effectiveDate'); };
  public get reqDesc() { return this.form.get('reqDesc'); };
  public get keyword() { return this.form.get('keyword'); };
  public get fileData() { return this.form.get('fileData'); };
  public get oriFileName() { return this.form.get('oriFileName'); };
  public get tmpFileName() { return this.form.get('tmpFileName'); };

}
