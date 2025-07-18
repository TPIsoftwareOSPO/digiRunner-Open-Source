import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import * as dayjs from 'dayjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DialogService } from 'primeng/dynamicdialog';
import { FileService } from 'src/app/shared/services/api-file.service';
import {
  AA1121Resp,
  RequestParam,
} from 'src/app/models/api/FileService/aa1121.interface';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as FileSaver from 'file-saver';

@Component({
  selector: 'app-ac0322',
  templateUrl: './ac0322.component.html',
  styleUrls: ['./ac0322.component.css'],
  providers: [MessageService, ConfirmationService],
})
export class Ac0322Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;

  _file?: File;

  checkLackApi: boolean = false;
  relateData?: AA1121Resp;

  checkCAflag: boolean = false;
  imported: boolean = false;

  field: string[] = [
    'API_NAME',
    'MODULE_NAME',
    'API_ID',
    'API_STATUS',
    'API_SRC',
    'API_CACHE',
    'HTTP_METHODS',
    'NO_AUTH',
    'ORG_ID',
    'SRC_URL',
    'API_DESC',
    'JWT_LABEL',
    'ENABLE_SCHEDULED_DATE',
    'DISABLE_SCHEDULED_DATE',
    'CREATE_USER',
    'UPDATE_USER',
    'CREATE_TIME',
    'UPDATE_TIME',
  ];

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private openApiService: OpenApiKeyService,
    private messageService: MessageService,
    private dialogService: DialogService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private fileService: FileService,
    private api: ApiBaseService,
    private ngxService: NgxUiLoaderService
  ) {
    super(route, tr);
  }

  async ngOnInit() {}

  headerReturn() {}

  apiListExport() {
    this.ngxService.start();
    this.serverService.apiListExport({type:"EXCEL", field: this.field}).subscribe((res: any) => {
      if (res.type === 'application/json') {
        const reader = new FileReader();
        reader.onload = () => {
          const jsonData = JSON.parse(reader.result as string);
          this.alertService.ok(
            jsonData.ResHeader.rtnMsg,
            '',
            AlertType.warning,
            jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID
          );
        };
        reader.readAsText(res);
      } else {
        const data: Blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
        });

        const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
        FileSaver.saveAs(data, `API_LIST_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  openFileBrowser() {
    $('#file').click();
  }

  changeFile(event) {
    if (event.target.files.length != 0) {
      this._file = event.target.files[0];
    } else {
      this._file = undefined;
    }
  }
}
