import { DialogService } from 'primeng/dynamicdialog';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit, Input, ViewChild, Output, EventEmitter } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as dayjs from 'dayjs';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { DPB0093ApiItem } from 'src/app/models/api/OpenApiService/dpb0093.interface';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { ApiListComponent } from '../api-list/api-list.component';
import { DPB0065Req, DPB0065OpenApiKey } from 'src/app/models/api/RequisitionService/dpb0065.interface';
import { RequisitionService } from 'src/app/shared/services/api-requisition.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0068Resp } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import { DPB0066Req, DPB0066OpenApiKey } from 'src/app/models/api/RequisitionService/dpb0066.interface';
import { of } from 'rxjs';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';

@Component({
  selector: 'app-open-api-key-form',
  templateUrl: './open-api-key-form.component.html',
  styleUrls: ['./open-api-key-form.component.css']
})
export class OpenApiKeyFormComponent extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;
  @Input() data!: FormParams;
  @Output() refresh: EventEmitter<boolean> = new EventEmitter;
  @Output() close!: Function;

  form!: FormGroup;
  formOperate = FormOperate;
  cols: { field: string; header: string; }[] = [];
  btnName: string = '';
  dialogTitle: string = '';
  apiList: Array<DPB0093ApiItem> = new Array<DPB0093ApiItem>();
  selectedApis: Array<DPB0093ApiItem> = new Array<DPB0093ApiItem>();
  rowcount: number = 0;
  minDateValue: Date = new Date();
  canSave: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tool: ToolService,
    private file: FileService,
    private ngxService: NgxUiLoaderService,
    private requisition: RequisitionService,
    private roleService: RoleService,
    private dialogService: DialogService
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.form = this.fb.group(this.resetFormGroup(this.data.operate)!);
    const code = ['button.save', 'api_desc', 'dept', 'button.update', 'button.resend', 'api_name', 'theme_name', 'api_doc'];
    const dict = await this.tool.getDict(code);
    this.cols = [
      { field: 'apiName', header: dict['api_name'] },
      { field: 'apiDesc', header: dict['api_desc'] },
      { field: 'orgName', header: dict['dept'] },
      // { field: 'themeDatas', header: dict['theme_name'] },
      // { field: 'fileName', header: dict['api_doc'] }
    ];
    this.roleService.queryRTMapByUk({ txIdList: ['DPB0065'] } as DPB0115Req).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.canSave = res.RespBody.dataList.find(item => item.txId === 'DPB0065') ? res.RespBody.dataList.find(item => item.txId === 'DPB0065')!.available : false;
      }
    });
    switch (this.data.operate) {
      case FormOperate.create:
        this.btnName = dict['button.save'];
        if (this.data.data.dataList.length != 0) {
          this.apiList = this.data.data.dataList;
          this.rowcount = this.apiList.length;
        }
        if (this.data.data.reqSubtype == 'OPEN_API_KEY_REVOKE') {
          this.openApiKeyAlias!.disable();
          this.expiredAt!.disable();
          this.timesThreshold!.disable();
        }
        this.requisition.createReqOpenApiKey_beforer().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
          }
        });
        break;
      case FormOperate.update:
      case FormOperate.resend:
        this.btnName = this.data.operate == FormOperate.update ? dict['button.update'] : dict['button.resend'];
        this.data.data.openApiKey.apiDatas.map(api => {
          let _fileName = Object.keys(api.docFileInfo)[0];
          let _filePath = api.docFileInfo[_fileName];
          this.apiList.push({ apiKey: api.apiKey, moduleName: api.moduleName, apiName: api.apiName, themeDatas: api.themeList, orgId: api.orgId, orgName: api.orgName, apiDesc: api.apiDesc, apiExtId: api.apiExtId, apiUid: api.apiUid, fileName: _fileName, filePath: _filePath });
        });
        this.rowcount = this.apiList.length;

        this.requisition.resendReq_before().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
          }
        });
        break;
    }
  }

  checkFromOperation() {
    return this.data.operate == FormOperate.create;
  }

  async searchAPI() {
    const code = ['api_list'];
    const dict = await this.tool.getDict(code);
    this.dialogTitle = dict['api_list'];
    // let data: FormParams = {
    //     operate: FormOperate.create,
    //     data: { keyword: this.keyword!.value },
    //     afterCloseCallback: (chooseApis) => {
    //         let set = new Set();
    //         this.apiList.map(item => {
    //             set.add(item.apiUid);
    //         });
    //         chooseApis.map(item => {
    //             if (!set.has(item.apiUid)) {
    //                 this.apiList.push(item);
    //             }
    //         });
    //         this.rowcount = this.apiList.length;
    //     }
    // };
    // this._dialog.open(ApiListComponent, data);

    const ref = this.dialogService.open(ApiListComponent, {
      data: {
        operate: FormOperate.create,
        data: { keyword: this.keyword!.value },
      },
      width: '80vw',
      height: '100vh',
      header: dict['api_list'],
    })

    ref.onClose.subscribe(chooseApis => {
      if (chooseApis) {
        let set = new Set();
        this.apiList.map(item => {
          set.add(item.apiUid);
        });
        chooseApis.map(item => {
          if (!set.has(item.apiUid)) {
            this.apiList.push(item);
          }
        });
        this.rowcount = this.apiList.length;
      }
    });


  }

  checkLength(obj: object, index: number): boolean {
    if (Object.keys(obj).length > 1 && (index + 1) != Object.keys(obj).length)
      return true;
    else
      return false;
  }

  delete() {
    this.selectedApis.map(item => {
      this.apiList = this.apiList.filter(list => list.apiUid != item.apiUid);
    });
    this.selectedApis = [];
    this.rowcount = this.apiList.length;
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

  submitForm() {
    switch (this.data.operate) {
      case FormOperate.create:
        this.createRequisition();
        break;
      case FormOperate.update:
      case FormOperate.resend:
        this.updateOrResendRequisition();
        break;
    }
  }

  createRequisition() {
    let ReqBody = {
      reqType: 'OPEN_API_KEY',
      reqSubtype: this.data.data.reqSubtype,
      reqDesc: this.reqDesc!.value,
      effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD'),
      openApiKeyD: {
        clientId: this.data.data.clientId,
        openApiKeyAlias: this.openApiKeyAlias!.value,
        timesThreshold: this.timesThreshold!.value,
        expiredAt: dayjs(this.expiredAt!.value).format('YYYY/MM/DD'),
        apiUids: this.convertApiUids()
      } as DPB0065OpenApiKey
    } as DPB0065Req;
    if (this.data.data.reqSubtype != 'OPEN_API_KEY_APPLICA') {
      ReqBody.openApiKeyD!.openApiKeyId = this.data.data.openApiKeyId;
      ReqBody.openApiKeyD!.openApiKey = this.data.data.openApiKey;
      ReqBody.openApiKeyD!.secretKey = this.data.data.secretKey;
    }
    // console.log('create req :', ReqBody)
    this.ngxService.start();
    this.requisition.createReq(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.ngxService.stop();
        this.clearData();
        this.refresh.emit(true);
      }
    });
  }

  updateOrResendRequisition() {
    let ReqBody = {
      reqOrdermId: this.data.data.reqOrdermId,
      lv: this.data.data.lv,
      act: this.data.operate == FormOperate.update ? 'U' : 'R',
      reqDesc: this.reqDesc!.value,
      effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD'),
      openApiKeyD: {
        clientId: this.data.data.openApiKey.clientId,
        openApiKeyAlias: this.openApiKeyAlias!.value,
        timesThreshold: this.timesThreshold!.value,
        expiredAt: dayjs(this.expiredAt!.value).format('YYYY/MM/DD'),
        apiUids: this.convertApiUids()
      } as DPB0066OpenApiKey
    } as DPB0066Req;
    if (this.data.data.reqSubtype != 'OPEN_API_KEY_APPLICA') {
      ReqBody.openApiKeyD!.openApiKeyId = this.data.data.openApiKey.openApiKeyId;
      ReqBody.openApiKeyD!.openApiKey = this.data.data.openApiKey.openApiKey;
      ReqBody.openApiKeyD!.secretKey = this.data.data.openApiKey.secretKey;
    }
    // console.log('dpb0066 reqbody :', ReqBody)
    this.requisition.resendReq(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        if (this.close) this.close(of(res.RespBody));
      }
    });
  }

  convertApiUids(): Array<string> {
    let _apiUids: any[] = [];
    this.apiList.map(item => {
      _apiUids.push(item.apiUid);
    });
    return _apiUids;
  }

  private resetFormGroup(formOperate?: FormOperate) {
    //初始化
    if (!formOperate) return {
      keyword: '',
      openApiKeyAlias: '',
      effectiveDate: '',
      timesThreshold: '',
      reqDesc: '',
      expiredAt: '',
    };
    switch (formOperate) {
      case FormOperate.create:
        return {
          keyword: new FormControl(''),
          effectiveDate: new FormControl(dayjs(new Date()).format('YYYY/MM/DD'), ValidatorFns.requiredValidator()),
          openApiKeyAlias: new FormControl(this.data.data.openApiKeyAlias, ValidatorFns.requiredValidator()),
          expiredAt: new FormControl(this.data.data.expiredAt != '' ? dayjs(this.data.data.expiredAt).format('YYYY/MM/DD') : '', ValidatorFns.requiredValidator()),
          timesThreshold: new FormControl(this.data.data.timesThreshold, ValidatorFns.requiredValidator()),
          reqDesc: new FormControl('', ValidatorFns.requiredValidator())
        }
      case FormOperate.update:
      case FormOperate.resend:
        let detailData = this.data.data as DPB0068Resp;
        return {
          keyword: new FormControl(''),
          effectiveDate: new FormControl(detailData.effectiveDate, ValidatorFns.requiredValidator()),
          openApiKeyAlias: new FormControl(detailData.openApiKey!.openApiKeyAlias, ValidatorFns.requiredValidator()),
          expiredAt: new FormControl(detailData.openApiKey!.expiredAt, ValidatorFns.requiredValidator()),
          timesThreshold: new FormControl(detailData.openApiKey!.timesThreshold, ValidatorFns.requiredValidator()),
          reqDesc: new FormControl(detailData.reqDesc, ValidatorFns.requiredValidator())
        }
      default:
        return {
          keyword: '',
          openApiKeyAlias: '',
          effectiveDate: '',
          timesThreshold: '',
          reqDesc: '',
          expiredAt: '',
        };
    }
  }

  clearData() {
    this.form.reset('');
    this.apiList = [];
    this.rowcount = this.apiList.length;
    this.selectedApis = [];
  }

  public get keyword() { return this.form.get('keyword'); };
  public get openApiKeyAlias() { return this.form.get('openApiKeyAlias'); };
  public get effectiveDate() { return this.form.get('effectiveDate'); };
  public get timesThreshold() { return this.form.get('timesThreshold'); };
  public get reqDesc() { return this.form.get('reqDesc'); };
  public get expiredAt() { return this.form.get('expiredAt'); };

}
