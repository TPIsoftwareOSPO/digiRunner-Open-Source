import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ConfirmationService, MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import {
  AA0103List,
  AA0103Req,
} from 'src/app/models/api/FuncService/aa0103.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { FuncService } from 'src/app/shared/services/api-func.service';
import {
  AA0106Req,
  AA0106ReqItem,
} from 'src/app/models/api/FuncService/aa0106.interface';
import {
  AA0104List,
  AA0104Req,
} from 'src/app/models/api/FuncService/aa0104.interface';
import { AA0102Req } from 'src/app/models/api/FuncService/aa0102.interface';
import { DPB9901Req } from 'src/app/models/api/ServerService/dpb9901.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-ac0105',
  templateUrl: './ac0105.component.html',
  styleUrls: ['./ac0105.component.css'],
  providers: [ConfirmationService],
})
export class Ac0105Component extends BaseComponent implements OnInit {
  funcInfoList: Array<AA0103List> = [];
  form!: FormGroup;
  detailForm!: FormGroup;
  editForm!: FormGroup;
  currentTitle: string = this.title;
  pageNum: number = 1;

  newFunNameLimitChar = { value: 50 };
  newFunDescLimitChar = { value: 300 };
  newReportUrlLimitChar = { value: 2000 };
  currentFuncInfo: AA0103List = {
    funcCode: '',
    locale: '',
    funcName: '',
    funcNameEn: '',
    funcDesc: '',
    updateUser: '',
    updateTime: '',
    funcType: '',
    localeName:''
  };
  funcInfoListCols: { field: string; header: string }[] = [];
  roleInfoListCols: { field: string; header: string }[] = [];
  roleInfoList: Array<AA0104List> = [];
  file: any = null;

  typeSubscription?: Subscription;
  mFuncData: { funcCode: string; funcName: string }[] = [];

  embbededUrlTip: string = ``;


  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private translate: TranslateService,
    private messageService: MessageService,
    private funcService: FuncService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private ngxService: NgxUiLoaderService,
    private api: ApiBaseService,
    private alertService: AlertService
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.form = this.fb.group({
      keyword: new FormControl(''),
      newFuncName: new FormControl(
        '',
        ValidatorFns.stringSpaceAliasValidator(this.newFunNameLimitChar.value)
      ),
      newFuncNameEn: new FormControl(
        '',
        ValidatorFns.stringNameSpaceValidator(this.newFunNameLimitChar.value)
      ),
      newDesc: new FormControl(
        '',
        ValidatorFns.maxLengthValidator(this.newFunDescLimitChar.value)
      ),
      newReportUrl: new FormControl(
        '',
        ValidatorFns.maxLengthValidator(this.newReportUrlLimitChar.value)
      ),
    });

    this.detailForm = this.fb.group({
      keyword: new FormControl(''),
    });

    this.editForm = this.fb.group({
      embeddedUrl: new FormControl(''),
      funcList: new FormControl(),
      type: new FormControl(0),
      masterFuncCode: new FormControl(''),
      isKibana: new  FormControl(false),
    });

    const code = [
      'fun_code',
      'fun_name',
      'fun_name_en',
      'fun_desc',
      'fun_locale',
      'update_user',
      'update_time',
      'role_name',
      'role_alias',
      'role_desc',
      "embedded_url_tip.type",
      "embedded_url_tip.1",
      "embedded_url_tip.1_1",
      "embedded_url_tip.1_2",
      "embedded_url_tip.1_3",
      "embedded_url_tip.2",
      "embedded_url_tip.2_1",
      "embedded_url_tip.3",
      "embedded_url_tip.3_1",
      "embedded_url_tip.4",
      "embedded_url_tip.4_1",
      "embedded_url_tip.5",
      "embedded_url_tip.6",
    ];
    const dict = await this.toolService.getDict(code);

    this.funcInfoListCols = [
      { field: 'funcCode', header: dict['fun_code'] },
      { field: 'funcName', header: dict['fun_name'] },
      // { field: 'funcNameEn', header: dict['fun_name_en'] },
      { field: 'funcDesc', header: dict['fun_desc'] },
      { field: 'reportType', header: dict['embedded_url_tip.type'] },
      { field: 'localeName', header: dict['fun_locale'] },
      { field: 'updateUser', header: dict['update_user'] },
      { field: 'updateTime', header: dict['update_time'] },
    ];
    this.roleInfoListCols = [
      { field: 'roleName', header: dict['role_name'] },
      { field: 'roleAlias', header: dict['role_desc'] },
    ];

    this.funcService
      .queryTFuncList_v3_ignore1298({
        funcType:
          this.toolService.Base64Encoder(this.toolService.BcryptEncoder('1')) +
          ',' +
          1,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.funcInfoList = res.RespBody.funcInfoList;
        }
      });

      this.embbededUrlTip = `<ol style="padding-inline-start: 20px;margin-block-end: 0;"><li>${dict["embedded_url_tip.1"]}<ol style="padding-inline-start: 20px;margin-block-end: 0;"><li>${dict["embedded_url_tip.1_1"]}</li><li>${dict["embedded_url_tip.1_2"]}</li><li>${dict["embedded_url_tip.1_3"]}</li></ol></li><li>${dict["embedded_url_tip.2"]}<ol style="padding-inline-start: 20px;margin-block-end: 0;"><li>${dict["embedded_url_tip.2_1"]}</li></ol></li><li>${dict["embedded_url_tip.3"]}<ol style="padding-inline-start: 20px;margin-block-end: 0;"><li>${dict["embedded_url_tip.3_1"]}</li></ol></li><li>${dict["embedded_url_tip.4"]}<ol style="padding-inline-start: 20px;margin-block-end: 0;"><li>${dict["embedded_url_tip.4_1"]}</li></ol></li><li>${dict["embedded_url_tip.5"]}</li><li>${dict["embedded_url_tip.6"]}</li></ol>`;
  }

  headerReturn() {
    this.changePage('query');
  }

  queryFuncList() {
    this.funcInfoList = [];
    let ReqBody = {
      keyword: this.keyword!.value,
      funcType:
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder('1')) +
        ',' +
        1,
    } as AA0103Req;
    this.funcService.queryTFuncList_v3(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.funcInfoList = res.RespBody.funcInfoList;
      }
    });
  }

  async changePage(action: string, rowData?: AA0103List) {
    const code = [
      'button.update',
      'button.create',
      'cfm_del',
      'fun_code',
      'fun_name',
      'button.roles',
      'message.delete',
      'message.success',
      'include_locale_sub_func',
      'include_locale'
    ];
    const dict = await this.toolService.getDict(code);
    this.resetFormValidator(this.form);
    this.resetFormValidator(this.detailForm);
    this.typeSubscription?.unsubscribe();

    this.currentFuncInfo = {} as AA0103List;
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':

      this.funcService.queryCusMasterFunc().subscribe(res=>{
        if(this.toolService.checkDpSuccess(res.ResHeader)){

          this.mFuncData = res.RespBody.funcInfoList;

          this.currentTitle = `${this.title} > ${dict['button.create']}`;

          this.embeddedUrl.setValidators([
            // ValidatorFns.requiredValidator(),
            ValidatorFns.maxLengthValidator(2000),
          ]);
          this.embeddedUrl?.setValue('');
          this.funcList?.setValidators(ValidatorFns.requiredValidator());
          this.funcList?.setValue('', ValidatorFns.requiredValidator());
          this.type.setValue(0, ValidatorFns.requiredValidator())
          this.isKibana.setValue(true);
          this.isKibana.valueChanges.subscribe(res=>{
            if(res){
              this.masterFuncCode.clearValidators();
              this.masterFuncCode.updateValueAndValidity();

            }else{
              this.masterFuncCode.setValidators([
                ValidatorFns.requiredValidator(),
              ]);
            }
            this.masterFuncCode.setValue('');
          })

          this.typeSubscription = this.type.valueChanges.subscribe( value => {
            if(value == 1){
              this.embeddedUrl?.setValue('');
              this.funcList?.setValue('');
              this.masterFuncCode.setValue('');
              this.embeddedUrl.setValidators([
                ValidatorFns.requiredValidator(),
                ValidatorFns.maxLengthValidator(2000),
              ]);
              this.masterFuncCode.setValidators([
                ValidatorFns.requiredValidator(),
              ]);
            }
            else{
              this.embeddedUrl.clearValidators();
              this.embeddedUrl.setValidators(ValidatorFns.maxLengthValidator(2000))
              this.embeddedUrl.updateValueAndValidity();
              this.masterFuncCode.clearValidators();
              this.masterFuncCode.updateValueAndValidity();
            }
          })

          this.pageNum = 4;
        }
      })



        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.roles']}`;
        this.pageNum = 2;
        this.currentFuncInfo = rowData!;
        this.queryRoleList(true);
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;

        this.currentFuncInfo = rowData!;
        this.newFuncName!.setValidators([
          ValidatorFns.requiredValidator(),
          ValidatorFns.maxLengthValidator(this.newFunNameLimitChar.value),
        ]);
        this.newFuncName!.setValue(this.currentFuncInfo!.funcName);
        // this.newFuncNameEn!.setValidators(
        //   ValidatorFns.stringNameSpaceValidator(this.newFunNameLimitChar.value)
        // );
        // this.newFuncNameEn!.setValue(this.currentFuncInfo!.funcNameEn);
        this.newDesc!.setValidators(
          ValidatorFns.maxLengthValidator(this.newFunDescLimitChar.value)
        );
        this.newDesc!.setValue(this.currentFuncInfo!.funcDesc);
        this.checkTextAreaHeight();
        if (this.currentFuncInfo?.reportUrl) {
          this.newReportUrl?.setValue(this.currentFuncInfo?.reportUrl);
          this.newReportUrl?.setValidators([
            // ValidatorFns.requiredValidator(),
            ValidatorFns.maxLengthValidator(2000),
          ]);
        }

        this.pageNum = 3;
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${dict['fun_code']}: ${rowData?.funcCode}, ${dict['fun_name']}: ${rowData?.funcName}, ${rowData?.funcCode.length==4 ? dict['include_locale_sub_func']:dict['include_locale']}`,
          accept: () => {
            this.funcService
              .deleteReport({
                funcCode: rowData!.funcCode,
                locale: rowData!.locale,
              })
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']}`,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.queryFuncList();
                }
              });
          },
        });
        break;
    }
  }

  queryRoleList(init: boolean = false) {
    this.roleInfoList = [];
    let ReqBody = {
      keyword: this.d_keyword!.value,
      funcCode: this.currentFuncInfo?.funcCode,
    } as AA0104Req;
    if (init) {
      this.funcService
        .queryTFuncRoleList_ignore1298(ReqBody)
        .subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.roleInfoList = res.RespBody.roleInfoList;
          }
        });
    } else {
      this.funcService.queryTFuncRoleList(ReqBody).subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.roleInfoList = res.RespBody.roleInfoList;
        }
      });
    }
  }

  checkTextAreaHeight() {
    const textArea = document.getElementById('newDescTextArea');
    if (
      this.currentFuncInfo!.funcDesc &&
      textArea &&
      textArea.clientHeight === 0
    ) {
      textArea.style.height = '254px';
    }
  }

  create() {
    let req = {
      type: this.type.value,
      funcList: this.funcList.value.map((item) => {
        return {
          locale: item.locale,
          funcName: item.funcName,
          funcDesc: item.funcDesc,
        } as AA0106ReqItem;
      }),
    } as AA0106Req;
    if(this.type.value == 1){
      req.embeddedUrl = this.embeddedUrl.value;
      req.isKibana = this.isKibana.value;
      if(!this.isKibana.value) req.masterFuncCode = this.masterFuncCode.value;
    }

    this.funcService.addReport(req).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success', 'message.user'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.create']}`,
          detail: `${dict['message.create']} ${dict['message.success']}!`,
        });
        this.queryFuncList();
        this.changePage('query');
      }
    });
  }

  moreFuncList() {
    let ReqBody = {
      funcCode: this.funcInfoList[this.funcInfoList.length - 1].funcCode,
      locale: this.funcInfoList[this.funcInfoList.length - 1].locale,
      keyword: this.keyword!.value,
      funcType:
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder('1')) +
        ',' +
        1,
    } as AA0103Req;
    this.funcService.queryTFuncList_v3(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.funcInfoList = this.funcInfoList.concat(res.RespBody.funcInfoList);
      }
    });
  }

  updateFunc() {
    let ReqBody = {
      funcCode: this.currentFuncInfo?.funcCode,
      locale: this.currentFuncInfo?.locale,
      desc: this.currentFuncInfo?.funcDesc,
      newDesc: this.newDesc!.value,
      funcName: this.currentFuncInfo?.funcName,
      newFuncName: this.newFuncName!.value,
      reportUrl: this.currentFuncInfo?.reportUrl,
      newReportUrl: this.newReportUrl!.value,
    } as AA0102Req;

    if(this.currentFuncInfo.reportType){
      ReqBody.isKibana  = this.currentFuncInfo.reportType == 'SYS_RPT' ;
    }
    this.funcService.updateTFunc(ReqBody).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.func', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.update']} ${dict['message.func']}`,
          detail: `${dict['message.update']} ${dict['message.success']}!`,
        });
        this.queryFuncList();
        this.changePage('query');
      }
    });
  }

  exportTsmpFunc() {
    this.ngxService.start();
    this.serverService
      .exportTsmpFunc({
        funcType:
          this.toolService.Base64Encoder(this.toolService.BcryptEncoder('1')) +
          ',' +
          1,
      })
      .subscribe((res) => {
        if (res.type === 'application/json') {
          const reader = new FileReader();
          reader.onload = () => {
            const jsonData = JSON.parse(reader.result as string);
            this.alertService.ok(
              jsonData.ResHeader.rtnMsg,
              '',
              AlertType.warning,
              jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID
            );
          };
          reader.readAsText(res);
        } else {
          const data: Blob = new Blob([res], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
          });

          const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
          FileSaver.saveAs(data, `EmbededFunc_${date}.xlsx`);
        }
        this.ngxService.stop();
      });
  }

  importTsmpFunc() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importTsmpFunc),
      ReqBody: {
        funcType:
          this.toolService.Base64Encoder(this.toolService.BcryptEncoder('1')) +
          ',' +
          1,
      },
    };
    this.ngxService.start();
    this.serverService.importTsmpFunc(req, this.file).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['uploading', 'message.success', 'upload_result'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: dict['upload_result'],
          detail: `${dict['message.success']}!`,
        });
        this.file = null;
        this.queryFuncList();
      }
      this.ngxService.stop();
    });
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;
    const code = [
      'uploading',
      'cfm_img_format',
      'cfm_size',
      'message.success',
      'upload_result',
      'waiting',
    ];
    const dict = await this.toolService.getDict(code);
    if (file.length != 0) {
      this.file = file.item(0);
      event.target.value = '';
    } else {
      // this.fileData!.setValue(null);
      this.file = null;
      event.target.value = '';
    }
  }

  openFileBrowser() {
    $('#file').click();
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get newFuncName() {
    return this.form.get('newFuncName');
  }
  // public get newFuncNameEn() {
  //   return this.form.get('newFuncNameEn');
  // }
  public get newDesc() {
    return this.form.get('newDesc');
  }
  public get newReportUrl() {
    return this.form.get('newReportUrl');
  }
  public get embeddedUrl() {
    return this.editForm.get('embeddedUrl')!;
  }
  public get funcList() {
    return this.editForm.get('funcList')!;
  }
  public get d_keyword() {
    return this.detailForm.get('keyword')!;
  }
  public get type() {
    return this.editForm.get('type')!;
  }
  public get masterFuncCode() {
    return this.editForm.get('masterFuncCode')!;
  }
  public get isKibana() {
    return this.editForm.get('isKibana')!;
  }
}
