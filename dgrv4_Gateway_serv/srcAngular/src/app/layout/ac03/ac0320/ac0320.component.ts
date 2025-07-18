import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { DPB0294RespItem } from 'src/app/models/api/ServerService/dpb0294.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB0290Req } from 'src/app/models/api/ServerService/dpb0290.interface';
import { DPB0292Req } from 'src/app/models/api/ServerService/dpb0292.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { Subscription } from 'rxjs';
import { saveAs } from 'file-saver';
import { DgrGrpcProxyMapDto } from 'src/app/models/api/ServerService/dpb0296.interface';

@Component({
  selector: 'app-ac0320',
  templateUrl: './ac0320.component.html',
  styleUrls: ['./ac0320.component.css'],
  providers: [MessageService, ConfirmationService, ApiService],
})
export class Ac0320Component extends BaseComponent implements OnInit {
  pageNum: number = 1;
  currentTitle: string = this.title;

  tableData: Array<DPB0294RespItem> = [];
  selected: Array<DPB0294RespItem> = [];
  formEdit!: FormGroup;
  formFile!: FormGroup;
  currentAction: string = '';

  selectedItem?: DPB0294RespItem;
  secureModeSub?: Subscription;
  importSelected: DgrGrpcProxyMapDto[] = [];

  checkImportStatus: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private messageService: MessageService,
    private toolService: ToolService,
    private ngxService: NgxUiLoaderService,
    private fb: FormBuilder,
    private serverService: ServerService,
    private confirmationService: ConfirmationService
  ) {
    super(route, tr);
  }

  ngOnInit(): void {
    this.formEdit = this.fb.group({
      gRPCProxyMapId: new FormControl(''),
      serviceName: new FormControl(''),
      proxyHostName: new FormControl(''),
      targetHostName: new FormControl(''),
      targetPort: new FormControl(''),
      connectTimeoutMs: new FormControl('5000'),
      sendTimeoutMs: new FormControl('10000'),
      readTimeoutMs: new FormControl('30000'),
      secureMode: new FormControl('SECURE'),
      trustedCertsContent: new FormControl(''),
      enable: new FormControl(''),
    });

    this.formFile = this.fb.group({
      file: new FormControl(),
      fileName: new FormControl({ value: '', disabled: true }),
    });

    this.serverService.queryGrpcService_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.infoList;
      } else this.tableData = [];
    });
  }

  queryGrpcService() {
    this.selected = [];
    this.serverService.queryGrpcService({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.infoList;
      } else this.tableData = [];
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  async changePage(action: string = 'query', rowData?: DPB0294RespItem) {
    const codes = [
      'button.detail',
      'button.create',
      'button.update',
      'cfm_del',
      'message.delete',
      'message.success',
    ];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);
    this.currentAction = action;
    // console.log(action, rowData);
    this.secureModeSub?.unsubscribe();
    this.secureModeSub = undefined;

    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.serverService.createGrpcService_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.currentTitle += `> ${dict['button.create']}`;
            this.pageNum = 2;
            this.enable?.setValue('N');
            this.connectTimeoutMs?.setValue('5000');
            this.sendTimeoutMs?.setValue('10000');
            this.readTimeoutMs?.setValue('30000');
            this.secureMode.setValue('PLAINTEXT');
            this.secureModeSub = this.secureMode.valueChanges.subscribe(
              (res) => {
                if (res === 'SECURE') {
                  this.trustedCertsContent.addValidators([
                    ValidatorFns.requiredValidator(),
                  ]);
                  this.trustedCertsContent.updateValueAndValidity();
                  this.trustedCertsContent.markAsTouched();
                } else {
                  this.trustedCertsContent.clearValidators();
                  this.trustedCertsContent.clearAsyncValidators();
                  this.trustedCertsContent.setValue('');
                  this.formEdit.updateValueAndValidity();
                  this.trustedCertsContent.markAsUntouched();
                  this.trustedCertsContent.markAsPristine();
                }
              }
            );
          }
        });
        break;
      case 'update':
        this.serverService.updateGrpcService_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.currentTitle += `> ${dict['button.update']}`;

            this.gRPCProxyMapId.setValue(rowData?.gRPCProxyMapId);
            this.serviceName.setValue(rowData?.serviceName);
            this.proxyHostName.setValue(rowData?.proxyHostName);
            this.targetHostName.setValue(rowData?.targetHostName);
            this.targetPort.setValue(rowData?.targetPort);
            this.connectTimeoutMs.setValue(rowData?.connectTimeoutMs);
            this.sendTimeoutMs.setValue(rowData?.sendTimeoutMs);
            this.readTimeoutMs.setValue(rowData?.readTimeoutMs);
            this.enable.setValue(rowData?.enable);

            this.secureMode.setValue(rowData?.secureMode);
            this.trustedCertsContent.setValue(rowData?.trustedCertsContent);

            this.selectedItem = rowData;
            if (this.secureMode.value == 'SECURE') {
              this.trustedCertsContent.addValidators([
                ValidatorFns.requiredValidator(),
              ]);
              this.trustedCertsContent.updateValueAndValidity();
            }

            this.secureModeSub = this.secureMode.valueChanges.subscribe(
              (res) => {
                if (res === 'SECURE') {
                  this.trustedCertsContent.addValidators([
                    ValidatorFns.requiredValidator(),
                  ]);
                  this.trustedCertsContent.updateValueAndValidity();
                  this.trustedCertsContent.markAsTouched();
                } else {
                  this.trustedCertsContent.clearValidators();
                  this.trustedCertsContent.clearAsyncValidators();
                  this.trustedCertsContent.setValue('');
                  this.formEdit.updateValueAndValidity();
                  this.trustedCertsContent.markAsUntouched();
                  this.trustedCertsContent.markAsPristine();
                }
              }
            );

            this.pageNum = 2;
          }
        });
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.serverService
              .deleteGrpcService({ gRPCProxyMapId: rowData!.gRPCProxyMapId })
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  const code = [
                    'message.delete',
                    'message.role_role_mapping',
                    'message.success',
                  ];
                  const dict = await this.toolService.getDict(code);
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']} `,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.queryGrpcService();
                  this.changePage('query');
                }
              });
          },
        });
        break;
      default:
        break;
    }
  }

  create() {
    let req = {
      serviceName: this.serviceName?.value,
      proxyHostName: this.proxyHostName?.value,
      targetHostName: this.targetHostName?.value,
      targetPort: this.targetPort?.value,
      connectTimeoutMs: this.connectTimeoutMs?.value,
      sendTimeoutMs: this.sendTimeoutMs?.value,
      readTimeoutMs: this.readTimeoutMs?.value,
      enable: this.enable?.value,
      secureMode: this.secureMode.value,
      autoTrustUpstreamCerts: 'Y',
    } as DPB0290Req;
    if (req.secureMode === 'SECURE')
      req.trustedCertsContent = this.trustedCertsContent.value;
    this.serverService.createGrpcService(req).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.create']}`,
          detail: `${dict['message.create']} ${dict['message.success']}!`,
        });
        this.queryGrpcService();
        this.changePage('query');
      }
    });
  }

  update() {
    let req = {
      gRPCProxyMapId: this.gRPCProxyMapId?.value,
      serviceName: this.serviceName?.value,
      proxyHostName: this.proxyHostName?.value,
      targetHostName: this.targetHostName?.value,
      targetPort: this.targetPort?.value,
      connectTimeoutMs: String(this.connectTimeoutMs?.value),
      sendTimeoutMs: String(this.sendTimeoutMs?.value),
      readTimeoutMs: String(this.readTimeoutMs?.value),
      enable: this.enable?.value,
      secureMode: this.secureMode.value,
      autoTrustUpstreamCerts: 'Y',
    } as DPB0292Req;
    if (req.secureMode === 'SECURE')
      req.trustedCertsContent = this.trustedCertsContent.value;
    this.serverService.updateGrpcService(req).subscribe(async (res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.update']}`,
          detail: `${dict['message.update']} ${dict['message.success']}!`,
        });
        this.queryGrpcService();
        this.changePage('query');
      }
    });
  }

  updateStatus(status: string = 'N') {
    this.serverService
      .batchUpdateProxyStatus({
        enable: status,
        proxyIds: this.selected.map((rowData) => rowData.gRPCProxyMapId),
      })
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['message.update', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['message.update']}`,
            detail: `${dict['message.update']} ${dict['message.success']}!`,
          });
          this.queryGrpcService();
          this.changePage('query');
        }
      });
  }

  export() {
    let ids = this.selected.map((rowData) => rowData.gRPCProxyMapId);

    this.ngxService.start();
    this.serverService.exportGrpcSetting(ids).subscribe((res) => {
      this.ngxService.stop();
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const blob = new Blob([JSON.stringify(res.RespBody.data, null, 2)], {
          type: 'application/json',
        });
        saveAs(blob, res.RespBody.fileName || 'grpc_export_file.json');
      }
    });
  }

  importFile?: File;
  importList: DgrGrpcProxyMapDto[] = [];
  async changePageImport() {
    const code = ['button.import'];
    const dict = await this.toolService.getDict(code);
    this.currentTitle = `${this.title} > ${dict['button.import']}`;
    this.resetFile();

    this.pageNum = 3;
  }

  uploadFile() {
    this.checkImportStatus = false;
    let fileReader = new FileReader();
    fileReader.onloadend = () => {
      this.serverService
        .parseGrpcSetting(null, this.importFile!)
        .subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.importSelected = [];
            this.importList = res.RespBody;
            const code = ['uploading', 'upload_result', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: dict['upload_result'],
              detail: `${dict['message.success']}!`,
            });
          }
        });
    };
    fileReader.readAsText(this.importFile!);
  }

  changeFile(event) {
    this.importList = [];
    this.importSelected = [];
    this.checkImportStatus = false;
    if (event.target.files.length != 0) {
      this.importFile = event.target.files[0];
      this.fileName.setValue(event.target.files[0].name);
    } else {
      this.resetFile();
    }
  }

  resetFile(){
    this.importFile = undefined;
    this.importList = [];
    this.file.reset();
    this.fileName.setValue('');
    $('#file').val('');
  }

  openFileBrowser() {
    $('#file').click();
  }

  import() {
    this.serverService
      .importGrpcSetting(this.importSelected)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.checkImportStatus = true;
          // this.importList = res.RespBody;
          this.importList.forEach(item=> {
            res.RespBody.forEach(changeItem=>{
              if(item.uuid == changeItem.uuid){
                item.success = changeItem.success;
                item.errorMessage = changeItem.errorMessage;
              }
            })
          })
          this.importSelected = [];
          const code = [
            'button.import',
            'message.success',
            'message.fail',
          ];
          const dict = await this.toolService.getDict(code);
           if (res.RespBody.every((item) => item.success )) {
             this.messageService.add({
               severity: 'success',
               summary: `${dict['button.import']} ${dict['message.success']}`,
             });
           }
           else{
             this.messageService.add({
              severity: 'error',
              summary: `${dict['button.import']} ${dict['message.fail']}`,
            });
           }
           this.queryGrpcService();
        }
      });
  }

  public get gRPCProxyMapId() {
    return this.formEdit.get('gRPCProxyMapId')!;
  }
  public get serviceName() {
    return this.formEdit.get('serviceName')!;
  }
  public get proxyHostName() {
    return this.formEdit.get('proxyHostName')!;
  }
  public get targetHostName() {
    return this.formEdit.get('targetHostName')!;
  }
  public get targetPort() {
    return this.formEdit.get('targetPort')!;
  }
  public get connectTimeoutMs() {
    return this.formEdit.get('connectTimeoutMs')!;
  }
  public get sendTimeoutMs() {
    return this.formEdit.get('sendTimeoutMs')!;
  }
  public get readTimeoutMs() {
    return this.formEdit.get('readTimeoutMs')!;
  }
  public get enable() {
    return this.formEdit.get('enable')!;
  }
  public get secureMode() {
    return this.formEdit.get('secureMode')!;
  }
  public get trustedCertsContent() {
    return this.formEdit.get('trustedCertsContent')!;
  }
  public get file() {
    return this.formFile.get('file')!;
  }
  public get fileName() {
    return this.formFile.get('fileName')!;
  }
}
