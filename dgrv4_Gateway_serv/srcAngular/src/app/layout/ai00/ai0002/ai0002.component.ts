import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { AiService } from 'src/app/shared/services/api-ai.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB0256Req } from 'src/app/models/api/ServerService/dpb0256.interface';
import { ConfirmationService, MessageService } from 'primeng/api';
import { DPB0255RespItem } from 'src/app/models/api/ServerService/dpb0255.interface';
import { DPB0257Resp } from 'src/app/models/api/ServerService/dpb0257.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as ValidatorFns from '../../../shared/validator-functions';
import { filter, map } from 'rxjs';

@Component({
  selector: 'app-ai0002',
  templateUrl: './ai0002.component.html',
  styleUrls: ['./ai0002.component.css'],
  providers: [ConfirmationService],
})
export class Ai0002Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  currentAction: string = '';
  cols: { field: string; header: string }[] = [];
  tableData: DPB0255RespItem[] = [];
  // aiProviderList: { label: string; value: string }[] = [];
  aiProviderList: { [key:string]:any }[] = [];
  apikeyDetail?: DPB0257Resp;

  formE!: FormGroup;
  btnName: string = '';

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
      aiApiKeyName: new FormControl(''),
      aiProviderId: new FormControl(''),
      aiProviderAlias: new FormControl(''),
      aiApiKeyCode: new FormControl(''),
      usageLimitInputToken: new FormControl(''),
      usageLimitOutputToken: new FormControl(''),
      usageLimitPolicy: new FormControl(''),
      backupKeyId: new FormControl(''),
      aiApiKeyEnable: new FormControl(''),
    });
  }

  async ngOnInit() {
    const codes = [
      'status',
      'ai.api_key_alias',
      'ai.provider',
      'ai.model',
      'ai.usage_limit',
    ];
    const dict = await this.toolService.getDict(codes);
    this.cols = [
      { field: 'aiApiKeyEnable', header: dict['status'] },
      { field: 'id', header: 'ID' },
      { field: 'aiApikeyName', header: dict['ai.api_key_alias'] },
      { field: 'aiProviderName', header: dict['ai.provider'] },
      { field: 'aiModel', header: dict['ai.model'] },
      { field: 'usageLimitPolicy', header: dict['ai.usage_limit'] },
    ];

    this.aiService.listAIAPIKEY_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
      }
    });

    this.aiService
      .listAIProvider()
      .pipe(
        filter((res) => this.toolService.checkDpSuccess(res.ResHeader)),
        map((res) => res.RespBody?.content || []),
        map((content) =>
          content
            .filter((row) => row.aiProviderEnable === 'Y') // 過濾啟用項目
            // .map((row) => ({
            //   label: `${row.aiProviderAlias}(${row.aiProviderName}-${row.aiModel})`,
            //   value: row.aiProviderId,
            // }))
        )
      )
      .subscribe((filterData) => {        
        this.aiProviderList = filterData;
      });
  }

  listAIAPIKEY() {
    this.aiService.listAIAPIKEY({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
      } else this.tableData = [];
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  iniFormRequired() {
    this.formE.enable();
    this.aiApiKeyName.setValidators([ValidatorFns.requiredValidator()]);
    this.aiApiKeyName.reset('');
    this.aiProviderId.setValidators([ValidatorFns.requiredValidator()]);
    this.aiProviderId.reset('');
    this.aiApiKeyCode.setValidators([ValidatorFns.requiredValidator()]);
    this.aiApiKeyCode.reset('');
    this.usageLimitInputToken.setValidators([ValidatorFns.requiredValidator()]);
    this.usageLimitInputToken.reset('');
    this.usageLimitOutputToken.setValidators([
      ValidatorFns.requiredValidator(),
    ]);
    this.usageLimitOutputToken.reset('');
    this.usageLimitPolicy.setValidators([ValidatorFns.requiredValidator()]);
    this.usageLimitPolicy.reset('');
    this.aiApiKeyEnable.setValidators([ValidatorFns.requiredValidator()]);
    this.aiApiKeyEnable.reset('');
    this.formE.updateValueAndValidity();
  }

  async changePage(action: string, rowData?: DPB0255RespItem) {
    this.currentAction = action;
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
    ];
    const dict = await this.toolService.getDict(code);

    this.iniFormRequired();

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.btnName = dict['button.create'];

        this.usageLimitInputToken.setValue(0);
        this.usageLimitOutputToken.setValue(0);
        this.aiApiKeyEnable.setValue('N');
        this.usageLimitPolicy.setValue('REJECT');
        this.pageNum = 2;

        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;

        this.aiService.getAIAPIKEY(rowData!.id).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.formE.disable();
            this.aiApiKeyName.setValue(res.RespBody.aiApikeyName);
            this.aiProviderId.setValue(res.RespBody.dgrAiProvider.aiProviderId);
            this.aiProviderAlias.setValue(
              res.RespBody.dgrAiProvider.aiProviderAlias
            );
            this.aiApiKeyEnable.setValue(res.RespBody.aiApikeyEnable);
            this.aiApiKeyCode.setValue(res.RespBody.aiApikeyCode);
            this.usageLimitInputToken.setValue(
              res.RespBody.usageLimitInputToken
            );
            this.usageLimitOutputToken.setValue(
              res.RespBody.usageLimitOutputToken
            );
            this.usageLimitPolicy.setValue(res.RespBody.usageLimitPolicy);
            this.pageNum = 2;
          }
        });
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.btnName = dict['button.update'];

        this.aiService.getAIAPIKEY(rowData!.id).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.apikeyDetail = res.RespBody;
            this.aiApiKeyName.setValue(res.RespBody.aiApikeyName);
            this.aiProviderId.setValue(res.RespBody.dgrAiProvider.aiProviderId);
            this.aiApiKeyEnable.setValue(res.RespBody.aiApikeyEnable);
            this.aiApiKeyCode.setValue(res.RespBody.aiApikeyCode);
            this.usageLimitInputToken.setValue(
              res.RespBody.usageLimitInputToken
            );
            this.usageLimitOutputToken.setValue(
              res.RespBody.usageLimitOutputToken
            );
            this.usageLimitPolicy.setValue(res.RespBody.usageLimitPolicy);
            this.pageNum = 2;
          }
        });

        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `Id: ${rowData?.id}`,

          accept: () => {
            this.aiService
              .deleteAIAPIKEY(rowData!.id)
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
                  this.listAIAPIKEY();
                }
              });
          },
        });
        break;
    }
  }

  procData() {
    switch (this.currentAction) {
      case 'create':
        let reqC = {
          aiApiKeyName: this.aiApiKeyName.value,
          aiProviderId: this.aiProviderId.value,
          aiApiKeyCode: this.aiApiKeyCode.value,
          usageLimitInputToken: this.usageLimitInputToken.value,
          usageLimitOutputToken: this.usageLimitOutputToken.value,
          usageLimitPolicy: this.usageLimitPolicy.value,
          aiApiKeyEnable: this.aiApiKeyEnable.value,
        } as DPB0256Req;
        this.aiService.registerAIAPIKEY(reqC).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.create', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']} `,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.listAIAPIKEY();
          }
        });
        break;
      case 'update':
        let reqU = {
          aiApiKeyName: this.aiApiKeyName.value,
          aiProviderId: this.aiProviderId.value,
          aiApiKeyCode: this.aiApiKeyCode.value,
          usageLimitInputToken: this.usageLimitInputToken.value,
          usageLimitOutputToken: this.usageLimitOutputToken.value,
          usageLimitPolicy: this.usageLimitPolicy.value,
          aiApiKeyEnable: this.aiApiKeyEnable.value,
        } as DPB0256Req;
        this.aiService
          .updateAIAPIKEY(this.apikeyDetail!.id, reqU)
          .subscribe(async (res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              const code = ['message.create', 'message.success'];
              const dict = await this.toolService.getDict(code);
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.create']} `,
                detail: `${dict['message.create']} ${dict['message.success']}!`,
              });
              this.changePage('query');
              this.listAIAPIKEY();
            }
          });
        break;
    }
  }

  public get aiApiKeyName() {
    return this.formE.get('aiApiKeyName')!;
  }
  public get aiProviderId() {
    return this.formE.get('aiProviderId')!;
  }
  public get aiProviderAlias() {
    return this.formE.get('aiProviderAlias')!;
  }
  public get aiApiKeyCode() {
    return this.formE.get('aiApiKeyCode')!;
  }
  public get usageLimitInputToken() {
    return this.formE.get('usageLimitInputToken')!;
  }
  public get usageLimitOutputToken() {
    return this.formE.get('usageLimitOutputToken')!;
  }
  public get usageLimitPolicy() {
    return this.formE.get('usageLimitPolicy')!;
  }
  public get backupKeyId() {
    return this.formE.get('backupKeyId')!;
  }
  public get aiApiKeyEnable() {
    return this.formE.get('aiApiKeyEnable')!;
  }
}
