import { ToolService } from './../../../shared/services/tool.service';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { FuncService } from './../../../shared/services/api-func.service';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { TranslateService } from '@ngx-translate/core';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import {
  AA0103List,
  AA0103Req,
} from 'src/app/models/api/FuncService/aa0103.interface';
import {
  AA0104List,
  AA0104Req,
} from 'src/app/models/api/FuncService/aa0104.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { AA0102Req } from 'src/app/models/api/FuncService/aa0102.interface';
import { MessageService } from 'primeng/api';
import { DPB9901Req } from 'src/app/models/api/ServerService/dpb9901.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';

@Component({
  selector: 'app-ac0101',
  templateUrl: './ac0101.component.html',
  styleUrls: ['./ac0101.component.css'],
})
export class Ac0101Component extends BaseComponent implements OnInit {
  form: FormGroup;
  detailForm: FormGroup;
  dialogTitle: string = '';
  funcInfoList: Array<AA0103List> = [];
  funcInfoListCols: { field: string; header: string }[] = [];
  // funcInfoListRowCount: number = 0;
  currentFuncInfo?: AA0103List;
  canUpdate: boolean = false;
  canDetail: boolean = false;
  currentTitle: string = this.title;
  pageNum: number = 1; // 1: 查詢、2: 關聯角色、3: 更新
  roleInfoList: Array<AA0104List> = [];
  roleInfoListCols: { field: string; header: string }[] = [];
  // roleInfoListRowCount: number = 0;
  newFunNameLimitChar = { value: 50 };
  newFunDescLimitChar = { value: 300 };
  newReportUrlLimitChar = { value: 2000 };

  file: any = null;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private funcService: FuncService,
    private fb: FormBuilder,
    private toolService: ToolService,
    private translate: TranslateService,
    private roleService: RoleService,
    private messageService: MessageService,
    private serverService: ServerService,
    private ngxService: NgxUiLoaderService,
    private api: ApiBaseService,
    private alertService: AlertService
  ) {
    super(route, tr);
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
  }

  ngOnInit() {
    this.roleService
      .queryRTMapByUk({ txIdList: ['AA0102', 'AA0104'] } as DPB0115Req)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.canUpdate = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0102'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0102')!
                .available
            : false;
          this.canDetail = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0104'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0104')!
                .available
            : false;
        }
      });
    const codes = [
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
    ];
    this.translate.get(codes).subscribe((i18n) => {
      this.funcInfoListCols = [
        { field: 'funcCode', header: i18n['fun_code'] },
        { field: 'funcName', header: i18n['fun_name'] },
        // { field: 'funcNameEn', header: i18n['fun_name_en'] },
        { field: 'funcDesc', header: i18n['fun_desc'] },
        { field: 'locale', header: i18n['fun_locale'] },
        { field: 'updateUser', header: i18n['update_user'] },
        { field: 'updateTime', header: i18n['update_time'] },
      ];
      this.roleInfoListCols = [
        { field: 'roleName', header: i18n['role_name'] },
        { field: 'roleAlias', header: i18n['role_desc'] },
      ];
    });
    this.funcInfoList = [];
    let ReqBody = {
      keyword: this.keyword!.value,
      funcType:
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder('0')) +
        ',' +
        0,
    } as AA0103Req;
    this.funcService.queryTFuncList_v3_ignore1298(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.funcInfoList = res.RespBody.funcInfoList;
      }
    });
  }

  queryFuncList() {
    this.funcInfoList = [];
    let ReqBody = {
      keyword: this.keyword!.value,
      funcType:
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder('0')) +
        ',' +
        0,
    } as AA0103Req;
    this.funcService.queryTFuncList_v3(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.funcInfoList = res.RespBody.funcInfoList;
      }
    });
  }

  moreFuncList() {
    let ReqBody = {
      funcCode: this.funcInfoList[this.funcInfoList.length - 1].funcCode,
      locale: this.funcInfoList[this.funcInfoList.length - 1].locale,
      keyword: this.keyword!.value,
      funcType:
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder('0')) +
        ',' +
        0,
    } as AA0103Req;
    this.funcService.queryTFuncList_v3(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.funcInfoList = this.funcInfoList.concat(res.RespBody.funcInfoList);
      }
    });
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

  moreRoleList() {
    let ReqBody = {
      roleId: this.roleInfoList[this.roleInfoList.length - 1].roleId,
      keyword: this.keyword!.value,
      funcCode: this.currentFuncInfo?.funcCode,
    } as AA0104Req;
    this.funcService.queryTFuncRoleList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.roleInfoList = this.roleInfoList.concat(res.RespBody.roleInfoList);
      }
    });
  }

  async changePage(action: string, rowData?: AA0103List) {
    const code = ['button.update', 'button.roles'];
    const dict = await this.toolService.getDict(code);
    let tmpKeyowrd = this.keyword?.value;
    this.resetFormValidator(this.form);
    this.resetFormValidator(this.detailForm);
    this.keyword?.setValue(tmpKeyowrd);
    this.currentFuncInfo = {} as AA0103List;
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.roles']}`;

        this.pageNum = 2;
        this.currentFuncInfo = rowData;
        this.queryRoleList(true);
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 3;
        this.currentFuncInfo = rowData;
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
        if (this.currentFuncInfo?.reportUrl) {
          this.newReportUrl?.setValue(this.currentFuncInfo?.reportUrl);
          this.newReportUrl?.setValidators([
            ValidatorFns.requiredValidator(),
            ValidatorFns.maxLengthValidator(this.newReportUrlLimitChar.value),
          ]);
        }
        this.checkTextAreaHeight();
        this.pageNum = 3;
        break;
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

  updateFunc() {
    let reqBody = {
      funcCode: this.currentFuncInfo?.funcCode,
      locale: this.currentFuncInfo?.locale,
      desc: this.currentFuncInfo?.funcDesc,
      newDesc: this.newDesc!.value,
      funcName: this.currentFuncInfo?.funcName,
      newFuncName: this.newFuncName!.value,
      // funcNameEn: this.currentFuncInfo?.funcNameEn,
      // newFuncNameEn: this.newFuncNameEn!.value,
    } as AA0102Req;
    if (this.currentFuncInfo?.reportUrl) {
      reqBody.reportUrl = this.currentFuncInfo?.reportUrl;
      reqBody.newReportUrl = this.newReportUrl?.value;
    }
    this.funcService.updateTFunc(reqBody).subscribe(async (res) => {
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

  headerReturn() {
    this.changePage('query');
  }

  exportTsmpFunc() {
    this.ngxService.start();
    this.serverService
      .exportTsmpFunc({
        funcType:
          this.toolService.Base64Encoder(this.toolService.BcryptEncoder('0')) +
          ',' +
          0,
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
          FileSaver.saveAs(data, `TsmpFunc_${date}.xlsx`);
        }
        this.ngxService.stop();
      });
  }

  importTsmpFunc() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importTsmpFunc),
      ReqBody: {
        funcType:
          this.toolService.Base64Encoder(this.toolService.BcryptEncoder('0')) +
          ',' +
          0,
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
    return this.form.get('keyword');
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
  public get d_keyword() {
    return this.detailForm.get('keyword');
  }
}
