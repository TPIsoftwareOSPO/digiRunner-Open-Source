import {
  HttpClient,
  HttpErrorResponse,
  HttpResponse,
} from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ClientCAService } from 'src/app/shared/services/api-certificate-authority.service';
import { DPB0229RespItem } from 'src/app/models/api/ServerService/dpb0229.interface';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0225Req } from 'src/app/models/api/ServerService/dpb0225.interface';
import * as dayjs from 'dayjs';
import { DPB0228Resp } from 'src/app/models/api/ServerService/dpb0228.interface';
import { DPB0226Req } from 'src/app/models/api/ServerService/dpb0226.interface';
import { DPB0231Req } from 'src/app/models/api/ServerService/dpb0231.interface';
import { AlertType } from 'src/app/models/common.enum';
import { DialogService } from 'primeng/dynamicdialog';
import { SslDecoderComponent } from './ssl-decoder/ssl-decoder.component';
import { DPB9901Req } from 'src/app/models/api/ServerService/dpb9901.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { CertInfoComponent } from './cert-info/cert-info.component';
import { environment } from 'src/environments/environment';
import { catchError, map, of } from 'rxjs';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-np0205',
  templateUrl: './np0205.component.html',
  styleUrls: ['./np0205.component.scss'],
  providers: [MessageService, ConfirmationService],
})
export class Np0205Component extends BaseComponent implements OnInit {
  form: FormGroup;
  cols: { field: string; header: string; width?: string }[] = [];
  dataList: Array<DPB0229RespItem> = [];
  selected: Array<DPB0229RespItem> = [];
  pageNum: number = 1;
  currentTitle = this.title;
  currentAction: string = '';
  btnData: MenuItem[] = [];

  detailData?: DPB0228Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private clientCA: ClientCAService,
    private confirmationService: ConfirmationService,
    private alertService: AlertService,
    private messageService: MessageService,
    private dialogService: DialogService,
    private httpClient: HttpClient,
    private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);
    this.form = this.fb.group({
      host: new FormControl(''),
      port: new FormControl(''),
      rootCa: new FormControl(''),
      clientCert: new FormControl(''),
      clientKey: new FormControl(''),
      keyMima: new FormControl(''),
      remark: new FormControl(''),
    });
  }

  ngOnInit() {
    this.init();
    this.port?.valueChanges.subscribe((res) => {
      if (!res) this.port?.setValue('', { emitEvent: false });
    });
  }

  async init() {
    const codes = [
      'status',
      'ca_expired_time',
      'expired_time',
      'update_time',
      'update_user',
    ];
    const dict = await this.toolService.getDict(codes);

    this.cols = [
      { field: 'enable', header: dict['status'] },
      { field: 'hostAndPort', header: 'Host & Post', width: '300px' },
      { field: 'rootCAExpireDate', header: `CA ${dict['ca_expired_time']}` },
      { field: 'crtexpireDate', header: `CRT ${dict['expired_time']}` },
      { field: 'updateDateTime', header: dict['update_time'] },
      { field: 'updateUser', header: dict['update_user'] },
    ];
    this.clientCA.querySiteList_ignore1298().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.infoList;
      }
    });
  }

  querySiteList() {
    this.clientCA.querySiteList().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.infoList;
      }
    });
  }

  async changePage(action: string, rowData?: DPB0229RespItem) {
    const codes = [
      'site',
      'button.detail',
      'button.create',
      'button.update',
      'cfm_del',
      'message.delete',
      'message.success',
      'button.active',
      'button.disable',
      'cfm_mtls_enable',
      'cfm_mtls_disable',
      'message.fail',
    ];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.form);
    this.currentAction = action;
    this.form.enable();
    this.selected = [];

    switch (action) {
      case 'default':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.clientCA.createClientCert_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
            this.currentTitle += `> ${dict['button.create']}`;
            this.pageNum = 2;
          }
        });
        break;
      case 'detail':
        this.clientCA
          .queryClientCertDetail({
            dgrMtlsClientCertId: rowData!.dgrMtlsClientCertId,
          })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.detailData = res.RespBody;
              this.currentTitle += `> ${dict['button.detail']}`;
              this.pageNum = 3;
            }
          });
        break;
      case 'update':
        this.clientCA
          .queryClientCertDetail({
            dgrMtlsClientCertId: rowData!.dgrMtlsClientCertId,
          })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.detailData = res.RespBody;
              this.host?.setValue(res.RespBody.host);
              this.port?.setValue(res.RespBody.port);
              this.rootCa?.setValue(res.RespBody.rootCa);
              this.clientCert?.setValue(res.RespBody.clientCert);
              this.clientKey?.setValue(res.RespBody.clientKey);
              this.keyMima?.setValue(res.RespBody.keyMima);
              this.remark?.setValue(res.RespBody.remark);

              this.clientCA.updateClientCert_before().subscribe((resValid) => {
                if (this.toolService.checkDpSuccess(resValid.ResHeader)) {
                  this.addFormValidator(
                    this.form,
                    resValid.RespBody.constraints
                  );
                  this.currentTitle += `> ${dict['button.update']}`;
                  this.pageNum = 2;
                }
              });
            }
          });
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${rowData?.hostAndPort}`,
          accept: () => {
            this.clientCA
              .deleteClientCert({
                dgrMtlsClientCertId: rowData!.dgrMtlsClientCertId,
              })
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']}`,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.querySiteList();
                }
              });
          },
        });

        break;
    }
  }

  async procEnableClientCert(state: string) {
    const codes = [
      'message.success',
      'message.update',
      'button.active',
      'button.disable',
      'cfm_mtls_enable',
      'cfm_mtls_disable',
    ];
    const dict = await this.toolService.getDict(codes);
    this.confirmationService.confirm({
      header: state == 'Y' ? dict['button.active'] : dict['button.disable'],
      message:
        state == 'Y' ? dict['cfm_mtls_enable'] : dict['cfm_mtls_disable'],
      accept: () => {
        const _IdList: Array<String> = this.selected.map(
          (row) => row.dgrMtlsClientCertId
        );
        this.clientCA
          .enableClientCert({ idList: _IdList!, enable: state })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              // this.alertService.ok(
              // `${dict['message.update']} ${dict['message.success']}`
              // );
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']}`,
                detail: `${dict['message.update']} ${dict['message.success']}`,
              });
              this.querySiteList();
            }
          });
      },
    });
  }

  headerReturn() {
    this.changePage('default');
  }

  formateDate(date: Date) {
    return dayjs(date).format('YYYY-MM-DD') != 'Invalid Date'
      ? dayjs(date).format('YYYY-MM-DD')
      : '';
  }

  async create() {
    let req = {
      host: this.host?.value,
      port: this.port?.value,
      rootCa: this.rootCa?.value,
      clientCert: this.clientCert?.value,
      clientKey: this.clientKey?.value,
      keyMima: this.keyMima?.value,
      remark: this.remark?.value,
    } as DPB0225Req;

    this.clientCA.createClientCert(req).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.create']}`,
          detail: `${dict['message.create']} ${dict['message.success']}!`,
        });
        this.querySiteList();
        this.changePage('default');
      }
    });
  }

  update() {
    let req = {
      dgrMtlsClientCertId: this.detailData?.dgrMtlsClientCertId,
      host: this.host?.value,
      port: this.port?.value,
      rootCa: this.rootCa?.value,
      clientCert: this.clientCert?.value,
      clientKey: this.clientKey?.value,
      keyMima: this.keyMima?.value,
      remark: this.remark?.value,
    } as DPB0226Req;

    this.clientCA.updateClientCert(req).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.update']}`,
          detail: `${dict['message.update']} ${dict['message.success']}!`,
        });
        this.querySiteList();
        this.changePage('default');
      }
    });
  }

  connectionTest() {
    let req = {
      host: this.host?.value,
      port: this.port?.value,
      rootCa: this.rootCa?.value,
      clientCert: this.clientCert?.value,
      clientKey: this.clientKey?.value,
      keyMima: this.keyMima?.value,
    } as DPB0231Req;

    this.clientCA.checkMtlsConnection(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (res.RespBody.success) {
          // console.log(res.RespBody.msg);

          this.alertService.ok(
            'Connect Success',
            res.RespBody.msg,
            AlertType.success
          );
        } else {
          this.alertService.ok(
            'Connect Fail',
            res.RespBody.msg,
            AlertType.error
          );
        }
      }
    });
  }

  procSSLDecoder(_value, type) {
    this.clientCA.SSLDecoder({ cert: _value }).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const ref = this.dialogService.open(SslDecoderComponent, {
          data: { details: res.RespBody },
          header: type,
          width: '800px',
          styleClass: 'cHeader cContent cIcon',
        });
      }
    });
  }

  showCertInfo() {
    const mtlsMemoType =
      this.toolService.getLocale() == 'ZH-TW' ? 'zh_tw' : 'en_us';
    this.ngxSrvice.start();
    this.httpClient
      .get(`${environment.apiUrl}/dgrv4/${mtlsMemoType}/mtlsMemo.md`, {
        responseType: 'text',
        observe: 'response',
        headers: {
          digiRunner: 'mtls memo',
        },
      })
      .pipe(
        map((response: HttpResponse<string>) => {
          const contentType = response.headers.get('Content-Type');

          if (contentType && contentType.includes('text/html')) {
            return null;
          }

          // 如果 Content-Type 正確，回傳 body 內容
          return response.body || '';
        }),
        catchError((error) => {
          // 雖然你的情況是 200，但保留 catchError 處理真正的網路錯誤或伺服器錯誤
          console.error('An unexpected error occurred:', error);
          return of('# 載入檔案時發生錯誤');
        })
      )
      .subscribe(async (res) => {
        // console.log(res);
        if (res) {
          const code = ['cert'];
          const dict = await this.toolService.getDict(code);
          const ref = this.dialogService.open(CertInfoComponent, {
            data: { details: res },
            header: dict['cert'],
            width: '800px',
            styleClass: 'cHeader cContent cIcon',
          });
        }else{
          this.alertService.error('Error','File does not exist.');
        }
        this.ngxSrvice.stop();
      });
  }

  public get host() {
    return this.form.get('host');
  }
  public get port() {
    return this.form.get('port');
  }
  public get rootCa() {
    return this.form.get('rootCa');
  }
  public get clientCert() {
    return this.form.get('clientCert');
  }
  public get clientKey() {
    return this.form.get('clientKey');
  }
  public get keyMima() {
    return this.form.get('keyMima');
  }
  public get remark() {
    return this.form.get('remark');
  }
}
