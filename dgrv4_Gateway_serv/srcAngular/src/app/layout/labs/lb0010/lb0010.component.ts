import { map } from 'rxjs/operators';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import * as dayjs from 'dayjs';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DPB0232WhitelistItem } from 'src/app/models/api/ServerService/dpb0232.interface';
import { DPB0233Req } from 'src/app/models/api/ServerService/dpb0233.interface';

@Component({
  selector: 'app-lb0010',
  templateUrl: './lb0010.component.html',
  styleUrls: ['./lb0010.component.css'],
  providers: [MessageService, ConfirmationService],
})
export class Lb0010Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  form!: FormGroup;
  currentAction: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private openApiService: OpenApiKeyService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);

    this.form = this.fb.group({
      status: new FormControl(''),
      dataList: new FormControl([]),
    });
  }

  ngOnInit() {
    this.serverService
      .createAndUpdateBotDetectionList_before()
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.addFormValidator(this.form, res.RespBody.constraints);
          this.status.markAsTouched();
        }
      });
    this.axios_queryBotDetectionList();
  }

  axios_queryBotDetectionList() {
    this.serverService.queryBotDetectionList().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.status.setValue(res.RespBody.status);
        this.dataList.setValue(res.RespBody.dataList);
      } else {
        this.status.setValue('');
        this.dataList.setValue([]);
      }
    });
  }

  update() {
    let _whitelistDataList = null;
    if (this.dataList.value && this.dataList.value.length >0) {
      _whitelistDataList = this.dataList.value
        .filter(
          (item) => !(item.id === '' && item.rule === '') //當id,rule都為空白時過濾該筆資料
        )
        .map((item) => {
          return {
            id: item.id,
            rule: item.rule,
          };
        });
    }

    let req = {
      status: this.status.value,
      whitelistDataList: _whitelistDataList
    } as DPB0233Req;

    this.ngxSrvice.start();
    this.serverService
      .createAndUpdateBotDetectionList(req)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['message.update', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['message.update']}`,
            detail: `${dict['message.update']} ${dict['message.success']}!`,
          });
          this.axios_queryBotDetectionList();
        }
        this.ngxSrvice.stop();
      });
  }

  public get status() {
    return this.form.get('status')!;
  }
  public get dataList() {
    return this.form.get('dataList')!;
  }
}
