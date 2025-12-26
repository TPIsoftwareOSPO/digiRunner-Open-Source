import { WebsiteSettingRowComponent } from './../website-setting-row/website-setting-row.component';
import { Component, EventEmitter, OnInit, Output, ViewChild, ViewContainerRef, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { DPB0201RespItem } from 'src/app/models/api/ServerService/dpb0201.interface';


@Component({
  selector: 'app-website-setting',
  templateUrl: './website-setting.component.html',
  styleUrls: ['./website-setting.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => WebsiteSettingComponent),
    multi: true
  }]
})
export class WebsiteSettingComponent implements OnInit {

  onTouched!: () => void;
  onChange!: (value: any) => void;
  disabled: boolean = false;

  @ViewChild('websiterow', { read: ViewContainerRef, static: true }) websiterowRef!: ViewContainerRef;

  _equal100: boolean = false;

  _websiteList: { probability: number, url: string, no: number, rowValid:boolean, targetThroughPut?:DPB0201RespItem }[] = [];
  hostnums: number = 0;

  @Output() formValid:EventEmitter<boolean> = new EventEmitter;

  constructor() { }

  ngOnInit(): void {

  }

  addRow(rowData?:{probability:number, url:string, targetThroughPut?:DPB0201RespItem }){
    let componentRef = this.websiterowRef.createComponent(WebsiteSettingRowComponent);
    if (rowData) {
      this._websiteList.push({ probability: rowData.probability, url: rowData.url, no: this.hostnums, rowValid:true, targetThroughPut: rowData.targetThroughPut });
    }
    else {
      this._websiteList.push({ probability: 0 , url: '', no: this.hostnums, rowValid:false });
      this.formValid.emit(false);
    }

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.hostnums;
    componentRef.instance.data = this._websiteList[this.hostnums];
    // console.log(this.disabled)
    componentRef.instance.readonly = this.disabled;


    this.hostnums++;
    componentRef.instance.change.subscribe((res: { probability: number, url: string, no: number, rowValid: boolean }) => {
      let idx = this._websiteList.findIndex(x => x.no === res.no);

      if (!idx && this._websiteList.length == 0) {
        this._websiteList.push({ probability: 100, url: '', no: this.hostnums, rowValid:false })
      } else {
        let idx = this._websiteList.findIndex(host => host.no === res.no);
        this._websiteList[idx].probability = res.probability;
        this._websiteList[idx].url = res.url;
        this._websiteList[idx].no = res.no;
        this._websiteList[idx].rowValid = res.rowValid;

      }


      this.onChange(this._websiteList)
      let totPer: number = 0;
      // console.log(this._websiteList)
      let rowValid = this._websiteList.every(item=> item.rowValid)
      this._websiteList.forEach(item => {
        totPer += item.probability;
      })
      this._equal100 = totPer == 100;

      // console.log( '百分比:', this._equal100)
      // console.log( '欄位檢核:',rowValid )
      // console.log( '百分比及欄位檢核:',rowValid && this._equal100)
      this.formValid.emit( rowValid && this._equal100);

    });
    componentRef.instance.remove.subscribe(no => {
      let idx = this._websiteList.findIndex(host => host.no === no);
      this._websiteList.splice(idx, 1);

      if (this._websiteList.length == 0) {
        this.hostnums = 0;
        this.onChange([]);
        this.addRow();
      }
      else {

        this.onChange(this._websiteList);
      }
    });

  }

  writeValue(data:any): void {
    // console.log(data)
    this.hostnums = 0;
    this._websiteList = [];
    this.websiterowRef.clear();
    if(data){
      data.forEach( rowData => {
        let addItem = {
          probability: rowData.probability,
          url: rowData.url,
          targetThroughPut: rowData.targetThroughPut
        }

        this.addRow(addItem);
      });
      this.formValid.emit(true);
    }else{

      this.addRow({probability:100,url:''})
      this._equal100 = true;
    }
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('接收disabled狀態', isDisabled)
    this.disabled = isDisabled;
  }

}
