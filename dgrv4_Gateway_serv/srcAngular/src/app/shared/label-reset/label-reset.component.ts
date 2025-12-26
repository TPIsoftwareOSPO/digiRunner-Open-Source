import { Component, OnInit } from "@angular/core";
import { ToolService } from "../services/tool.service";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import * as dayjs from 'dayjs';
import { ServerService } from "../services/api-server.service";
import { ListService } from "../services/api-list.service";
import { ApiService } from "../services/api-api.service";
import { AA0423RespItem } from "src/app/models/api/ApiService/aa0423.interface";
import { FormBuilder, FormControl, FormGroup } from "@angular/forms";
import { ConfirmationService } from "primeng/api";

@Component({
  selector: 'app-label-reset',
  templateUrl: './label-reset.component.html',
  styleUrls: ['./label-reset.component.css'],
  providers: [ApiService, ConfirmationService],

})
export class LabelResetComponent implements OnInit {

  selected: Array<AA0423RespItem> = [];

  form!: FormGroup;
  showLabelList_tip: boolean = false;

  constructor(
    private apiService: ApiService,
    private toolService: ToolService,
    private serverService: ServerService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private fb: FormBuilder,
    private confirmationService: ConfirmationService,
  ) { }

  ngOnInit(): void {
    this.selected = this.config?.data?.data ? this.config?.data?.data : [];
    this.form = this.fb.group({
      labelList: new FormControl([]),

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

  cancelRole() {
    this.ref.close(null);
  }

  public get labelList() { return this.form.get('labelList')!; };

}
