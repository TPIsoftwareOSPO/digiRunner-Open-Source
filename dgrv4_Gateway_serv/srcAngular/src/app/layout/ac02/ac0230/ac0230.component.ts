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
import { TxID } from 'src/app/models/common.enum';
import { NgxUiLoaderService } from 'ngx-ui-loader';


@Component({
  selector: 'app-ac0230',
  templateUrl: './ac0230.component.html',
  styleUrls: ['./ac0230.component.css'],
  providers: [MessageService, ConfirmationService],
})
export class Ac0230Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;

  jsonFile?: File;

  checkLackApi: boolean = false;
  relateData?: AA1121Resp;

  checkCAflag: boolean = false;
  imported:boolean = false;

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
    private ngxService: NgxUiLoaderService,

  ) {
    super(route, tr);
  }

  async ngOnInit() {
  }

  headerReturn() {}

  epxortClientRelatedClick() {
    this.fileService.exportClientRelated().subscribe((res:any) => {
      let dateStr = this.toolService.setformate(
        new Date(),
        'YYYY-MM-DD_HH-mm-ss'
      );
      let fileName = `client_related_data_${dateStr}.json`;
      // let JSONString = JSON.stringify(res);
      // let blob = new Blob(res);
      let file = new File([res], fileName);
      const url = window.URL.createObjectURL(file);
      const a = document.createElement('a');
      document.body.appendChild(a);
      a.setAttribute('style', 'display: none');
      a.href = url;
      a.download = fileName;
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove();
    });
  }

  openFileBrowser() {
    $('#file').click();
  }

  changeFile(event) {
    if (event.target.files.length != 0) {
      this.jsonFile = event.target.files[0];
    } else {
      this.jsonFile = undefined;
    }
  }

  // async dataStatusValueTranslate() {
  //   const code = [
  //     'client_related.A',
  //     'client_related.R',
  //     'client_related.C',
  //     'client_related.CA',
  //     'client_related.CR',
  //   ];
  //   const dict = await this.toolService.getDict(code);
  //   if (this.relateData?.clientList)
  //     this.relateData.clientList = this.relateData?.clientList.map((row) => {
  //       return {
  //         dataStatusName: dict[`client_related.${row.dataStatus}`],
  //         ...row,
  //       };
  //     });
  //   if (this.relateData?.groupList)
  //     this.relateData.groupList = this.relateData?.groupList.map((row) => {
  //       return {
  //         dataStatusName: dict[`client_related.${row.dataStatus}`],
  //         ...row,
  //       };
  //     });
  //   if (this.relateData?.vgroupList)
  //     this.relateData.vgroupList = this.relateData?.vgroupList.map((row) => {
  //       return {
  //         dataStatusName: dict[`client_related.${row.dataStatus}`],
  //         ...row,
  //       };
  //     });
  //   if (this.relateData?.groupAuthList)
  //     this.relateData.groupAuthList = this.relateData?.groupAuthList.map(
  //       (row) => {
  //         return {
  //           dataStatusName: dict[`client_related.${row.dataStatus}`],
  //           ...row,
  //         };
  //       }
  //     );
  //   if (this.relateData?.securityLevelList)
  //     this.relateData.securityLevelList =
  //       this.relateData?.securityLevelList.map((row) => {
  //         return {
  //           dataStatusName: dict[`client_related.${row.dataStatus}`],
  //           ...row,
  //         };
  //       });
  //   if (this.relateData?.rdbConnectionList)
  //     this.relateData.rdbConnectionList =
  //       this.relateData?.rdbConnectionList.map((row) => {
  //         return {
  //           dataStatusName: dict[`client_related.${row.dataStatus}`],
  //           ...row,
  //         };
  //       });
  // }

  uploadFile() {
    // this.relateData = this.testData;
    // this.dataStatusValueTranslate();
    // this.dataStatusValueCheck();
    const reqStr = {
      ReqHeader: this.api.getReqHeader(TxID.importClientRelated),
      ReqBody: {},
    };
    this.ngxService.start();
    this.fileService
      .importClientRelated(reqStr, this.jsonFile!)
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.relateData = res.RespBody;
          // this.dataStatusValueTranslate();
          this.dataStatusValueCheck();
          this.imported = false;
          this.checkLackApi = false;
        }
        this.ngxService.stop();
      });
  }

  dataStatusValueRepeatToCover() {
    if (this.relateData?.clientList)
      this.relateData.clientList = this.relateData?.clientList.map((row) => {
        delete row['dataStatusName'];
        if (row.dataStatus == 'R') row.dataStatus = 'C';
        return row;
      });
    if (this.relateData?.groupList)
      this.relateData.groupList = this.relateData?.groupList.map((row) => {
        delete row['dataStatusName'];
        if (row.dataStatus == 'R') row.dataStatus = 'C';
        return row;
      });
    if (this.relateData?.vgroupList)
      this.relateData.vgroupList = this.relateData?.vgroupList.map((row) => {
        delete row['dataStatusName'];
        if (row.dataStatus == 'R') row.dataStatus = 'C';
        return row;
      });
    if (this.relateData?.groupAuthList)
      this.relateData.groupAuthList = this.relateData?.groupAuthList.map(
        (row) => {
          delete row['dataStatusName'];
          if (row.dataStatus == 'R') row.dataStatus = 'C';
          return row;
        }
      );
    if (this.relateData?.securityLevelList)
      this.relateData.securityLevelList =
        this.relateData?.securityLevelList.map((row) => {
          delete row['dataStatusName'];
          if (row.dataStatus == 'R') row.dataStatus = 'C';
          return row;
        });
    if (this.relateData?.rdbConnectionList)
      this.relateData.rdbConnectionList =
        this.relateData?.rdbConnectionList.map((row) => {
          delete row['dataStatusName'];
          if (row.dataStatus == 'R') row.dataStatus = 'C';
          return row;
        });
  }

  async dataStatusValueCheck() {
    this.checkCAflag =
      this.relateData!.groupList.some((row) => row.dataStatus == 'CA') ||
      this.relateData!.vgroupList.some((row) => row.dataStatus == 'CA') ||
      this.relateData!.groupAuthList.some((row) => row.dataStatus == 'CA') ||
      this.relateData!.securityLevelList.some((row) => row.dataStatus == 'CA');

    if (this.checkCAflag) {
      const code = ['client_related.CA_reject_tip'];
      const dict = await this.toolService.getDict(code);
      this.alertService.ok(dict['client_related.CA_reject_tip'], '');
    }
  }

  async allCover() {
    const code = ['client_related.cfm_cover_all'];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: ' ',
      message: dict['client_related.cfm_cover_all'],
      accept: () => {
        this.ngxService.start();
        this.fileService
          .importClientRelatedAllCover({ longId: this.relateData!.longId })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.dataStatusValueRepeatToCover();
              // this.dataStatusValueTranslate();
            }
            this.ngxService.stop();
          });
      },
    });
  }

  async importConfirm() {
    const code = [
      'client_related.cfm_import',
      'client_related.import_complete',
      'message.success',
    ];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: ' ',
      message: dict['client_related.cfm_import'],
      accept: () => {
        // 確認要匯入新增和覆蓋資料
        this.ngxService.start();
        this.fileService
          .importClientRelatedConfirm({ longId: this.relateData!.longId })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.success']}`,
                detail: `${dict['client_related.import_complete']}`,
              });
              this.imported = true;
            }
            this.ngxService.stop();
          });
      },
    });
  }
}
