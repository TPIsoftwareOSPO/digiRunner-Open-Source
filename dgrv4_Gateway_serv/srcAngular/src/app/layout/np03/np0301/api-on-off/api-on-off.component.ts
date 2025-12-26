import { DialogService } from 'primeng/dynamicdialog';
import { Component, OnInit, ViewChild, forwardRef, ViewContainerRef, ElementRef, ComponentFactoryResolver, Input } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, FormControl, NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { DPB0075RespItem, DPB0075Req } from 'src/app/models/api/LovService/dpb0075.interface';
import { DPB0065Req, DPB0065ApiBindingData, newApiOnOff, newApiOnOffAddNo, DPB0065ApiOnOff } from 'src/app/models/api/RequisitionService/dpb0065.interface';
import * as dayjs from 'dayjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import { LovService } from 'src/app/shared/services/api-lov.service';
import { MessageService } from 'primeng/api';
import { ApiLovComponent } from '../api-lov/api-lov.component';
import { ApiShelvesComponent } from '../api-shelves/api-shelves.component';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0066Req, DPB0066ApiOnOff, DPB0066ApiBindingData } from 'src/app/models/api/RequisitionService/dpb0066.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { TOrgService } from 'src/app/shared/services/org.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import { DPB0068Resp, DPB0068D2 } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { RequisitionService } from 'src/app/shared/services/api-requisition.service';

@Component({
  selector: 'app-api-on-off',
  templateUrl: './api-on-off.component.html',
  styleUrls: ['./api-on-off.component.css'],
  // providers: [{
  //     provide: NG_VALUE_ACCESSOR,
  //     useExisting: forwardRef(() => ApiOnOffComponent),
  //     multi: true
  // }, MessageService]
  providers: [MessageService]
})
export class ApiOnOffComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;
  @ViewChild('apiShelves', { read: ViewContainerRef }) apiShelvesRef!: ViewContainerRef;
  @ViewChild('content') content!: ElementRef;
  onTouched!: () => void;
  onChange: (value: any) => void = (_: any) => { };
  @Input() data!: FormParams;
  @Input() close!: Function;

  apiOnOffs: Array<newApiOnOffAddNo> = new Array<newApiOnOffAddNo>();
  value: Array<newApiOnOff> = new Array<newApiOnOff>();
  disabled: boolean = false;
  apiOnOffNo: number = 0;
  form: FormGroup;
  cols: { field: string; header: string; }[] = [];
  apiOffDataList: Array<DPB0075RespItem> = new Array();
  apiOffSelected: Array<DPB0075RespItem> = new Array();
  apiOnUpdateSelected: Array<DPB0075RespItem> = new Array();
  dialogTitle: string = '';
  rowcount: number = 0;
  userAlias: string = '';
  orgName: string = '';
  keyword: string = '';
  createDateTime: string = '';
  formOperate = FormOperate;
  subTypes: { label: string; value: string; }[] = [];
  publicFlags: { label: string; value: string; }[] = [];
  isInvalid: boolean = true;
  btnName: string = '';
  minDateValue: Date = new Date();
  oriApi: Array<DPB0068D2> = new Array<DPB0068D2>();

  constructor(
    private fb: FormBuilder,
    private tool: ToolService,
    private factoryResolver: ComponentFactoryResolver,
    private lov: LovService,
    private router: Router,
    private message: MessageService,
    private list: ListService,
    private orgService: TOrgService,
    private file: FileService,
    private ngxService: NgxUiLoaderService,
    private requisition: RequisitionService,
    private dialogService: DialogService,
  ) {


    this.form = this.fb.group({
      reqSubtype: new FormControl(''),
      reqDesc: new FormControl(''),
      effectiveDate: new FormControl(''),
      encPublicFlag: new FormControl('')
    });
  }

  async ngOnInit() {
    this.form = this.fb.group(this.resetFormGroup(this.data.operate)!);
    // 申請項目
    let typeReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_ON_OFF')) + ',' + 5,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(typeReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _subTypes: { label: string, value: string }[] = [];
        res.RespBody.subItems?.map(item => {
          _subTypes.push({ label: item.subitemName, value: item.subitemNo })
        });
        this.subTypes = _subTypes;
      }
    });
    // 開放狀態
    let flagReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_AUTHORITY')) + ',' + 7,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(flagReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _publicFlags: { label: string, value: string }[] = [];
        res.RespBody.subItems?.map(item => {
          if (item.subitemNo != '-1') {
            _publicFlags.push({ label: item.subitemName, value: item.subitemNo });
          }
        });
        this.publicFlags = _publicFlags;
      }
    });

    this.reqSubtype!.valueChanges.subscribe(type => {
      this.apiOnOffs = [];
      this.apiOffDataList = [];
      this.apiOffSelected = [];
      this.apiOnUpdateSelected = [];
    });
    const code = ['api_name', 'api_desc', 'public_flag', 'org_name', 'theme_name', 'api_doc', 'button.save', 'button.update', 'button.resend'];
    const dict = await this.tool.getDict(code);
    this.cols = [
      { field: 'apiName', header: dict['api_name'] },
      { field: 'apiDesc', header: dict['api_desc'] },
      { field: 'publicFlagName', header: dict['public_flag'] },
      { field: 'orgName', header: dict['org_name'] },
      { field: 'themeDatas', header: dict['theme_name'] },
      { field: 'fileName', header: dict['api_doc'] }
    ];
    switch (this.data.operate) {
      case FormOperate.create:
        this.btnName = dict['button.save'];
        this.createDateTime = dayjs(new Date()).format('YYYY/MM/DD');
        this.userAlias = this.tool.getUserAlias();
        this.orgService.queryTOrgList({ orgID: this.tool.getOrgId() }).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.orgName = res.RespBody.orgList[0].orgName;
          }
        });
        break;
      case FormOperate.update:
      case FormOperate.resend:
        this.btnName = this.data.operate == FormOperate.update ? dict['button.update'] : dict['button.resend'];
        if (this.reqSubtype!.value == 'API_ON' || this.reqSubtype!.value == 'API_ON_UPDATE') {
          this.data.data.apiOnOff.apiOnOffList.map(item => {
            let themeId: any[] = [];
            let themeName: any[] = [];
            Object.keys(item.themeList).map(key => {
              themeId.push(parseInt(key));
              themeName.push(item.themeList[key]);
            });
            window.setTimeout(() => {
              this.addApiOnOffRef({ apiUid: item.apiUid, apiName: item.apiName, refThemeId: themeId, refThemeName: themeName, fileName: Object.keys(item.docFileInfo)[0], tempFileName: null });
              this.isInvalid = false;
            });
          });
        }
        else {
          this.oriApi = this.data.data.apiOnOff.apiOnOffList;
          this.data.data.apiOnOff.apiOnOffList.map(item => {
            let _fileName = Object.keys(item.docFileInfo)[0];
            let _filePath = item.docFileInfo[_fileName];
            this.apiOffDataList.push({ apiUid: item.apiUid, apiKey: item.apiKey, apiName: item.apiName, apiDesc: item.apiDesc, orgName: item.orgName, themeDatas: item.themeList, fileName: _fileName, filePath: _filePath, moduleName: item.moduleName });
          });
        }
        break;
    }
  }

  // writeValue(apiOnOffs: Array<newApiOnOff>): void {
  //     this.value = apiOnOffs;
  //     if (this.value)
  //         this.value.forEach(val => this.addApiOnOffRef(val));
  //     else if (apiOnOffs != null)
  //         this.addApiOnOffRef();
  // }

  // registerOnChange(fn: (value: any) => void): void {
  //     this.onChange = fn
  // }

  // registerOnTouched(fn: () => void): void {
  //     this.onTouched = fn;
  // }

  // setDisabledState?(isDisabled: boolean): void {
  //     this.disabled = isDisabled;
  // }

  addApiOnOffRef(newApiOnOff?: newApiOnOff) {
    if (this.data.operate != FormOperate.create && window.location.hash == '#/np04/np0401') {
      this.apiOnUpdateSelected = this.data.data.apiOnOff.apiOnOffList;
    }
    console.log(newApiOnOff)
    // let componentFactory = this.factoryResolver.resolveComponentFactory(ApiShelvesComponent);
    let componentRef = this.apiShelvesRef.createComponent(ApiShelvesComponent);
    this.isInvalid = this.data.operate == FormOperate.create ? true : false;
    if (newApiOnOff) {
      this.apiOnOffs.push({ no: this.apiOnOffNo, apiUid: newApiOnOff.apiUid, apiName: newApiOnOff.apiName, refThemeId: newApiOnOff.refThemeId, refThemeName: newApiOnOff.refThemeName, fileName: newApiOnOff.fileName, tempFileName: newApiOnOff.tempFileName });
    }
    else {
      this.apiOnOffs.push({ no: this.apiOnOffNo, apiUid: '', apiName: '', refThemeId: [], refThemeName: [], fileName: null, tempFileName: null });
    }
    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.apiOnOffNo;
    componentRef.instance.data = newApiOnOff;
    componentRef.instance.subType = this.reqSubtype!.value;
    this.apiOnOffNo++;
    componentRef.instance.change.subscribe((res: newApiOnOffAddNo) => {
      // console.log('apiShelvesRef change apiOnOffs :', this.apiOnOffs);
      // console.log(res.no);
      let idx = this.apiOnOffs.findIndex(x => x.no === res.no);
      if (!idx && this.apiOnOffs.length == 0) {
        this.apiOnOffs.push({ no: this.apiOnOffNo, apiUid: '', apiName: '', refThemeId: [], refThemeName: [], fileName: null, tempFileName: null });
      }
      else {
        let idx = this.apiOnOffs.findIndex(api => api.no === res.no);
        this.apiOnOffs[idx].no = res.no;
        this.apiOnOffs[idx].apiUid = res.apiUid;
        this.apiOnOffs[idx].apiName = res.apiName;
        this.apiOnOffs[idx].refThemeId = res.refThemeId;
        this.apiOnOffs[idx].refThemeName = res.refThemeName;
        this.apiOnOffs[idx].fileName = res.fileName;
        this.apiOnOffs[idx].tempFileName = res.tempFileName;
      }
      let _newApiOnOff: Array<newApiOnOff> = this.apiOnOffs.map(x => {
        return { apiUid: x.apiUid, apiName: x.apiName, refThemeId: x.refThemeId, refThemeName: x.refThemeName, fileName: x.fileName, tempFileName: x.tempFileName }
      });
      this.onChange(_newApiOnOff);
    });
    componentRef.instance.remove.subscribe(no => {
      let idx = this.apiOnOffs.findIndex(api => api.no === no);
      let deleteApi = this.apiOnOffs.find(api => api.no == no);
      this.apiOnOffs.splice(idx, 1);
      this.onChange(this.apiOnOffs);
      if (this.data.operate != FormOperate.create && window.location.hash == '#/np04/np0401') {
        let _idx = this.apiOnUpdateSelected.findIndex(api => api.apiUid == deleteApi!.apiUid);
        this.apiOnUpdateSelected.splice(_idx, 1);
      }
    });
    componentRef.instance.isInvalid.subscribe(flag => {
      this.isInvalid = flag;
    });
  }

  delete() {
    this.apiOffSelected.map(item => {
      this.apiOffDataList = this.apiOffDataList.filter(list => list.apiUid != item.apiUid);
    });
    this.apiOffSelected = [];
  }

  async searchShelvesAPI(action: string) {
    let ReqBody = {
      keyword: this.keyword,
      dpStatus: '1'
    } as DPB0075Req;
    this.lov.queryApiLov(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['on_shelves_api_search'];
        const dict = await this.tool.getDict(code);
        this.dialogTitle = dict['on_shelves_api_search'];
        switch (action) {
          case 'API_OFF':
            // let data: FormParams = {
            //   displayInDialog: true,
            //   data: { dpStatus: '1', selectedApis: this.apiOffDataList, apiList: res.RespBody.dataList, selectionMode: 'multiple', keyword: this.keyword },
            //   afterCloseCallback: (_chooseAPI) => {
            //     if (_chooseAPI) {
            //       this.keyword = '';
            //       if (this.data.operate != FormOperate.create && window.location.hash == '#/np04/np0401') {
            //         this.data.data.apiOnOff.apiOnOffList = _chooseAPI;
            //       }
            //       this.apiOffDataList = _chooseAPI;
            //       this.rowcount = this.apiOffDataList.length;
            //     }
            //   }
            // }
            // this._dialog.open(ApiLovComponent, data);

            const ref = this.dialogService.open(ApiLovComponent, {
              data: {
                data:{ dpStatus: '1', selectedApis: this.apiOffDataList, apiList: res.RespBody.dataList, selectionMode: 'multiple', keyword: this.keyword },
                width: '80vw',
                height: '100vh',
              },
              header: dict['on_shelves_api_search'],
            })

            ref.onClose.subscribe(_chooseAPI => {
              if (_chooseAPI) {
                this.keyword = '';
                if (this.data.operate != FormOperate.create && window.location.hash == '#/np04/np0401') {
                      this.data.data.apiOnOff.apiOnOffList = _chooseAPI;
                }
                this.apiOffDataList = _chooseAPI;
                this.rowcount = this.apiOffDataList.length;
              }

            });
            break;
          case 'API_ON_UPDATE':
            // let updateData: FormParams = {
            //   displayInDialog: true,
            //   data: { dpStatus: '1', selectedApis: this.apiOnUpdateSelected, apiList: res.RespBody.dataList, selectionMode: 'multiple', keyword: this.keyword },
            //   afterCloseCallback: (_chooseAPI) => {
            //     if (_chooseAPI) {
            //       this.keyword = '';
            //       if (this.data.operate != FormOperate.create && window.location.hash == '#/np04/np0401') {
            //         this.data.data.apiOnOff.apiOnOffList = _chooseAPI;
            //       }
            //       this.apiOnUpdateSelected = _chooseAPI;
            //       let set = new Set();
            //       for (let api of _chooseAPI) {
            //         let _refThemeId: any[] = [];
            //         let _refThemeName: any[] = [];
            //         Object.keys(api.themeDatas).map(key => {
            //           _refThemeId.push(key);
            //           _refThemeName.push(api.themeDatas[key]);
            //         });
            //         if (!set.has(api.apiUid)) {
            //           this.addApiOnOffRef({ apiUid: api.apiUid, apiName: api.apiName, refThemeId: _refThemeId, refThemeName: _refThemeName, fileName: api.fileName, tempFileName: null });
            //         }
            //       }
            //       this.isInvalid = false;
            //     }
            //   }
            // }
            // this._dialog.open(ApiLovComponent, updateData);
            console.log('update')
            const ref2 = this.dialogService.open(ApiLovComponent, {
              data: {
                data: { dpStatus: '1', selectedApis: this.apiOnUpdateSelected, apiList: res.RespBody.dataList, selectionMode: 'multiple', keyword: this.keyword },
                width: '80vw',
                height: '100vh',

              },
              header: this.dialogTitle,
            })

            ref2.onClose.subscribe(_chooseAPI => {
              if (_chooseAPI) {
                this.keyword = '';
                if (this.data.operate != FormOperate.create && window.location.hash == '#/np04/np0401') {
                  this.data.data.apiOnOff.apiOnOffList = _chooseAPI;
                }
                this.apiOnUpdateSelected = _chooseAPI;
                let set = new Set();
                for (let api of _chooseAPI) {
                  let _refThemeId: any[] = [];
                  let _refThemeName: any[] = [];
                  Object.keys(api.themeDatas).map(key => {
                    _refThemeId.push(key);
                    _refThemeName.push(api.themeDatas[key]);
                  });
                  if (!set.has(api.apiUid)) {
                    console.log(api)
                    this.addApiOnOffRef({ apiUid: api.apiUid, apiName: api.apiName, refThemeId: _refThemeId, refThemeName: _refThemeName, fileName: api.fileName, tempFileName: null });
                  }
                }
                this.isInvalid = false;
              }

            });
            break;
        }
      }
    });
  }

  submitForm() {
    switch (this.data.operate) {
      case FormOperate.create:
        let ReqBody = {
          reqType: 'API_ON_OFF',
          reqSubtype: this.reqSubtype!.value,
          effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD'),
          reqDesc: this.reqDesc!.value,
          apiOnOffD: this.converApiOnOffData(this.reqSubtype!.value)
        } as DPB0065Req;
        // console.log('ReqBody :', ReqBody)
        this.ngxService.start();
        this.requisition.createReq(ReqBody).subscribe(async res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            const code = ['message.create', 'message.requisition.api_on_off', 'message.success'];
            const dict = await this.tool.getDict(code);
            this.message.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.requisition.api_on_off']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
            window.setTimeout(() => {
              this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
                this.router.navigate(['np03', 'np0301']);
              });
            }, 1000);
          }
        });
        break;
      case FormOperate.update:
      case FormOperate.resend:
        let resendReqBody = {
          reqOrdermId: this.data.data.reqOrdermId,
          lv: this.data.data.lv,
          act: this.data.operate == FormOperate.update ? 'U' : 'R',
          reqDesc: this.reqDesc!.value,
          effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD'),
          apiOnOffD: this.converApiOnOffDataForUpdate(this.reqSubtype!.value),
        } as DPB0066Req;
        // console.log('resend :', resendReqBody)
        let updateOrResendObservable = this.requisition.resendReq(resendReqBody);
        if (this.close) this.close(updateOrResendObservable);
        break;
    }
  }

  converApiOnOffData(apply_type: string): DPB0065ApiOnOff {
    let obj = {} as DPB0065ApiOnOff;
    let apiUidObj = new Object();
    let fileNameObj = new Object();
    switch (apply_type) {
      case 'API_ON':
      case 'API_ON_UPDATE':
        this.apiOnOffs.map(api => {
          let apiBinding = new Array<DPB0065ApiBindingData>();
          for (let themeId of api.refThemeId) {
            apiBinding.push({ apiUid: api.apiUid, refThemeId: themeId });
          }
          apiUidObj[api.apiUid] = apiBinding;
          if ((api.fileName != '' && api.fileName != null) && api.tempFileName == null) {
            fileNameObj[api.apiUid] = api.fileName;
          }
          else if (api.fileName == null && api.tempFileName == null) {
            return;
          }
          else {
            fileNameObj[api.apiUid] = api.tempFileName;
          }
        });
        break;
      case 'API_OFF':
        this.apiOffDataList.map(api => {
          let apiBinding = new Array<DPB0065ApiBindingData>();
          Object.keys(api.themeDatas).map(key => {
            apiBinding.push({ apiUid: api.apiUid, refThemeId: parseInt(key) });
          });
          apiUidObj[api.apiUid] = apiBinding;
          if (api.fileName) {
            fileNameObj[api.apiUid] = api.fileName;
          }
        });
        break;
    }
    obj.apiUidDatas = apiUidObj;
    obj.apiMapFileName = fileNameObj;
    obj.encPublicFlag = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encPublicFlag!.value)) + ',' + this.encPublicFlag!.value;
    return obj;
  }

  converApiOnOffDataForUpdate(apply_type: string): DPB0066ApiOnOff {
    console.log(apply_type)
    let obj = {} as DPB0066ApiOnOff;
    let apiUidObj = new Object();
    let oriFileNameObj = new Object();
    let newfileNameObj = new Object();
    switch (apply_type) {
      case 'API_ON':
      case 'API_ON_UPDATE':
        if (this.data.operate == FormOperate.create) {
          obj.oriApiMapFileName = {};
        } else {
          this.data.data.apiOnOff.apiOnOffList.map(oriApi => {
            if (Object.keys(oriApi.docFileInfo).length > 0) {
              oriFileNameObj[oriApi.apiUid] = Object.keys(oriApi.docFileInfo)[0]
            }
          });
          obj.oriApiMapFileName = oriFileNameObj;
        }
        this.apiOnOffs.map(api => {
          let apiBinding = new Array<DPB0066ApiBindingData>();
          for (let themeId of api.refThemeId) {
            apiBinding.push({ apiUid: api.apiUid, refThemeId: themeId });
          }
          apiUidObj[api.apiUid] = apiBinding;
          if ((api.fileName != '' && api.fileName != null) && api.tempFileName == null) {
            newfileNameObj[api.apiUid] = api.fileName;
          }
          else if (api.fileName == null && api.tempFileName == null) {
            return;
          }
          else {
            newfileNameObj[api.apiUid] = api.tempFileName;
          }
        });
        obj.newApiMapFileName = newfileNameObj;
        break;
      case 'API_OFF':
        // let _oriApiList = new Array<DPB0068D2>();
        this.apiOffDataList.map(item => {
          console.log(item)
          // if (this.oriApi.findIndex(api => api.apiUid == list.apiUid) > -1) {
          //     _oriApiList.push(this.oriApi.find(api => api.apiUid == list.apiUid));
          // }
          // else {
          //     if (list.fileName) {
          //         newfileNameObj[list.apiUid] = list.fileName;
          //     }
          // }

          if (item.fileName) {
            newfileNameObj[item.apiUid] = item.fileName;
          }
          else {
            newfileNameObj[item.apiUid] = null;
          }

          let apiBinding = new Array<DPB0066ApiBindingData>();
          Object.keys(item.themeDatas).map(key => {
            apiBinding.push({ apiUid: item.apiUid, refThemeId: parseInt(key) });
          });
          apiUidObj[item.apiUid] = apiBinding;
        });

        this.oriApi.map(oriApi => {
          if (Object.keys(oriApi.docFileInfo).length > 0) {
            oriFileNameObj[oriApi.apiUid] = Object.keys(oriApi.docFileInfo)[0]
          }
          else {
            oriFileNameObj[oriApi.apiUid] = null;
          }
        });
        obj.oriApiMapFileName = oriFileNameObj;
        obj.newApiMapFileName = newfileNameObj;

        break;
    }
    obj.apiUidDatas = apiUidObj;
    obj.encPublicFlag = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encPublicFlag!.value)) + ',' + this.encPublicFlag!.value;
    return obj;
  }

  checkLength(obj: object, index: number): boolean {
    if (Object.keys(obj).length > 1 && (index + 1) != Object.keys(obj).length)
      return true;
    else
      return false;
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

  fileNameConvert(fileName: string): string {
    if (fileName) {
      return decodeURIComponent(fileName);
    }
    return '';
  }

  private resetFormGroup(formOperate?: FormOperate) {
    //初始化
    if (!formOperate) return {
      reqSubtype: '',
      reqDesc: '',
      effectiveDate: '',
      encPublicFlag: ''
    };
    switch (formOperate) {
      case FormOperate.create:
        return {
          reqSubtype: new FormControl('API_ON', ValidatorFns.requiredValidator()),
          reqDesc: new FormControl('', ValidatorFns.requiredValidator()),
          effectiveDate: new FormControl(dayjs(new Date()).format('YYYY/MM/DD'), ValidatorFns.requiredValidator()),
          encPublicFlag: new FormControl('', ValidatorFns.requiredValidator())
        }
      case FormOperate.update:
      case FormOperate.resend:
        let detailData = this.data.data as DPB0068Resp;
        return {
          reqSubtype: new FormControl({ value: detailData.reqSubtype, disabled: true }, ValidatorFns.requiredValidator()),
          reqDesc: new FormControl(detailData.reqDesc, ValidatorFns.requiredValidator()),
          effectiveDate: new FormControl(detailData.effectiveDate, ValidatorFns.requiredValidator()),
          encPublicFlag: new FormControl(detailData.apiOnOff!.publicFlag, ValidatorFns.requiredValidator())
        };
      default:
        return {
          reqSubtype: '',
          reqDesc: '',
          effectiveDate: '',
          encPublicFlag: ''
        };
    }
  }

  public get reqSubtype() { return this.form.get('reqSubtype'); };
  public get reqDesc() { return this.form.get('reqDesc'); };
  public get effectiveDate() { return this.form.get('effectiveDate'); };
  public get encPublicFlag() { return this.form.get('encPublicFlag'); };
}
