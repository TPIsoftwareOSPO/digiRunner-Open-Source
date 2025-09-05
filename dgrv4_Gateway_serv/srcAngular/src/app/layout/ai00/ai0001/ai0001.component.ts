import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { AiService } from 'src/app/shared/services/api-ai.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { contentItem } from 'src/app/models/api/ServerService/dpb0250.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0251Req } from 'src/app/models/api/ServerService/dpb0251.interface';
import { DPB0252Resp } from 'src/app/models/api/ServerService/dpb0252.interface';
import { DPB0253Req } from 'src/app/models/api/ServerService/dpb0253.interface';
import * as ValidatorFns from '../../../shared/validator-functions';

@Component({
  selector: 'app-ai0001',
  templateUrl: './ai0001.component.html',
  styleUrls: ['./ai0001.component.css'],
  providers: [ConfirmationService],
})
export class Ai0001Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  currentAction: string = '';
  cols: { field: string; header: string }[] = [];
  tableData: Array<contentItem> = [];
  formE!: FormGroup;
  btnName: string = '';

  aiProviderDetail?: DPB0252Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private aiService: AiService,
    private toolService: ToolService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);

    this.formE = this.fb.group({
      aiProviderAlias: new FormControl(''),
      aiProviderName: new FormControl(''),
      aiModel: new FormControl(''),
      generateAPI: new FormControl(''),
      countTokenAPI: new FormControl(''),
      aiProviderEnabled: new FormControl(''),
    });
  }

  async ngOnInit() {
    const codes = [
      'status',
      'name',
      'ai.provider',
      'ai.llm_name',
      'ai.apikey_count',
    ];
    const dict = await this.toolService.getDict(codes);
    this.cols = [
      { field: 'aiProviderEnable', header: dict['status'] },
      { field: 'aiProviderId', header: 'ID' },
      { field: 'aiProviderAlias', header: dict['name'] },
      { field: 'aiProviderName', header: dict['ai.provider'] },
      { field: 'aiModel', header: dict['ai.llm_name'] },
      { field: 'aiApiKeysCount', header: dict['ai.apikey_count'] },
    ];

    this.aiService.listAIProvider_ignore1298().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
      } else this.tableData = [];
    });
  }

  listAiProvider() {
    this.aiService.listAIProvider().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
      } else this.tableData = [];
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  initRequiredForm() {
    this.aiProviderName.setValidators([ValidatorFns.requiredValidator()]);
    this.aiProviderName.reset('Composer');
    this.aiProviderAlias.setValidators([ValidatorFns.requiredValidator()]);
    this.aiProviderAlias.reset('');
    this.aiModel.setValidators([ValidatorFns.requiredValidator()]);
    this.aiModel.reset('');
    this.generateAPI.setValidators([ValidatorFns.requiredValidator()]);
    this.generateAPI.reset('');
    // this.countTokenAPI.setValidators([ValidatorFns.requiredValidator()]);
    this.countTokenAPI.reset('');
    this.aiProviderEnabled.setValidators([ValidatorFns.requiredValidator()]);
    this.aiProviderEnabled.reset('');
    this.formE.updateValueAndValidity();
  }

  async changePage(action: string, rowData?: contentItem) {
    this.currentAction = action;
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
    ];
    const dict = await this.toolService.getDict(code);
    // this.resetFormValidator(this.formE);
    this.initRequiredForm();
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.btnName = dict['button.create'];

        this.aiProviderEnabled.setValue('N');
        this.aiProviderName.disable();
        this.pageNum = 2;
        break;
      case 'update':
        this.aiService.getAIProvider(rowData!.aiProviderId).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            // console.log(res.RespBody);
            this.aiProviderAlias.setValue(res.RespBody.aiProviderAlias);
            this.aiModel.setValue(res.RespBody.aiModel);
            this.aiProviderEnabled.setValue(res.RespBody.aiProviderEnable);
            this.generateAPI.setValue(res.RespBody.generateApi);
            this.countTokenAPI.setValue(res.RespBody.countTokenApi);
            this.aiProviderName.setValue(res.RespBody.aiProviderName);
            this.aiProviderName.disable();
            this.aiProviderDetail = res.RespBody;
            this.currentTitle = `${this.title} > ${dict['button.update']}`;
            this.btnName = dict['button.update'];
            this.pageNum = 2;
          }
        });
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${rowData?.aiProviderAlias}`,

          accept: () => {
            this.aiService
              .deleteAIProvider(rowData!.aiProviderId)
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  const code = ['message.delete', 'message.success'];
                  const dict = await this.toolService.getDict(code);
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']}`,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.changePage('query');
                  this.listAiProvider();
                }
              });
          },
        });
        break;
      default:
        break;
    }
  }

  procData() {
    switch (this.currentAction) {
      case 'create':
        let req = {
          aiProviderAlias: this.aiProviderAlias.value,
          aiProviderName: this.aiProviderName.value,
          aiModel: this.aiModel.value,
          generateAPI: this.generateAPI.value,
          countTokenAPI: this.countTokenAPI.value,
          aiProviderEnabled: this.aiProviderEnabled.value,
        } as DPB0251Req;
        this.aiService.createAIProvider(req).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.create', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']} `,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.listAiProvider();
          }
        });
        break;
      case 'update':
        let reqU = {
          aiProviderAlias: this.aiProviderAlias.value,
          aiProviderName: this.aiProviderName.value,
          aiModel: this.aiModel.value,
          generateAPI: this.generateAPI.value,
          countTokenAPI: this.countTokenAPI.value,
          aiProviderEnabled: this.aiProviderEnabled.value,
        } as DPB0253Req;
        this.aiService
          .updateAIProvider(this.aiProviderDetail!.aiProviderId, reqU)
          .subscribe(async (res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              const code = ['message.update', 'message.success'];
              const dict = await this.toolService.getDict(code);
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']} `,
                detail: `${dict['message.update']} ${dict['message.success']}!`,
              });
              this.changePage('query');
              this.listAiProvider();
            }
          });
        break;
    }
  }

  public get aiProviderAlias() {
    return this.formE.get('aiProviderAlias')!;
  }
  public get aiProviderName() {
    return this.formE.get('aiProviderName')!;
  }
  public get aiModel() {
    return this.formE.get('aiModel')!;
  }
  public get generateAPI() {
    return this.formE.get('generateAPI')!;
  }
  public get countTokenAPI() {
    return this.formE.get('countTokenAPI')!;
  }
  public get aiProviderEnabled() {
    return this.formE.get('aiProviderEnabled')!;
  }
}
