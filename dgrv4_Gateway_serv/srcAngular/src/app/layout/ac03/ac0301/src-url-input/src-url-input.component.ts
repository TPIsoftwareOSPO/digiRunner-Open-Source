import { SrcUrlInputDetailComponent } from './../src-url-input-detail/src-url-input-detail.component';
import { ToolService } from 'src/app/shared/services/tool.service';
import { Component, OnInit, ChangeDetectionStrategy, forwardRef, ViewChild, ViewContainerRef, Output, EventEmitter, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import * as base64 from 'js-base64'

@Component({
  selector: 'app-src-url-input',
  templateUrl: './src-url-input.component.html',
  styleUrls: ['./src-url-input.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SrcUrlInputComponent),
    multi: true
  }]
})
export class SrcUrlInputComponent implements OnInit {

  onTouched!: () => void;
  onChange!: (value: any) => void;

  @ViewChild('srcUrlInput', { read: ViewContainerRef, static: true }) srcUrlInputRef!: ViewContainerRef;

  @Input() _disabled:boolean = false;

  _srcUrl: string = '';
  _srcUrlList: { percent: string, url: string, no: number }[] = [];
  hostnums: number = 0;

  _equal100: boolean = false;

  constructor(
    private toolService: ToolService,
  ) { }

  ngOnInit(): void {
  }

  writeValue(srcUrl: string): void {
    this.srcUrlInputRef.clear();
    this._srcUrlList = [];
    this._srcUrl = '';
    this._equal100 = false;
    this.hostnums = 0;

    this._srcUrl = srcUrl;


    if (this._srcUrl.includes('b64.')) {

      if (this._srcUrl?.substring(0, 3) == 'b64') {

        let srcUrlArr = this._srcUrl.split('.');
        srcUrlArr.shift();


        let srcUrlArrEdit: { percent: string, url: string }[] = [];
        for (let i = 0; i < srcUrlArr.length; i++) {
          if (i % 2 == 0) {
            srcUrlArrEdit.push({
               percent: srcUrlArr[i],
               url: base64.Base64.decode(srcUrlArr[i + 1])
              });
          }
        }
        // console.log(srcUrlArrEdit)
        if (srcUrlArrEdit.length > 0) {
          let totPer: number = 0;

          srcUrlArrEdit.forEach(item => {
            totPer += Number(item.percent);
            this.addSrcUrlInput(item)
          })

          this._equal100 = totPer == 100;

        } else {
          this.addSrcUrlInput();
        }

      }

    }
    else {
      this.addSrcUrlInput({ percent: '100', url: this._srcUrl });
      this._equal100 = true;
    }

  }

  addSrcUrlInput(item?: { percent: string, url: string }) {
    // this.hostInputRef.clear();
    // var componentFactory = this.factoryResolver.resolveComponentFactory(HostInputDetailComponent);
    let componentRef = this.srcUrlInputRef.createComponent(SrcUrlInputDetailComponent);
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
    componentRef.instance.disabled = this._disabled;
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

      // console.log(this._srcUrlList.length)
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
        this.addSrcUrlInput();
      }
      else if (this._srcUrlList.length == 1) {
        this.onChange(this._srcUrlList[0].url);
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
  }


  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

}
