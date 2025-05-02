import { Component, forwardRef, OnInit, ViewChild, ViewContainerRef, Input, Output, EventEmitter } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ApiUrlSettingDetailComponent } from '../api-url-setting-detail/api-url-setting-detail.component';
import { AA0423RespItem } from 'src/app/models/api/ApiService/aa0423.interface';
import { AA0424Req, AA0424ReqSrcUrlList } from 'src/app/models/api/ApiService/aa0424.interface';

interface _AA0424ReqSrcUrlList extends AA0424ReqSrcUrlList {
  no: number
}

@Component({
  selector: 'app-api-url-setting',
  templateUrl: './api-url-setting.component.html',
  styleUrls: ['./api-url-setting.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => ApiUrlSettingComponent),
    multi: true
  }]
})

export class ApiUrlSettingComponent implements OnInit {

  onTouched!: () => void;
  onChange!: (value: any) => void;

  @ViewChild('apiUrlSetting', { read: ViewContainerRef, static: true }) apiUrlSettingRef!: ViewContainerRef;

  no: number = 0;
  srcUrlList: Array<_AA0424ReqSrcUrlList> = [];
  cpRefList: Array<any> = [];

  beyoungRange: boolean = false;
  srcUrlInvalid: boolean = false;

  @Input() procData: Array<AA0423RespItem> = [];
  @Output() changePage: EventEmitter<string> = new EventEmitter;
  @Output() procPreviewPage: EventEmitter<AA0424Req> = new EventEmitter;

  constructor() { }

  ngOnInit(): void {
    // console.log('init', this.procData);
    this.addRow();
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  writeValue(data?: any): void {
    // console.log('data', data);
  }

  addRow() {
    let componentRef = this.apiUrlSettingRef.createComponent(ApiUrlSettingDetailComponent);

    let _row = {
      srcUrl: '',
      isPercentage: false,
      isReplace: false,
      no: this.no
    } as _AA0424ReqSrcUrlList;

    this.srcUrlList.push(_row);

    componentRef.instance.ref = componentRef;
    componentRef.instance.no = this.no;
    componentRef.instance.data = this.srcUrlList[this.no]

    this.cpRefList.push(componentRef);
    this.no++;

    this.srcUrlInvalid = this.cpRefList.some(comp => comp.instance.data.srcUrl == '');
    this.beyoungRange = true;

    componentRef.instance.change.subscribe((res: _AA0424ReqSrcUrlList) => {

      const changedItem = this.srcUrlList.find(item => item.no === res.no)
      if (changedItem) {
        changedItem.srcUrl = res.srcUrl;
        changedItem.isPercentage = res.isPercentage;
        changedItem.percentage = res.percentage;
        changedItem.isReplace = res.isReplace;
        changedItem.replaceString = res.replaceString;


        // //確保修改項目的
        // console.log(changedItem);
        // console.log(this.srcUrlList);
        const checkPercentage = this.srcUrlList.filter(item => item.srcUrl == changedItem.srcUrl)
        if (checkPercentage) {
          let totPercent: number = 0;
          checkPercentage.forEach(item => {

            if (item.isPercentage && item.percentage) {

              totPercent += item.percentage;
            }
          })

          this.cpRefList.forEach(comp => {
            if (comp.instance.srcUrl.value == changedItem.srcUrl) {
              comp.instance.beyoungRange = (totPercent > 100);
            }

          })
          // || (comp.instance.data.isPercentage && comp.instance.data.percentage == '')
          this.beyoungRange = this.cpRefList.some(comp => comp.instance.beyoungRange == true);

          this.srcUrlInvalid = this.cpRefList.some(comp => comp.instance.data.srcUrl == '');

          // console.log(this.srcUrlInvalid);
        }
      }

    })

    componentRef.instance.remove.subscribe(no => {
      let idx = this.srcUrlList.findIndex(item => item.no === no)

      const removeSrcUrl = this.srcUrlList[idx].srcUrl;

      this.srcUrlList.splice(idx, 1);
      this.cpRefList.splice(idx, 1);

      const checkPercentage = this.srcUrlList.filter(item => item.srcUrl == removeSrcUrl)
      if (checkPercentage) {
        let totPercent: number = 0;
        checkPercentage.forEach(item => {
          if (item.isPercentage && item.percentage) totPercent += item.percentage;
        })

        this.cpRefList.forEach(comp => {
          if (comp.instance.srcUrl.value == removeSrcUrl) {
            comp.instance.beyoungRange = (totPercent > 100);
          }
        })

        this.beyoungRange = this.cpRefList.some(comp => comp.instance.beyoungRange == true)
        this.srcUrlInvalid = this.cpRefList.some(comp => comp.instance.data.srcUrl == '');
      }

      if (this.srcUrlList.length == 0) {
        this.no = 0;
        this.addRow();
      }
      // console.log(this.cpRefList);
    })
  }

  proc_temporaryByModifyBatch() {
    // console.log('apiList', this.procData);
    // console.log('srcUrlList', this.srcUrlList);
    let req = {
      apiList: this.procData.map(item => {
        return {
          apiKey: item.apiKey,
          moduleName: item.moduleName
        }
      }),
      // srcUrlList: this.srcUrlList.map(item => {
      //   return {
      //     srcUrl: item.srcUrl,
      //     isPercentage: item.isPercentage,
      //     percentage: item.percentage,
      //     isReplace: item.isReplace,
      //     replaceString: item.replaceString
      //   }}
      srcUrlList: this.srcUrlList.map(({no,...item})=> item )

    } as AA0424Req;
    this.procPreviewPage.emit(req);
  }



}
