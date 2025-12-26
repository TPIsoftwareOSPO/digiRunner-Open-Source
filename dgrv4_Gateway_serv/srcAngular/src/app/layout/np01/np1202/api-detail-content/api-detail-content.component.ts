import { Component, OnInit } from "@angular/core";
import { FormBuilder } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { AA0302RedirectByIpData, AA0302Resp } from "src/app/models/api/ApiService/aa0302_v3.interface";
import { AA0320Item, AA0320Req } from "src/app/models/api/ApiService/aa0320.interface";
import { ToolService } from "src/app/shared/services/tool.service";
import * as base64 from 'js-base64';
import { ApiService } from "src/app/shared/services/api-api.service";

@Component({
  selector: 'app-api-detail-content',
  templateUrl: './api-detail-content.component.html',
  styleUrls: ['./api-detail-content.component.css'],
  providers:[ApiService]
})
export class ApiDetailContentComponent implements OnInit {
  apiDetail!: AA0302Resp;
  srcUrlPool: { percent: string; url: string }[] = [];
  ipSrcUrl: {
    ipForRediret: string;
    srcUrlPool: { percent: string; url: string }[];
  }[] = [];
  apiGroupList: Array<AA0320Item> = [];
  detailCols: { field: string; header: string }[] = [];

  constructor(
    private toolService: ToolService,
    private translateService: TranslateService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private apiService: ApiService,
  ) {
     if(this.config.data.apiDetail){
      this.apiDetail = this.config.data.apiDetail ;
      this.procSrcUrl(this.apiDetail.srcUrl);
      if (this.apiDetail.isRedirectByIp) {
        this.generateIPSrcUrl(this.apiDetail.redirectByIpDataList);
      }

      if (!this.apiDetail.headerMaskPolicy) this.apiDetail.headerMaskPolicy = '0';
      if (!this.apiDetail.bodyMaskPolicy) this.apiDetail.bodyMaskPolicy = '0';
     }

  }

 async ngOnInit() {
    const dict = await this.toolService.getDict(['group_id', 'group_id', 'group_name', 'group_alias', 'group_desc']);
    this.detailCols = [
      { field: 'gId', header: dict['group_id'] },
      {
        field: 'name',
        header: `${dict['group_name']}(${dict['group_alias']})`,
      },
      { field: 'desc', header: dict['group_desc'] },
    ];
    this.queryApiGroupList();
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

  generateIPSrcUrl(ipSrcUrl: Array<AA0302RedirectByIpData>) {
    // console.log(ipSrcUrl)
    this.ipSrcUrl = ipSrcUrl.map((row) => {
      return {
        ipForRediret: row.ipForRedirect,
        srcUrlPool: this.formatSrcUrl(row.ipSrcUrl),
      };
    });

  }

  formatSrcUrl(srcUrlObj) {
    let _srcUrl = srcUrlObj.o ? srcUrlObj.o : srcUrlObj.v;
    let _srcUrlPool: { percent: string; url: string }[] = [];

    if (_srcUrl.includes('b64.')) {
      let srcUrlArr = _srcUrl.split('.');
      srcUrlArr.shift();
      for (let i = 0; i < srcUrlArr.length; i++) {
        if (i % 2 == 0) {
          _srcUrlPool.push({
            percent: srcUrlArr[i],
            url: base64.Base64.decode(srcUrlArr[i + 1]),
          });
        }
      }
    } else {
      _srcUrlPool.push({ percent: '100', url: _srcUrl });
    }
    return _srcUrlPool;
  }

  getApiGroupListEvt(keyword) {
    // console.log('keyword',keyword)
    this.queryApiGroupList(keyword);
  }

  queryApiGroupList(keyword?:any) {
    this.apiGroupList = [];
    let ReqBody = {
      keyword: keyword? keyword:'',
      moduleName: this.config.data.apiData.moduleName,
      apiKey:this.config.data.apiData.apiPath,
    } as AA0320Req;
    this.apiService.queryGroupApiList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.apiGroupList = res.RespBody.dataList;
      }
    });
  }

  close(){
    this.ref.close();
  }

}
