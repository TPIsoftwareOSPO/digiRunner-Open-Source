import { Component, OnInit } from "@angular/core";
import { ToolService } from "../services/tool.service";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import * as dayjs from 'dayjs';
import { ServerService } from "../services/api-server.service";
import { ListService } from "../services/api-list.service";
import { ApiService } from "../services/api-api.service";
import { AA0423RespItem } from "src/app/models/api/ApiService/aa0423.interface";
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup } from "@angular/forms";
import { ConfirmationService, MessageService } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";

@Component({
    selector: 'app-label-reset',
    templateUrl: './label-reset.component.html',
    styleUrls: ['./label-reset.component.css'],
    providers: [ApiService, ConfirmationService],
    standalone: false
})
export class LabelResetComponent implements OnInit {

  selected: Array<AA0423RespItem> = [];

  form!: UntypedFormGroup;
  showLabelList_tip: boolean = false;

  constructor(
    private apiService: ApiService,
    private toolService: ToolService,
    private serverService: ServerService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private fb: UntypedFormBuilder,
    private confirmationService: ConfirmationService,
    private messageService:MessageService,
    private translate: TranslateService
  ) { }

  ngOnInit(): void {
    this.selected = this.config?.data?.data ? this.config?.data?.data : [];
    this.form = this.fb.group({
      labelList: new UntypedFormControl([]),

    })

    this.labelList.valueChanges.subscribe(res => {
      this.labelList.setValue(Array.isArray(res) ? res.map(item => item.toLowerCase()) : [], { emitEvent: false })
    })

  }

  async checkChips(evt) {
    if (evt.value.length > 20) {
      this.labelList.value.pop();
      this.showLabelList_tip = true;
    }
    else {
      this.showLabelList_tip = false;
    }
  }

  async chooseRole() {
    const codes = ['cfm_reset_label'];
    const dict = await this.toolService.getDict(codes);
    this.confirmationService.confirm({
      header: ' ',
      message: `${dict['cfm_reset_label']}`,
      accept: () => {

        this.ref.close(this.labelList.value);

      }
    });

  }

  checkLength(event: KeyboardEvent) {

    const maxLimit = 5;
    const currentValues = this.form.get('labelList')?.value || [];
    // this.showLabelList_tip = false;

    if (event.key === 'Enter') {
      const duplicates = currentValues.filter((item, index) => {
        return currentValues.indexOf(item) !== index;
      });

      if(duplicates.length > 0){
        currentValues.pop();
        this.form.get('labelList')?.setValue(currentValues);
      }

      if (currentValues.length > maxLimit) {

        event.preventDefault();

        // this.showLabelList_tip = true;
        this.messageService.add({
          severity: 'warn',
          // summary: '上限提醒',
          detail: this.translate.instant('label_tag_max', { value: 5 }),
        });
        currentValues.pop();
        this.form.get('labelList')?.setValue(currentValues);
      }
    }
  }

  cancelRole() {
    this.ref.close(null);
  }

  public get labelList() { return this.form.get('labelList')!; };

}
