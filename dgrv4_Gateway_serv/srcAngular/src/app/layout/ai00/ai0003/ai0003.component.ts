import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { AiService } from 'src/app/shared/services/api-ai.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as dayjs from 'dayjs';
import { SortEvent } from 'primeng/api';
import { Table } from 'primeng/table';

@Component({
  selector: 'app-ai0003',
  templateUrl: './ai0003.component.html',
  styleUrls: ['./ai0003.component.css'],
  // providers: [ConfirmationService],
})
export class Ai0003Component extends BaseComponent implements OnInit {
  @ViewChild('dt') table!: Table;
  currentTitle = this.title;
  pageNum: number = 1;
  currentAction: string = '';

  tableData: { [key: string]: any }[] = [];
  totalElements: number = 0;
  currentPage: number = 0;
  sortField?: string | undefined;
  sortOrder?: number | undefined;

  lastSortEvent?: SortEvent;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private aiService: AiService,
    private toolService: ToolService // private fb: FormBuilder, // private messageService: MessageService, // private confirmationService: ConfirmationService, // private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);
  }

  listAIUsage() {
    this.currentPage = 0;
    this.aiService
      .listAIUsage({
        pagination: {
          pageNum: this.currentPage,
          pageSize: 20,
          sortBy: ['createDateTime'],
          sortOrder: 'desc',
        },
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.content;
        } else this.tableData = [];
        this.table.reset();
        this.lastSortEvent = undefined;
      });
  }

  ngOnInit(): void {
    this.currentPage = 0;
    this.aiService
      .listAIUsage_ignore1298({
        pagination: {
          pageNum: this.currentPage,
          pageSize: 20,
          sortBy: ['createDateTime'],
          sortOrder: 'desc',
        },
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.content;
          this.totalElements = res.RespBody.totalElements;
          this.currentPage = res.RespBody.number + 1;
        } else this.tableData = [];
      });
  }

  headerReturn() {
    this.changePage('query');
  }

  moreData() {
    this.aiService
      .listAIUsage({
        pagination: {
          pageNum: this.currentPage,
          pageSize: 20,
          sortBy: ['createDateTime'],
          sortOrder: 'desc',
        },
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = this.tableData.concat(res.RespBody.content);
          this.totalElements = res.RespBody.totalElements;
          this.currentPage = res.RespBody.number + 1;

          if (this.lastSortEvent) this.customSort(this.lastSortEvent);
        }
      });
  }

  async changePage(action: string, rowData?: any) {
    this.currentAction = action;
    const code = [
      // 'button.create',
      // 'button.update',
      // 'button.delete',
      'button.detail',
      // 'cfm_del',
    ];
    const dict = await this.toolService.getDict(code);
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      // case 'detail':
      //   this.currentTitle = `${this.title} > ${dict['button.detail']}`;
      //   this.aiService.getAIUsageDetail(rowData.id).subscribe(res=>{
      //     if (this.toolService.checkDpSuccess(res.ResHeader)) {

      //       this.detailData = res.RespBody;
      //       this.pageNum = 2;
      //     }
      //   })
      //   break;
    }
  }

  formateDate(date: Date) {
    if (!date) return '';
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'
      ? dayjs(date).format('YYYY-MM-DD HH:mm:ss')
      : '';
  }

  customSort(event: SortEvent) {
    this.lastSortEvent = event;
    const fieldName = event.field!;
    event.data!.sort((data1, data2) => {
      let value1: any;
      let value2: any;
      // 處理巢狀欄位狀況
      if (fieldName == 'dgrAiPromptTemplate.aiPromptTemplateName') {
        value1 = this.resolveFieldData(data1, fieldName);
        value2 = this.resolveFieldData(data2, fieldName);
      } else {
        value1 = data1[fieldName];
        value2 = data2[fieldName];
      }

      let result: number;

      // 僅當欄位名稱於陣列內時才轉為數字比較
      if (
        [
          'createDateTime',
          'outputTokenCount',
          'inputTokenCount',
          'requstTs',
        ].includes(fieldName)
      ) {
        const num1 = Number(value1);
        const num2 = Number(value2);
        result = num1 - num2;
      } else {
        //字串排序
        if (value1 == null && value2 != null) result = -1;
        else if (value1 != null && value2 == null) result = 1;
        else if (value1 == null && value2 == null) result = 0;
        else result = value1.toString().localeCompare(value2.toString());
      }

      return event.order! * result; // event.order: 1(升冪) 或 -1(降冪)
    });
  }

  resolveFieldData(data: any, field: string): any {
    if (!field) return null;
    if (field.indexOf('.') === -1) return data[field];
    return field.split('.').reduce((obj, key) => (obj ? obj[key] : null), data);
  }
}
