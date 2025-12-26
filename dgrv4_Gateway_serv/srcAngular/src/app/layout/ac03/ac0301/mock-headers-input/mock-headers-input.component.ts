import { MockHeadersInputDetailComponent } from './../mock-headers-input-detail/mock-headers-input-detail.component';
import { AA0313KeyVal } from './../../../../models/api/ApiService/aa0313.interface';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { Component, OnInit, forwardRef, ViewChild, ViewContainerRef } from '@angular/core';

@Component({
  selector: 'app-mock-headers-input',
  templateUrl: './mock-headers-input.component.html',
  styleUrls: ['./mock-headers-input.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => MockHeadersInputComponent),
    multi: true
  }]
})
export class MockHeadersInputComponent implements OnInit {

  @ViewChild('mockHeadersInput', { read: ViewContainerRef, static: true }) _mockHeaderInputRef!: ViewContainerRef;
  onTouched!: () => void;
  onChange!: (value: any) => void;

  idx: number = 0;
  dataList: {key:string,value:string,no:number}[] = [];

  constructor() { }

  ngOnInit(): void {
  }

  writeValue(data: AA0313KeyVal[] ): void {
    this.idx = 0;
    if(data && data.length>0)
    {
      data.forEach(row=>{
        this.addInput(row)
      })
    }
    else{
      this.addInput();
    }

  }

  addInput(row?:AA0313KeyVal) {
    let componentRef = this._mockHeaderInputRef.createComponent(MockHeadersInputDetailComponent);
    if(row)
    {
      this.dataList.push({key: row.key,value:row.value,no:this.idx})
    }
    else{
      this.dataList.push({key: '', value: '', no: this.idx})
    }

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.idx;
    componentRef.instance.data = this.dataList[this.idx];
    this.idx++;

    componentRef.instance.change.subscribe((res: { key: string, value: string, no: number }) => {
      let idx = this.dataList.findIndex(x => x.no === res.no);

      if (!idx && this.dataList.length == 0) {
        this.dataList.push({ key: '', value: '', no: this.idx })
      } else {
        let idx = this.dataList.findIndex(host => host.no === res.no);
        this.dataList[idx].key = res.key;
        this.dataList[idx].value = res.value;
        this.dataList[idx].no = res.no;
      }



      const _mockHeaders = this.dataList.filter(row=>{
        return (row.key && row.key!='') || (row.value && row.value!='')
      })
      .map(row=>{
        return {key:row.key,value:row.value};
      })
      this.onChange(_mockHeaders);

    });
    componentRef.instance.remove.subscribe(no => {
      let idx = this.dataList.findIndex(host => host.no === no);
      this.dataList.splice(idx, 1);

      if (this.dataList.length == 0) {
        this.idx = 0;
        this.onChange(null);
        this.addInput();
      }
      else {
        const _mockHeaders = this.dataList.filter(row=>{
          return (row.key && row.key!='') || (row.value && row.value!='')
        })
        .map(row=>{
          return {key:row.key,value:row.value};
        })
        this.onChange(_mockHeaders);
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
