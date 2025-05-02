import { Component, OnInit, forwardRef, ViewChild, ViewContainerRef, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { DPB0181LdapDataItem } from 'src/app/models/api/ServerService/dpb0181.interface';
import { LdapDataListDetailComponent } from '../ldap-data-list-detail/ldap-data-list-detail.component';
import { DPB0182LdapDataItem } from 'src/app/models/api/ServerService/dpb0182.interface';

interface _ldapDataListDetail extends DPB0181LdapDataItem {
  valid: boolean;
  no:number;
  detailId?:string;
}

@Component({
  selector: 'app-ldap-data-list',
  templateUrl: './ldap-data-list.component.html',
  styleUrls: ['./ldap-data-list.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => LdapDataListComponent),
    multi: true
  }]
})
export class LdapDataListComponent implements OnInit {
  @ViewChild('ldapData', { read: ViewContainerRef, static: true }) ldapDataRef!: ViewContainerRef;

  onTouched!: () => void;
  onChange!: (value: any) => void;

  @Input() action:string='';

  // 用來接收 setDisabledState 的狀態
  disabled:boolean = false;

  no: number = 0;

  _ldapDataList: Array<_ldapDataListDetail> = [];

  constructor() { }

  ngOnInit(): void {

  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  writeValue(data?: Array<DPB0182LdapDataItem> | String): void {
    // console.log(this.action)
    // console.log('ldap data', data)
    // console.log('ldap data', typeof data)
    this.ldapDataRef.clear();
    this._ldapDataList = [];
    this.no = 0;
    if (data == '') {
      this.addldapData();
    }
    else{
      // console.log('data', data)
      if(data && Array.isArray(data))
      {
        data.forEach(item => {
          let pushItem = {
            orderNo: item.orderNo,
            ldapUrl: item.ldapUrl,
            ldapBaseDn: item.ldapBaseDn,
            ldapDn: item.ldapDn
          } as DPB0182LdapDataItem;
          if(item.detailId) pushItem.detailId = item.detailId;
          this.addldapData(pushItem);
        });
      }

    }
  }

  addldapData(data?: DPB0182LdapDataItem) {
    let componentRef = this.ldapDataRef.createComponent(LdapDataListDetailComponent);
    let addData = { } as _ldapDataListDetail;
    if (data) {
      addData.orderNo = data.orderNo;
      addData.ldapUrl = data.ldapUrl;
      addData.ldapBaseDn = data.ldapBaseDn;
      addData.ldapDn = data.ldapDn;
      addData.no = this.no;
      if(data?.detailId) addData.detailId = data.detailId
      addData.valid = true;
    }
    else {
      addData.orderNo = (this.no) + 1;
      addData.ldapBaseDn = '';
      addData.ldapDn = '';
      addData.ldapUrl = '';
      addData.no = this.no;
      addData.valid = false;
    }
    this._ldapDataList.push(addData);

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.no;
    componentRef.instance.data = this._ldapDataList[this.no]
    componentRef.instance._disabled = this.disabled;
    componentRef.instance.action = this.action;
    this.no++;
    this.onChange(this._ldapDataList);

    componentRef.instance.change.subscribe((res:_ldapDataListDetail)=>{
      let idx = this._ldapDataList.findIndex(item=> item.no ===res.no)
      if(!idx && this._ldapDataList.length == 0){
        console.log('_ldapDataList.length = ', this._ldapDataList.length)
      }
      else{
        this._ldapDataList[idx].ldapBaseDn = res.ldapBaseDn;
        this._ldapDataList[idx].ldapDn = res.ldapDn;
        this._ldapDataList[idx].ldapUrl = res.ldapUrl;
        this._ldapDataList[idx].orderNo = res.orderNo;
        this._ldapDataList[idx].valid = res.valid;
      }

      this.onChange(this._ldapDataList);
    })

    componentRef.instance.remove.subscribe( no => {
      let idx = this._ldapDataList.findIndex(item=> item.no ===no)

      this._ldapDataList.splice(idx,1);

      if(this._ldapDataList.length == 0 ){
        this.no = 0 ;
        this.onChange([]);
        this.addldapData();

      }

    })

  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('接收disabled狀態', isDisabled)
    this.disabled = isDisabled;
  }

}
