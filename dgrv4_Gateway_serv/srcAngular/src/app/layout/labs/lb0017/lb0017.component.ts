import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
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
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { SharedPipesModule } from 'src/app/shared/pipes/shared-pipes.module';
import { TokenExpiredGuard } from 'src/app/shared';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { DPB9940Req } from 'src/app/models/api/ServerService/dpb9940.interface';
import { TxID, AlertType } from 'src/app/models/common.enum';
import dayjs from 'dayjs';
import * as FileSaver from 'file-saver';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-lb0017',
  templateUrl: './lb0017.component.html',
  styleUrls: ['./lb0017.component.css'],
  providers: [TokenExpiredGuard],
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  standalone: true,
})
export class Lb0017Component extends BaseComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  currentTitle = this.title;
  form!: FormGroup;

  localeList: { label: string; value: string }[] = [];
  exportItemOptions: { label: string; value: string }[] = [];

  file: File | null = null;
  fileName: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private listService: ListService,
    private serverService: ServerService,
    private api: ApiBaseService,
    private alertService: AlertService,
    private messageService: MessageService,
    private ngxService: NgxUiLoaderService,
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.form = this.fb.group({
      exportItemList: new UntypedFormControl([]),
      exportLocaleList: new UntypedFormControl([]),
    });

    this.exportItemOptions = [
      { label: 'setting', value: 'setting' },
      { label: 'items', value: 'items' },
      { label: 'mailTplt', value: 'mailTplt' },
      { label: 'func', value: 'func' },
      { label: 'embeddedFunc', value: 'embeddedFunc' },
      { label: 'rtnCode', value: 'rtnCode' },
    ];

    let ReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('RTN_CODE_LOCALE'),
        ) +
        ',' +
        22,
      isDefault: 'N',
    } as DPB0047Req;
    this.listService.querySubItemsByItemNo(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (res.RespBody.subItems) {
          this.localeList = res.RespBody.subItems.map((item) => {
            return { label: item.subitemName, value: item.subitemNo };
          });
        }
      }
    });
  }

  export() {
    const reqBody = {
      exportItemList: this.exportItemList.value,
      exportLocaleList: this.exportLocaleList.value,
    } as DPB9940Req;

    this.ngxService.start();
    this.serverService.exportMultilingual(reqBody).subscribe(async (res) => {
      if (res.type === 'application/json') {
        const reader = new FileReader();
        reader.onload = () => {
          const jsonData = JSON.parse(reader.result as string);
          this.alertService.ok(
            jsonData.ResHeader.rtnMsg,
            '',
            AlertType.warning,
            jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID,
          );
        };
        reader.readAsText(res);
      } else {
        const data: Blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
        });

        const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
        FileSaver.saveAs(data, `Multilingual_${date}.xlsx`);

        const code = ['export', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: dict['export'],
          detail: `${dict['export']} ${dict['message.success']}!`,
        });
      }
      this.ngxService.stop();
    });
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;
    if (file.length != 0) {
      this.file = file.item(0);
      this.fileName = file[0].name;
      event.target.value = '';
    } else {
      this.file = null;
      event.target.value = '';
    }
  }

  openFileBrowser() {
    this.fileInput.nativeElement.click();
  }

  importMultilingual() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importMultilingual),
      ReqBody: {},
    };

    this.ngxService.start();
    this.serverService
      .importMultilingual(req, this.file!)
      .pipe(finalize(() => this.ngxService.stop()))
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['uploading', 'message.success', 'upload_result'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: dict['upload_result'],
            detail: `${dict['message.success']}!`,
          });
          this.file = null;
          this.fileName = '';
        }
      });
  }

  headerReturn() {}

  public get exportItemList() {
    return this.form.get('exportItemList')!;
  }
  public get exportLocaleList() {
    return this.form.get('exportLocaleList')!;
  }
}
