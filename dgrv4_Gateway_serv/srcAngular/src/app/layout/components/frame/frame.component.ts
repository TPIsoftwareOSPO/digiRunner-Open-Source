import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { environment } from 'src/environments/environment';
import { TimeRange } from 'src/app/models/common.enum';
// const createHash = require("sha256-uint8array").createHash;
const base64url = require('base64url');
import * as shajs from 'sha.js';
import { AlertService } from 'src/app/shared/services/alert.service';
import { delay, Subscription } from 'rxjs';
import { event } from 'jquery';


@Component({
  selector: 'app-frame',
  templateUrl: './frame.component.html',
  styleUrls: ['./frame.component.css'],
  providers: [UtilService],
})
export class FrameComponent implements OnInit {
  @Input('reportID') reportID: string = '';

  url: SafeResourceUrl = '';
  timeRange = TimeRange.Today;
  timeRanges;
  showStatus: boolean = false;
  msgListenterAdd: boolean = false;
  messageSubscription?: Subscription;

  messageHandler: (event: MessageEvent) => void;
  processing:boolean = false;

  constructor(
    private sanitizer: DomSanitizer,
    private util: UtilService,
    private tool: ToolService,
    private alertService: AlertService,

  ) {

    this.messageHandler = this.createMessageHandler();
  }

  ngOnInit() {
    this.showStatus = false;

    // this.timeRanges = this.tool.getTimeRange();
    this.url = this.sanitizer.bypassSecurityTrustResourceUrl('');
    this.search();

    window.addEventListener('message', this.messageHandler);
  }

  ngOnDestroy() {
    window.removeEventListener('message', this.messageHandler);
  }

  createMessageHandler(): (event: MessageEvent) => void {
    return (event: MessageEvent) => {
      if (event.data?.from == 'frame' && event.data?.type == 'token_expired') {
        if(!this.processing){
          this.processing = true; //避免同時事件(兩筆以上)造成 oauth/token事件被cancel導致登出
          this.util!.queryReportUrls({
            reportID: this.reportID.toUpperCase(),
            timeRange: 'T',
          }).subscribe((r) => {
            this.processing = false;
            if (this.tool.checkDpSuccess(r.ResHeader)) {
              const iframe = document.getElementById(
                '_frame'
              ) as HTMLIFrameElement;
              iframe.contentWindow?.postMessage(
                { from: 'dgr', type: 'redo' },
                '*'
              );
            }
          });
        }
      }
      else if(event.data?.from == 'frame' && event.data?.type == 'reset_idle_time'){
        // console.log('reset_idle_time')
        this.tool.setExpiredTime();
      }
    };
  }

  search(noframe: boolean = false) {
    let uuidObj = this.generateCAPI();

    if (this.timeRange) {
      this.util
        .queryReportUrls({
          reportID: this.reportID.toUpperCase(), //此欄位必須大寫，否則若客戶使用Orcle資料庫/大小寫有區別會造成查無資料的錯誤
          timeRange: 'T',
        })
        .subscribe((r) => {
          if (this.tool.checkDpSuccess(r.ResHeader)) {
            this.showStatus = true;

            if (noframe) return;
            let tarUrl = '';
            if (
              location.hostname == 'localhost' ||
              location.hostname == '127.0.0.1'
            ) {
              tarUrl = environment.apiUrl;
            } else {
              tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
            }

            // SYS_RPT:kibana報表
            // IFRAME:嵌內頁面
            // LINK:另開頁面
            let url = '';
            switch (r.RespBody.reportType) {
              case 'SYS_RPT':
                url = `${tarUrl}${r.RespBody.rpContentPath}/login?cuuid=${uuidObj.uuid}&capikey=${uuidObj.capi}&reportURL=${r.RespBody.reportUrl}&embed=true`;
                this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
                break;
              case 'IFRAME':
                if (environment.production) {
                  url = `${location.protocol}//${location.hostname}:${location.port}${r.RespBody.reportUrl}`;
                } else {
                  url = `${environment.cusHostName}${r.RespBody.reportUrl}`;
                }
                this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
                break;
              case 'LINK':
                url = `${r.RespBody.reportUrl}`;
                window.open(url, '_blank');
                break;
              default:
                // 預期reportType存在
                this.alertService.ok('reportType not found', '');

                break;
            }
          }
        });
    }
  }

  generate_uuid() {
    var d = Date.now();
    if (
      typeof performance !== 'undefined' &&
      typeof performance.now === 'function'
    ) {
      d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
      /[xy]/g,
      function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
      }
    );
  }

  generateCAPI(): { uuid: string; capi: string } {
    let _uuid = this.generate_uuid().toUpperCase();
    let _capi = this.encodeUUID(_uuid);

    /**20230617 若錯誤給預設值 */
    if (_capi === 'Error: Digest method not supported') {
      _uuid = '67C42D24-BCFD-40B1-99E9-C4CE4D9ADBCD';
      _capi = 'tflvB2FW3nhUIvF3PMHQxw';
    }

    let uuidEncode: { uuid: string; capi: string } = {
      uuid: _uuid,
      capi: _capi,
    };

    return uuidEncode;
  }

  // CAPI key演算法流程
  encodeUUID(uuid: string) {
    if (typeof uuid !== 'string' || uuid.length === 0) return '';
    const sTpi = 'TPIdigiRunner';
    const arrayTpi = Uint8Array.of(0x80);
    const textEncoder = new TextEncoder();

    try {
      let sTarget = uuid.toLocaleUpperCase() + sTpi;

      let u8PayloadBytes = textEncoder.encode(sTarget);
      let u8Result = this.concatenate(Uint8Array, u8PayloadBytes, arrayTpi);

      // let sSha256 = createHash("SHA256").update(u8Result).digest();
      // console.log('createHash',sSha256)
      let sSha256 = shajs('sha256').update(u8Result).digest();
      // console.log('shajs',test)
      let sHalfSha256 = sSha256.subarray(sSha256.length / 2, sSha256.length);
      const result = base64url(sHalfSha256);
      return result;
    } catch (e) {
      console.log('e', e);
      return e;
    }
  }

  concatenate(resultConstructor, ...arrays) {
    let totalLength = 0;
    for (const arr of arrays) {
      totalLength += arr.length;
    }
    const result = new resultConstructor(totalLength);
    let offset = 0;
    for (const arr of arrays) {
      result.set(arr, offset);
      offset += arr.length;
    }
    return result;
  }
}
