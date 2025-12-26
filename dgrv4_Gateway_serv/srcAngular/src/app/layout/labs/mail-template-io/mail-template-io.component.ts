import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
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
  selector: 'app-mail-template-io',
  templateUrl: './mail-template-io.component.html',
  styleUrls: ['./mail-template-io.component.css'],
  providers: [MessageService, ConfirmationService],
})
export class MailTemplateIoComponent extends BaseComponent implements OnInit {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;
  currentTitle = this.title;
  pageNum: number = 1;

  _file?: File;

  checkLackApi: boolean = false;
  relateData?: AA1121Resp;

  checkCAflag: boolean = false;
  imported: boolean = false;

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

  exportTsmpDpMailTpltClick() {
    this.ngxService.start();
    this.serverService.exportTsmpDpMailTplt().subscribe((res: any) => {
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
        FileSaver.saveAs(data, `MailTplt_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  openFileBrowser() {
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
    this.fileInput?.nativeElement.click();
  }

  changeFile(event:Event) {
    const element = event.target as HTMLInputElement;
    if (element.files && element.files.length != 0) {
      this._file = element.files[0];
    } else {
       if (this.fileInput) {
         this.fileInput.nativeElement.value = '';
       }
      this._file = undefined;
    }
  }

  uploadFile() {
    const reqStr = {
      ReqHeader: this.api.getReqHeader(TxID.importTsmpDpMailTplt),
      ReqBody: {},
    };
    this.ngxService.start();
    console.log(this._file);
    this.serverService
      .importTsmpDpMailTplt(reqStr, this._file!)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['uploading', 'message.success', 'upload_result'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: dict['upload_result'],
            detail: `${dict['message.success']}!`,
          });
          this._file = undefined;
        }
        this.ngxService.stop();
      });
  }
}
