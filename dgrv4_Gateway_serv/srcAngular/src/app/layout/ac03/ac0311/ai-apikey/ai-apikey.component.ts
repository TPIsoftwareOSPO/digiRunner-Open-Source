import { Component, OnInit } from '@angular/core';
import { DynamicDialogRef } from 'primeng/dynamicdialog';
import { DPB0255RespItem } from 'src/app/models/api/ServerService/dpb0255.interface';
import { AiService } from 'src/app/shared/services/api-ai.service';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-ai-apikey',
  templateUrl: './ai-apikey.component.html',
  styleUrls: ['./ai-apikey.component.css'],
})
export class AiApikeyComponent implements OnInit {
  cols: { field: string; header: string }[] = [];
  tableData: DPB0255RespItem[] = [];
  selected?:DPB0255RespItem;

  constructor(
    private aiService: AiService,
    private toolService: ToolService,
    private ref: DynamicDialogRef
  ) {}

  async ngOnInit() {
    const codes = [
      // 'status',
      'ai.api_key_alias',
      'ai.provider',
      'ai.model',
      'ai.usage_limit',
    ];
    const dict = await this.toolService.getDict(codes);
    this.cols = [
      // { field: 'aiApiKeyEnable', header: dict['status'] },
      { field: 'id', header: 'ID' },
      { field: 'aiApikeyName', header: dict['ai.api_key_alias'] },
      { field: 'aiProviderName', header: dict['ai.provider'] },
      { field: 'aiModel', header: dict['ai.model'] },
      { field: 'usageLimitPolicy', header: dict['ai.usage_limit'] },
    ];

    this.aiService.listAIAPIKEY_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content.filter(
          (rowData) => rowData.aiApikeyEnable == 'Y'
        );
      }
    });
  }

  confirmProc() {
    this.ref.close(this.selected);
  }

  cancelProc() {
    this.ref.close(null);
  }
}
