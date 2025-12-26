import { flatMap } from 'rxjs/operators';
import { DPB0282Field } from './../../../models/api/ServerService/dpb0282.interface';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import {
  DPB0280Req,
  DPB0280WebhookNotify,
} from 'src/app/models/api/ServerService/dpb0280.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ListService } from 'src/app/shared/services/api-list.service';
import { KeyValueFormComponent } from '../../ac02/ac0228/key-value-form/key-value-form.component';
import { DPB0281Req } from 'src/app/models/api/ServerService/dpb0281.interface';
import { DPB0283Req } from 'src/app/models/api/ServerService/dpb0283.interface';
import { DPB0282Resp } from 'src/app/models/api/ServerService/dpb0282.interface';
import { DPB0284Req } from 'src/app/models/api/ServerService/dpb0284.interface';
import { Subscription } from 'rxjs';
import { AlertType } from 'src/app/models/common.enum';
import * as FileSaver from 'file-saver';
import * as dayjs from 'dayjs';

@Component({
  selector: 'app-lb0011',
  templateUrl: './lb0011.component.html',
  styleUrls: ['./lb0011.component.css'],
  providers: [ConfirmationService],
})
export class Lb0011Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  formE!: FormGroup;
  tableData: Array<DPB0280WebhookNotify> = [];
  statusList: { label: string; value: string }[] = [];
  statusListIgnoreAll: { label: string; value: string }[] = [];
  currentAction: string = '';
  btnName: string = '';

  notifyTypeList: { label: string; value: string }[] = [
    { label: 'LINE', value: 'LINE' },
    { label: 'Email', value: 'EMAIL' },
    { label: 'Slack', value: 'SLACK' },
    { label: 'Discord', value: 'DISCORD' },
    // { label: 'Classic', value: 'CLASSIC' },
    { label: 'Http', value: 'HTTP' },
  ];

  tarData?: DPB0282Resp;
  selectedItem?: DPB0280WebhookNotify;
  notifyTypeSubscription: Subscription | undefined;
  _formValid: boolean = false;

  fileName: string = '';
  file: any = null;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private listService: ListService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private ngxService: NgxUiLoaderService
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      webhookNotifyId: new FormControl(''),
      keyword: new FormControl(''),
      enable: new FormControl('-1'),
      paging: new FormControl(''),
    });
    this.formE = this.fb.group({
      notifyName: new FormControl(''),
      notifyType: new FormControl(''),
      enable: new FormControl(''),
      message: new FormControl(''),
      payloadFlag: new FormControl('0'),
      fieldList: new FormControl(''),
      url: new FormControl(''),
    });

    this.notifyTypeE.valueChanges.subscribe((res) => {
      if (res == 'HTTP') this.messageE.setValue('');
    });

    this.getStatusList();
    this.serverService.queryWebhookNotify_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.webhookNotifyList;
      }
    });
  }

  queryWebhookNotify() {
    let req = {
      keyword: this.keyword.value,
      enable: this.getStatusEncodeString(this.enable.value),
    } as DPB0280Req;
    this.serverService.queryWebhookNotify(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.webhookNotifyList;
      } else this.tableData = [];
    });
  }

  getStatusEncodeString(status: string) {
    return status == '-1'
      ? status
      : this.toolService.Base64Encoder(this.toolService.BcryptEncoder(status)) +
          ',' +
          this.statusList.findIndex((item) => item.value == status);
  }

  getStatusList() {
    let apiStatusReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('ENABLE_FLAG')
        ) +
        ',' +
        9,
      isDefault: 'N',
    } as DPB0047Req;
    this.listService
      .querySubItemsByItemNo(apiStatusReqBody)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          let statusOpt: { label: string; value: string }[] = [];
          if (res.RespBody.subItems) {
            res.RespBody.subItems
              .filter((item) => item.subitemNo !== '2')
              .map((item) => {
                // if (item.subitemNo != '2') {
                //   // 不要鎖定
                statusOpt.push({
                  label: item.subitemName,
                  value: item.param2 ? item.param2 : '-1',
                });
                // }
              });
          }
          this.statusList = statusOpt;

          this.statusListIgnoreAll = this.statusList.filter(
            (x) => x.value != '-1'
          );
        }
      });
  }

  getMoreData() {
    let req = {
      keyword: this.keyword.value,
      enable: this.getStatusEncodeString(this.enable.value),
      webhookNotifyId:
        this.tableData[this.tableData.length - 1].webhookNotifyId,
    } as DPB0280Req;
    this.serverService.queryWebhookNotify(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.webhookNotifyList);
      }
    });
  }

  async changePage(action: string, rowData?: DPB0280WebhookNotify) {
    this.currentAction = action;
    if (this.notifyTypeSubscription) {
      this.notifyTypeSubscription.unsubscribe();
      this.notifyTypeSubscription = undefined;
    }
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
    ];
    const dict = await this.toolService.getDict(code);
    let _enable = this.enable.value;
    let _keyword = this.keyword.value;
    this.resetFormValidator(this.form);
    this.resetFormValidator(this.formE);
    this.keyword.setValue(_keyword);
    this.enable.setValue(_enable);
    this.formE.enable();

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.btnName = dict['button.create'];
        this.pageNum = 2;
        this.serverService.createWebhookNotify_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.formE, res.RespBody.constraints);
            this.fieldListE.setValue('');
            this.enableE.setValue('N');
            this.payloadFlagE.setValue('0');
            this.notifyTypeE.setValue('HTTP'); //set default : HTTP
          }
        });

        this.notifyTypeSubscription = this.notifyTypeE.valueChanges.subscribe(
          (res) => {
            this.urlE.setValue('');
            this.payloadFlagE.setValue('0');
            setTimeout(() => {
              switch (res) {
                case 'LINE':
                  this.fieldListE.setValue(
                    JSON.stringify([
                      { key: 'authorization', value: '', required: true },
                      { key: 'to', value: '' },
                      { key: 'notificationDisabled', value: 'N' },
                    ])
                  );
                  break;
                case 'EMAIL':
                  this.fieldListE.setValue(
                    JSON.stringify([
                      { key: 'subject', value: '', required: true },
                      { key: 'recipients', value: '', required: true },
                    ])
                  );
                  break;
                case 'HTTP':
                  this._formValid = false;
                  this.fieldListE.setValue(
                    JSON.stringify([
                      { key: 'url', value: '', type: '0', mappingUrl: '' },
                      {
                        key: 'percent',
                        value: '100',
                        type: '0',
                        mappingUrl: '',
                      },
                    ])
                  );
                  break;
                case 'SLACK':
                  this.fieldListE.setValue(
                    JSON.stringify([
                      { key: 'url', value: '', required: true },
                      { key: 'username', value: '' },
                      { key: 'icon_emoji', value: '' },
                      { key: 'channel', value: '' },
                    ])
                  );
                  break;
                case 'DISCORD':
                  this.fieldListE.setValue(
                    JSON.stringify([
                      { key: 'url', value: '', required: true },
                      { key: 'username', value: '' },
                      { key: 'avatar_url', value: '' },
                    ])
                  );
                  break;
                default:
                  this.fieldListE.setValue('[]');
                  this.payloadFlagE.setValue('0');
                  break;
              }
            }, 0);
          }
        );
        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.btnName = dict['button.detail'];
        this.serverService
          .getWebhookNotify({ webhookNotifyId: rowData!.webhookNotifyId })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.formE.disable();
              this.notifyNameE.setValue(res.RespBody.notifyName);
              this.notifyTypeE.setValue(res.RespBody.notifyType);
              this.fieldListE.setValue(JSON.stringify(res.RespBody.fieldList));
              this.enableE.setValue(res.RespBody.enable);
              this.payloadFlagE.setValue(res.RespBody.payloadFlag);
              this.messageE.setValue(res.RespBody.message);
              this.pageNum = 2;
            }
          });
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.btnName = dict['button.update'];

        this.serverService
          .updateWebhookNotify_before()
          .subscribe((resValid) => {
            if (this.toolService.checkDpSuccess(resValid.ResHeader)) {
              this.addFormValidator(this.formE, resValid.RespBody.constraints);

              this.serverService
                .getWebhookNotify({ webhookNotifyId: rowData!.webhookNotifyId })
                .subscribe((res) => {
                  if (this.toolService.checkDpSuccess(res.ResHeader)) {
                    this.notifyNameE.setValue(res.RespBody.notifyName);
                    this.notifyNameE.disable();
                    this.notifyTypeE.setValue(res.RespBody.notifyType);
                    this.notifyTypeE.disable();

                    let oriFieldList = [...res.RespBody.fieldList];
                    let addRequiredTagFieldList: Array<DPB0282Field> = [];
                    let required: Array<string> = [];
                    switch (res.RespBody.notifyType) {
                      case 'LINE':
                        required = ['authorization'];
                        break;
                      case 'EMAIL':
                        required = ['subject', 'recipients'];
                        break;
                      case 'SLACK':
                      case 'DISCORD':
                        required = ['url'];
                        break;
                      default:
                        break;
                    }
                    addRequiredTagFieldList = oriFieldList.map((item) => {
                      if (required.includes(item.key)) {
                        return { ...item, required: true };
                      }
                      return item;
                    });
                    this.fieldListE.setValue(
                      JSON.stringify(addRequiredTagFieldList)
                    );

                    this.enableE.setValue(res.RespBody.enable);
                    this.payloadFlagE.setValue(res.RespBody.payloadFlag);
                    this.messageE.setValue(res.RespBody.message);

                    this.tarData = res.RespBody;
                    this.pageNum = 2;
                  }
                });
            }
          });
        break;
      case 'delete':
        this.selectedItem = rowData;
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.deleteConfirm();
          },
        });
        break;
    }
  }

  deleteConfirm() {
    this.messageService.clear();
    let ReqBody = {
      webhookNotifyId: this.selectedItem?.webhookNotifyId,
    } as DPB0284Req;

    this.serverService.deleteWebhookNotify(ReqBody).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.delete']} `,
          detail: `${dict['message.delete']} ${dict['message.success']}!`,
        });

        this.changePage('query');
        this.queryWebhookNotify();
      }
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  procData() {
    switch (this.currentAction) {
      case 'create':
        let req = {
          notifyName: this.notifyNameE.value,
          notifyType: this.notifyTypeE.value,
          enable: this.getStatusEncodeString(this.enableE.value),
          message:
            this.notifyTypeE.value != 'HTTP' ? this.messageE.value : null,
          payloadFlag: this.payloadFlagE.value,
          fieldList: this.fieldListE.value,
        } as DPB0281Req;

        this.serverService.createWebhookNotify(req).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.create', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']} `,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.queryWebhookNotify();
          }
        });
        break;
      case 'update':
        let reqU = {
          webhookNotifyId: this.tarData?.webhookNotifyId,
          enable: this.getStatusEncodeString(this.enableE.value),
          message:
            this.notifyTypeE.value != 'HTTP' ? this.messageE.value : null,
          payloadFlag: this.payloadFlagE.value,
          fieldList: this.fieldListE.value,
        } as DPB0283Req;
        this.serverService.updateWebhookNotify(reqU).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.update', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']} `,
              detail: `${dict['message.update']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.queryWebhookNotify();
          }
        });
        break;
      default:
        break;
    }
  }

  fieldFormator(data) {
    let keyValueList: Array<any> = [];
    const kvObj = JSON.parse(data);
    if (Array.isArray(kvObj)) {
      kvObj.forEach((item) => {
        Object.keys(item).map((key) => {
          if (key && key != '') {
            keyValueList.push({
              key: key,
              value: item[key],
            });
          }
        });
      });
    }
    return keyValueList;
  }

  set_formValid(event) {
    this._formValid = event;
  }

  exportFile() {
    this.ngxService.start();
    this.serverService.exportWebhook().subscribe((res) => {
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
        FileSaver.saveAs(data, `Webhook_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  importFile() {
    this.serverService.importWebhook(this.file).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['uploading', 'message.success', 'upload_result'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: dict['upload_result'],
          detail: `${dict['message.success']}!`,
        });
        this.file = null;
        this.fileName = '';
        this.queryWebhookNotify();
      }
      this.ngxService.stop();
    });
  }

  openFileBrowser() {
    $('#file').click();
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;
    if (file.length != 0) {
      this.file = file.item(0);
      this.fileName = file[0].name;
      event.target.value = '';
    } else {
      // this.fileData!.setValue(null);
      this.file = null;
      event.target.value = '';
    }
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get enable() {
    return this.form.get('enable')!;
  }

  public get notifyNameE() {
    return this.formE.get('notifyName')!;
  }
  public get notifyTypeE() {
    return this.formE.get('notifyType')!;
  }
  public get enableE() {
    return this.formE.get('enable')!;
  }
  public get messageE() {
    return this.formE.get('message')!;
  }
  public get payloadFlagE() {
    return this.formE.get('payloadFlag')!;
  }
  public get fieldListE() {
    return this.formE.get('fieldList')!;
  }
  public get urlE() {
    return this.formE.get('url')!;
  }
}
