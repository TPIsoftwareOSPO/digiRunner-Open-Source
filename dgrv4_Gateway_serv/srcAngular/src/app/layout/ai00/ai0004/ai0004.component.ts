import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { AiService } from 'src/app/shared/services/api-ai.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB0262RespItem } from 'src/app/models/api/ServerService/dpb0262.interface';
import { ConfirmationService, MessageService } from 'primeng/api';
import { DPB0263Req } from 'src/app/models/api/ServerService/dpb0263.interface';
import { DPB0264Resp } from 'src/app/models/api/ServerService/dpb0264.interface';
import { DPB0265Req } from 'src/app/models/api/ServerService/dpb0265.interface';
import * as ValidatorFns from '../../../shared/validator-functions';

@Component({
  selector: 'app-ai0004',
  templateUrl: './ai0004.component.html',
  styleUrls: ['./ai0004.component.css'],
  providers: [ConfirmationService],
})
export class Ai0004Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  currentAction: string = '';
  form!: FormGroup;
  formE!: FormGroup;
  tableData: DPB0262RespItem[] = [];
  btnName: string = '';

  aiTempletePrompt?: DPB0264Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private aiService: AiService,
    private toolService: ToolService,
    private confirmationService: ConfirmationService,
    private fb: FormBuilder,
    private messageService: MessageService // private confirmationService: ConfirmationService, // private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);
    this.form = this.fb.group({
      aiPromptTemplateName: new FormControl(''),
      aiPromptTemplateEnable: new FormControl(''),
    });

    this.formE = this.fb.group({
      aiPromptTemplateName: new FormControl(''),
      aiPromptTemplateEnable: new FormControl(''),
      aiPromptTemplateContent: new FormControl(''),
      aiPromptTemplateRemark: new FormControl(''),
    });
  }

  ngOnInit() {
    this.aiService.listAIPromptTemplate({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
      }
    });
  }

  listAIPromptTemplate() {
    this.aiService
      .listAIPromptTemplate({
        aiPromptTemplateEnable:
          this.aiPromptTemplateEnable.value == ''
            ? undefined
            : this.aiPromptTemplateEnable.value,
        aiPromptTemplateName:
          this.aiPromptTemplateName.value == ''
            ? undefined
            : this.aiPromptTemplateName.value,
      })
      .subscribe((res) => {
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
      this.aiPromptTemplateNameE.setValidators([ValidatorFns.requiredValidator()]);
      this.aiPromptTemplateNameE.reset('');
      this.aiPromptTemplateEnableE.setValidators([ValidatorFns.requiredValidator()]);
      this.aiPromptTemplateEnableE.reset('');
      this.aiPromptTemplateContentE.setValidators([ValidatorFns.requiredValidator()]);
      this.aiPromptTemplateContentE.reset('');
      this.aiPromptTemplateRemarkE.reset('');
      this.formE.updateValueAndValidity();
  }

  async changePage(action: string, rowData?: DPB0262RespItem) {
    this.currentAction = action;
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
    ];
    const dict = await this.toolService.getDict(code);
    // let tmpEnable = this.aiPromptTemplateEnable.value;
    // this.resetFormValidator(this.formE);
    // this.aiPromptTemplateEnable.setValue(tmpEnable);
    // this.formE.enable();
    this.iniFormRequired();

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.btnName = dict['button.create'];
        this.aiPromptTemplateEnableE.setValue('N');
        this.pageNum = 2;
        break;
      case 'update':
        this.aiService.getAIPromptTemplate(rowData!.id).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.aiTempletePrompt = res.RespBody;
            this.aiPromptTemplateNameE.setValue(
              res.RespBody.aiPromptTemplateName
            );
            this.aiPromptTemplateEnableE.setValue(
              res.RespBody.aiPromptTemplateEnable
            );
            this.aiPromptTemplateContentE.setValue(
              res.RespBody.aiPromptTemplateContent
            );
            this.aiPromptTemplateRemarkE.setValue(
              res.RespBody.aiPromptTemplateRemark
            );
            this.currentTitle = `${this.title} > ${dict['button.update']}`;
            this.btnName = dict['button.update'];
            this.pageNum = 2;
          }
        });
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${rowData?.id}`,

          accept: () => {
            this.aiService
              .deleteAIPromptTemplate(rowData!.id)
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
                  this.listAIPromptTemplate();
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
          aiPromptTemplateName: this.aiPromptTemplateNameE.value,
          aiPromptTemplateContent: this.aiPromptTemplateContentE.value,
          aiPromptTemplateEnable: this.aiPromptTemplateEnableE.value,
          aiPromptTemplateRemark: this.aiPromptTemplateRemarkE.value,
        } as DPB0263Req;
        this.aiService.createAIPromptTemplate(reqC).subscribe(async (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const code = ['message.create', 'message.success'];
            const dict = await this.toolService.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']} `,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.listAIPromptTemplate();
          }
        });
        break;
      case 'update':
        let reqU = {
          aiPromptTemplateName: this.aiPromptTemplateNameE.value,
          aiPromptTemplateContent: this.aiPromptTemplateContentE.value,
          aiPromptTemplateEnable: this.aiPromptTemplateEnableE.value,
          aiPromptTemplateRemark: this.aiPromptTemplateRemarkE.value,
        } as DPB0265Req;
        this.aiService
          .updateAIPromptTemplate(this.aiTempletePrompt!.id, reqU)
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
              this.listAIPromptTemplate();
            }
          });
        break;
    }
  }

  public get aiPromptTemplateName() {
    return this.form.get('aiPromptTemplateName')!;
  }

  public get aiPromptTemplateEnable() {
    return this.form.get('aiPromptTemplateEnable')!;
  }

  public get aiPromptTemplateNameE() {
    return this.formE.get('aiPromptTemplateName')!;
  }

  public get aiPromptTemplateContentE() {
    return this.formE.get('aiPromptTemplateContent')!;
  }

  public get aiPromptTemplateEnableE() {
    return this.formE.get('aiPromptTemplateEnable')!;
  }

  public get aiPromptTemplateRemarkE() {
    return this.formE.get('aiPromptTemplateRemark')!;
  }
}
