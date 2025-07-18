import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import {
  AA0302RedirectByIpData,
  AA0302Resp,
} from 'src/app/models/api/ApiService/aa0302_v3.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as base64 from 'js-base64';
import { AA0320Item } from 'src/app/models/api/ApiService/aa0320.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolServiceWebhoook } from 'src/app/shared/services/tool-webhook.service';

@Component({
  selector: 'app-api-detail',
  templateUrl: './api-detail.component.html',
  styleUrls: ['./api-detail.component.css'],
})
export class ApiDetailComponent implements OnInit {
  @Input() apiDetail!: AA0302Resp;

  @Input() srcUrlPool: { percent: string; url: string }[] = [];
  @Input() ipSrcUrl: {
    ipForRediret: string;
    srcUrlPool: { percent: string; url: string }[];
  }[] = [];
  @Input() apiGroupList: Array<AA0320Item> = [];
  @Input() detailCols: { field: string; header: string }[] = [];

  @Output() getApiGroupListEvt: EventEmitter<string> = new EventEmitter();

  detailForm: FormGroup;

  isWebhook: boolean = false;

  constructor(
    private toolServiceW: ToolServiceWebhoook,
    private translateService: TranslateService,
    private messageService: MessageService,
    private fb: FormBuilder
  ) {
    this.detailForm = this.fb.group({
      keyword: new FormControl(''),
    });
  }

  ngOnInit() {
    if (this.apiDetail.srcUrl) {
      const dgrProtocol_match = this.toolServiceW.dgrProtocol_match(
        this.apiDetail.srcUrl.o ? this.apiDetail.srcUrl.o: this.apiDetail.srcUrl.v
      );

      if (dgrProtocol_match.length > 0) {
        this.isWebhook = dgrProtocol_match.some((item) => item.includes('webhook'));
      }
    }
  }

  switchOri(item: any) {
    item.t = !item.t;
  }

  originString(item: any) {
    let str: String = item.o ? (item.t == true ? item.v : item.o) : item.v;
    return !str || str == '[]' || str == '' ? ' - ' : str;
  }

  procSrcUrl(srcUrlObj) {
    let srcUrl = srcUrlObj.o ? srcUrlObj.o : srcUrlObj.v;
    this.srcUrlPool = [];

    if (srcUrl.includes('b64.')) {
      let _srcUrl = srcUrl.split('b64.')[1];
      let srcUrlArr = srcUrl.split('.');
      srcUrlArr.shift();

      for (let i = 0; i < srcUrlArr.length; i++) {
        if (i % 2 == 0) {
          this.srcUrlPool.push({
            percent: srcUrlArr[i],
            //  url: this.tool.Base64Decoder(srcUrlArr[i + 1])
            url: base64.Base64.decode(srcUrlArr[i + 1]),
          });
        }
      }
    } else {
      this.srcUrlPool.push({ percent: '100', url: srcUrl });
    }
  }

  queryApiGroupList() {
    // console.log(this.keyword.value)
    this.getApiGroupListEvt.emit(this.keyword.value);
  }

  public get keyword() {
    return this.detailForm.get('keyword')!;
  }
}
