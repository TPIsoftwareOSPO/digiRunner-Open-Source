import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  UntypedFormControl,
} from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { SharedPipesModule } from 'src/app/shared/pipes/shared-pipes.module';
import { TokenExpiredGuard } from 'src/app/shared';
import {
  DPB0304Resp,
  DPB0304RespItem,
} from 'src/app/models/api/ServerService/dpb0304.interface';
import { DbhaStatusTypePipe } from 'src/app/shared/pipes/dbhaStatusType.pipe';
import { TranslateService } from '@ngx-translate/core';
import { DPB0305Resp } from 'src/app/models/api/ServerService/dpb0305.interface';

@Component({
  selector: 'app-lb0015',
  templateUrl: './lb0015.component.html',
  styleUrls: ['./lb0015.component.css'],
  providers: [ConfirmationService, TokenExpiredGuard],
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule,
    DbhaStatusTypePipe,
  ],
  standalone: true,
})
export class Lb0015Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  // tableData: Array<DPB0285WebhookLogItem> = [];
  // statusList: { label: string; value: string }[] = [];
  // statusListIgnoreAll: { label: string; value: string }[] = [];
  // currentAction: string = '';
  // selectedItem?: DPB0285WebhookLogItem;

  tableData: DPB0304RespItem[] = [];
  detailData?: DPB0305Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private listService: ListService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private ngxSrvice: NgxUiLoaderService,
    private translateService: TranslateService,
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      keyword: new UntypedFormControl(''),
      status: new UntypedFormControl(''),
      syncType: new UntypedFormControl(''),
    });

    this.serverService.dbSyncHistory_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.dataList;
      }
    });
  }

  headerReturn() {
    this.changePage('query');
  }

  dbSyncHistory() {
    this.serverService
      .dbSyncHistory({
        keyword: this.keyword.value,
        status: this.status.value,
        syncType: this.syncType.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.dataList;
        } else this.tableData = [];
      });
  }

  changePage(action: string, rowData?: DPB0304RespItem) {
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'detail':
        this.serverService
          .dbSyncHistoryDetail({ syncId: rowData?.syncId || '' })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.detailData = res.RespBody;

              this.pageNum = 2;
              this.currentTitle = `${this.title} > ${this.translateService.instant('button.detail')}`;
            }
          });
        break;
      default:
        break;
    }
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }

  public get status() {
    return this.form.get('status')!;
  }
  public get syncType() {
    return this.form.get('syncType')!;
  }
}
