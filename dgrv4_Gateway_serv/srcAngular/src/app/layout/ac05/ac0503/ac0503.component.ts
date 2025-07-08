import { ApiService } from 'src/app/shared/services/api-api.service';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { ReportService } from 'src/app/shared/services/api-report.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import * as dayjs from 'dayjs';
// import * as chartjs from 'chart.js';
import { Chart } from 'chart.js/auto';
import { AA1205Req } from 'src/app/models/api/ReportService/aa1205.interface';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AlertType } from 'src/app/models/common.enum';
import { AA1213RespItem } from 'src/app/models/api/ReportService/aa1213.interface';

@Component({
  selector: 'app-ac0503',
  templateUrl: './ac0503.component.html',
  styleUrls: ['./ac0503.component.css'],
  providers: [ApiService],
})
export class Ac0503Component extends BaseComponent implements OnInit {
  form!: FormGroup;
  tableData: AA1213RespItem[] = [];

  constructor(
    router: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private reportService: ReportService
  ) {
    super(router, tr);
    this.form = this.fb.group({
      rptMode: new FormControl('group'),
      abnormalElapsedTime: new FormControl(30000),
    });
  }

  ngOnInit() {
    this.reportService
      .queryApiAbnormal_ignore1298({
        queryItem: this.rptMode.value === 'all' ? 2 : 1,
        abnormalElapsedTime: this.abnormalElapsedTime.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.dataList;
          if (this.tableData.length > 0) {
            this.tableData = this.tableData.map((item) => {
              const cleanedItem = { ...item };
              if (cleanedItem.uri)
                cleanedItem.uri = cleanedItem.uri.replace(/\n/g, '');
              return cleanedItem;
            });
          }
        }
      });

    this.form.get('abnormalElapsedTime')!.valueChanges.subscribe((value) => {
      const min = -864000000;
      const max = 864000000;

      if (value != null) {
        let fixedValue = value;

        if (value < min) fixedValue = min;
        if (value > max) fixedValue = max;

        if (fixedValue !== value) {
          this.form
            .get('abnormalElapsedTime')!
            .setValue(fixedValue, { emitEvent: false });
        }
      }
    });
  }

  queryApiAbnormal() {
    this.reportService
      .queryApiAbnormal({
        queryItem: this.rptMode.value === 'all' ? 2 : 1,
        abnormalElapsedTime: this.abnormalElapsedTime.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.dataList;
          if (this.tableData.length > 0) {
            this.tableData = this.tableData.map((item) => {
              const cleanedItem = { ...item };
              if (cleanedItem.uri)
                cleanedItem.uri = cleanedItem.uri.replace(/\n/g, '');
              return cleanedItem;
            });
          }
        } else this.tableData = [];
      });
  }

  numberComma(tar?: any) {
    return tar ? this.toolService.numberComma(tar) : tar;
  }

  onInputChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    if (value > 864000000) input.value = '864000000';
    if (value < -864000000) input.value = '-864000000';
  }

  public get abnormalElapsedTime() {
    return this.form.get('abnormalElapsedTime')!;
  }

  public get rptMode() {
    return this.form.get('rptMode')!;
  }
}
