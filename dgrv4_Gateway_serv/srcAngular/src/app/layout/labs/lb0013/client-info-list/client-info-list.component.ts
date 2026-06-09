import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { AA0202List } from 'src/app/models/api/ClientService/aa0202.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-client-info-list',
  templateUrl: './client-info-list.component.html',
  styleUrls: ['./client-info-list.component.css'],
  standalone: false,
})
export class ClientInfoListComponent implements OnInit {
  form!: FormGroup;
  queryStatusOption: { label: string; value: string }[] = [];
  clientInfoData: Array<AA0202List> = [];
  selectedClientInfoData: Array<AA0202List> = [];
  cols: { field: string; header: string }[] = [];

  constructor(
    private clientService: ClientService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private toolService: ToolService,
    private listService: ListService,
    private fb: FormBuilder,
    private translate: TranslateService,
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      keyword: new FormControl(''),
      encodeStatus: new FormControl('-1'),
    });

    this.cols = [
      { field: 'statusName', header: this.translate.instant('status') },
      { field: 'clientName', header: this.translate.instant('client_name') },
      { field: 'clientAlias', header: this.translate.instant('client_alias') },
    ];

    let ReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('ENABLE_FLAG'),
        ) +
        ',' +
        9,
      isDefault: 'N',
    } as DPB0047Req;

    this.listService.querySubItemsByItemNo(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.queryStatusOption =
          res.RespBody.subItems?.map((row: any) => {
            console.log(row);
            return { label: row.subitemName, value: row.subitemNo };
          }) ?? [];
        console.log(this.queryStatusOption);
      }
    });

    this.clientService
      .queryClientList_ignore1298({
        keyword: this.keyword!.value,
        encodeStatus:
          this.toolService.Base64Encoder(
            this.toolService.BcryptEncoder(this.encodeStatus!.value),
          ) +
          ',' +
          this.convertEncodeStatusIndex(this.encodeStatus!.value),
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.clientInfoData = res.RespBody.clientInfoList;
        }
      });
  }

  queryClientList() {

    this.clientService
      .queryClientList({
        keyword: this.keyword!.value,
        encodeStatus:
          this.toolService.Base64Encoder(
            this.toolService.BcryptEncoder(this.encodeStatus!.value),
          ) +
          ',' +
          this.convertEncodeStatusIndex(this.encodeStatus!.value),
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.clientInfoData = res.RespBody.clientInfoList;
        }
        else this.clientInfoData = [];
      });
  }

  convertEncodeStatusIndex(encodeStatus: string) {
    switch (encodeStatus) {
      case '1': // 啟用
        return 0;
      case '0': // 停用
        return 1;
      case '-1': // 全部
        return 2;
      case '2': // 鎖定
        return 3;
      default:
        return encodeStatus;
    }
  }

  moreData() {
    this.clientService
      .queryClientList({
        clientId: this.clientInfoData[this.clientInfoData.length - 1].clientId,
        keyword: this.keyword!.value,
        encodeStatus:
          this.toolService.Base64Encoder(
            this.toolService.BcryptEncoder(this.encodeStatus!.value),
          ) +
          ',' +
          this.convertEncodeStatusIndex(this.encodeStatus!.value),
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.clientInfoData = this.clientInfoData.concat(
            res.RespBody.clientInfoList,
          );
        }
      });
  }

  chooseClient(){
  this.ref.close(this.selectedClientInfoData);
  }
  cancelClient(){
    this.ref.close(null);
  }

  public get keyword() {
    return this.form.get('keyword');
  }

  public get encodeStatus() {
    return this.form.get('encodeStatus');
  }
}
