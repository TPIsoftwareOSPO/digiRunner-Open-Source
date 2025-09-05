import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { AiService } from 'src/app/shared/services/api-ai.service';
import { filter, map, tap } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB02671RespItem } from 'src/app/models/api/ServerService/dpb02671.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0262RespItem } from 'src/app/models/api/ServerService/dpb0262.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { DPB0267Req } from 'src/app/models/api/ServerService/dpb0267.interface';

@Component({
  selector: 'app-ai0005',
  templateUrl: './ai0005.component.html',
  styleUrls: ['./ai0005.component.css'],
  providers: [ConfirmationService],
})
export class Ai0005Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  currentAction: string = '';
  tableData: DPB02671RespItem[] = [];
  formE!: FormGroup;
  btnName: string = '';
  aIPromptTemplateData: { label: string; value: string }[] = [];

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
      aiApiKeyConsumerType: new FormControl(''),
      aiApiKeyConsumerId: new FormControl(''),
      aiPromptTemplateId: new FormControl(''),
    });
  }

  ngOnInit(): void {
    this.listAIPromptTemplateEnable();
    this.listConsumerAIPromptTemplateSetting();
  }

  listConsumerAIPromptTemplateSetting() {
    this.aiService.listConsumerAIPromptTemplateSetting({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
      } else this.tableData = [];
    });
  }

  listAIPromptTemplateEnable() {
    this.aiService
      .listAIPromptTemplate_ignore1298({})
      .pipe(
        filter((res) => this.toolService.checkDpSuccess(res.ResHeader)),
        map((res) => res.RespBody?.content || []),
        map((content) =>
          content
            .filter((row) => row.aiPromptTemplateEnable === 'Y') // 過濾啟用項目
            .map((row) => ({
              label: row.aiPromptTemplateName,
              value: row.id,
            }))
        )
      )
      .subscribe((res) => {
        this.aIPromptTemplateData = res;
        // console.log(res);
      });
  }

  headerReturn() {
    this.changePage('query');
  }

  iniFormRequired() {
    this.formE.enable();
    this.aiApiKeyConsumerType.setValidators([ValidatorFns.requiredValidator()]);
    this.aiApiKeyConsumerType.reset('user');
    this.aiApiKeyConsumerId.setValidators([ValidatorFns.requiredValidator()]);
    this.aiApiKeyConsumerId.reset('');
    this.aiPromptTemplateId.setValidators([ValidatorFns.requiredValidator()]);
    this.aiPromptTemplateId.reset('');
    this.formE.updateValueAndValidity();
  }

  async changePage(action: string, rowData?: DPB02671RespItem) {
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

        this.pageNum = 2;
        break;
      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.btnName = dict['button.update'];
        this.aiApiKeyConsumerType.setValue(rowData!.aiApikeyConsumerType)
        this.aiApiKeyConsumerType.disable();
        this.aiApiKeyConsumerId.setValue(rowData!.aiApikeyConsumerId)
        this.aiApiKeyConsumerId.disable();
        this.aiPromptTemplateId.setValue(rowData!.dgrAiPromptTemplate.id)
        this.pageNum = 2;
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `Id: ${rowData?.id}`,

          accept: () => {
            this.aiService
              .deleteConsumerAIPromptTemplateSetting(rowData!.id)
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
                  this.listConsumerAIPromptTemplateSetting();
                }
              });
          },
        });
        break;
    }
  }

  procData() {
    let req = {
      aiApiKeyConsumerType: this.aiApiKeyConsumerType.value,
      aiApiKeyConsumerId: this.aiApiKeyConsumerId.value,
      aiPromptTemplateId: this.aiPromptTemplateId.value,
    } as DPB0267Req;

    this.aiService
      .updateConsumerAIPromptTemplateSetting(req)
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
          this.listConsumerAIPromptTemplateSetting();
        }
      });
  }

  public get aiApiKeyConsumerType() {
    return this.formE.get('aiApiKeyConsumerType')!;
  }

  public get aiApiKeyConsumerId() {
    return this.formE.get('aiApiKeyConsumerId')!;
  }

  public get aiPromptTemplateId() {
    return this.formE.get('aiPromptTemplateId')!;
  }
}
