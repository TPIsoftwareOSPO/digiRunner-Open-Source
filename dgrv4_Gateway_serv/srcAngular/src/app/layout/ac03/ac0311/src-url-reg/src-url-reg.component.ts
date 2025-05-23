import { ToolService } from 'src/app/shared/services/tool.service';
import { Component, OnInit, forwardRef, ViewChild, ViewContainerRef, Output, EventEmitter } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import * as base64 from 'js-base64'
import { SrcUrlRegDetailComponent } from '../src-url-reg-detail/src-url-reg-detail.component';

@Component({
  selector: 'app-src-url-reg',
  templateUrl: './src-url-reg.component.html',
  styleUrls: ['./src-url-reg.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SrcUrlRegComponent),
    multi: true
  }]
})
export class SrcUrlRegComponent implements OnInit {

  onTouched!: () => void;
  onChange!: (value: any) => void;


  @ViewChild('srcUrlReg', { read: ViewContainerRef, static: true }) srcUrlRegRef!: ViewContainerRef;

  _srcUrl: string = '';
  _srcUrlList: { percent: string, url: string, no: number }[] = [];
  hostnums: number = 0;

  _equal100: boolean = false;
  disabled: boolean = false;

  @Output() testApiEvt: EventEmitter<string> = new EventEmitter;

  constructor(
    private toolService: ToolService,
  ) { }

  ngOnInit(): void {

  }

  writeValue(srcUrl?: string): void {

    this._srcUrl = srcUrl ? srcUrl : '';
    // this.hostnums = 0;
    // this._srcUrlList = [];
    // this.srcUrlRegRef.clear();

    // console.log('src-url-reg writevalue, disabled:', this.disabled)

    this.generateSrcUrlReg();


  }

  generateSrcUrlReg() {

    this.hostnums = 0;
    this._srcUrlList = [];
    this.srcUrlRegRef.clear();

    if (this._srcUrl.includes('b64.')) {

      if (this._srcUrl?.substring(0, 3) == 'b64') {

        let srcUrlArr = this._srcUrl.split('.');
        srcUrlArr.shift();


        let srcUrlArrEdit: { percent: string, url: string }[] = [];
        for (let i = 0; i < srcUrlArr.length; i++) {
          if (i % 2 == 0) {
            srcUrlArrEdit.push({
              percent: srcUrlArr[i],
              // url: this.toolService.Base64Decoder(srcUrlArr[i + 1])
              url: base64.Base64.decode(srcUrlArr[i + 1])
            });
          }
        }
        // console.log(srcUrlArrEdit)
        if (srcUrlArrEdit.length > 0) {
          let totPer: number = 0;

          srcUrlArrEdit.forEach(item => {
            totPer += Number(item.percent);
            this.addSrcUrlReg(item)
          })

          this._equal100 = totPer == 100;

        } else {
          this.addSrcUrlReg();
        }

      }

    }
    else {
      this.addSrcUrlReg({ percent: '100', url: this._srcUrl });
      this._equal100 = true;
    }
  }

  addSrcUrlReg(item?: { percent: string, url: string }) {
    // this.hostInputRef.clear();
    // var componentFactory = this.factoryResolver.resolveComponentFactory(HostInputDetailComponent);
    let componentRef = this.srcUrlRegRef.createComponent(SrcUrlRegDetailComponent);
    if (item) {
      this._srcUrlList.push({ percent: item.percent, url: item.url, no: this.hostnums });
    }
    else {

      this._srcUrlList.push({ percent: '0', url: '', no: this.hostnums });

    }
    // this.componentRef.instance.hostnums = this.hostnums;
    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.hostnums;
    componentRef.instance.data = this._srcUrlList[this.hostnums];
    componentRef.instance.disabled = this.disabled;
    this.hostnums++;
    componentRef.instance.change.subscribe((res: { percent: string, url: string, no: number }) => {
      let idx = this._srcUrlList.findIndex(x => x.no === res.no);

      if (!idx && this._srcUrlList.length == 0) {
        this._srcUrlList.push({ percent: '100', url: '', no: this.hostnums })
      } else {
        let idx = this._srcUrlList.findIndex(host => host.no === res.no);
        this._srcUrlList[idx].percent = res.percent;
        this._srcUrlList[idx].url = res.url;
        this._srcUrlList[idx].no = res.no;
      }

      if (this._srcUrlList.length == 1) {
        this.onChange(this._srcUrlList[0].url);
      }
      else {
        let encodeStrPool: string[] = this._srcUrlList.map(x => {
          return x.percent + '.' + base64.Base64.encodeURL(x.url)
        });

        let encodeString = 'b64.' + encodeStrPool.join('.')
        this.onChange(encodeString);
      }



      let totPer: number = 0;
      this._srcUrlList.forEach(item => {
        totPer += Number(item.percent);
      })
      this._equal100 = totPer == 100;

    });
    componentRef.instance.remove.subscribe(no => {
      let idx = this._srcUrlList.findIndex(host => host.no === no);
      this._srcUrlList.splice(idx, 1);

      if (this._srcUrlList.length == 0) {
        this.hostnums = 0;
        this.onChange('');
        this.addSrcUrlReg();
      }
      else {



        let encodeStrPool: string[] = this._srcUrlList.map(x => {
          return x.percent + '.' + base64.Base64.encodeURL(x.url)
        });


        let encodeString = (this._srcUrlList.length > 0 && (this._srcUrlList[0].percent + this._srcUrlList[0].percent) != '') ? 'b64.' + encodeStrPool.join('.') : ''

        let totPer: number = 0;
        this._srcUrlList.forEach(item => {
          totPer += Number(item.percent);
        })
        this._equal100 = totPer == 100;
        this.onChange(encodeString);
      }
    });

    componentRef.instance.testApiEvt.subscribe(evt => {
      this.testApiEvt.emit(evt)
    })
  }


  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('srcUrlReg 接收disabled狀態', isDisabled)
    this.disabled = isDisabled;

    this.generateSrcUrlReg();
  }

}
