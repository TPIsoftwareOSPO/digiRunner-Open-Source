import { SourceIpFormDetailComponent } from './../source-ip-form-detail/source-ip-form-detail.component';
import { Component, OnInit, forwardRef, ViewChild, ViewContainerRef } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { AA0311RedirectByIpData } from 'src/app/models/api/ApiService/aa0311_v3.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';

export interface _AA0311RedirectByIpData extends AA0311RedirectByIpData {
  listNum: number;
  allowAll?:boolean;
}

@Component({
  selector: 'app-source-ip-form',
  templateUrl: './source-ip-form.component.html',
  styleUrls: ['./source-ip-form.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SourceIpFormComponent),
    multi: true
  }]
})
export class SourceIpFormComponent implements OnInit {

  onTouched!: () => void;
  onChange!: (value: any) => void;

  disabled: boolean = false;
  @ViewChild('ipDataList', { read: ViewContainerRef, static: true }) ipDataListRef!: ViewContainerRef;

  listNum: number = 0;
  isAllowAll:boolean = false;

  /**
   *  ipForRedirect: string;
   *  ipSrcUrl: string;
   *  listNum: number;
  */
  redirectByIpDataList: Array<_AA0311RedirectByIpData> = [];

  constructor(
    private alertService: AlertService,
    private translate: TranslateService,
    private toolService: ToolService
  ) { }

  ngOnInit(): void {

  }

  writeValue(redirectByIpDataList?: []): void {
    // console.log(redirectByIpDataList)

    this.initFormDetail(redirectByIpDataList);
  }

  initFormDetail(redirectByIpDataList?: []) {
    this.redirectByIpDataList = [];
    this.listNum = 0;
    this.ipDataListRef.clear();

    if(redirectByIpDataList && redirectByIpDataList.length>0){
      redirectByIpDataList.forEach(rowData=>{
        this.addIdData(rowData);
      })
      this.isAllowAll = this.redirectByIpDataList.some(res=> res.ipForRedirect.includes('0.0.0.0/0')||res.ipForRedirect.includes('::/0') )
    }
    else{
      this.addIdData();
    }

  }

  async addIdData(rowData?:AA0311RedirectByIpData,allowAll:boolean = false) {

    if (this.redirectByIpDataList.length == 5) {
      // 上限不可超過5
      const code = ['rowCountLimit'];
      const dict = await this.toolService.getDict(code, { num: 5 });
      this.alertService.ok(dict['rowCountLimit'], '');
      return;
    }
    let componentRef = this.ipDataListRef.createComponent(SourceIpFormDetailComponent);
    if(rowData){
      this.redirectByIpDataList.push({ ipForRedirect: rowData.ipForRedirect, ipSrcUrl: rowData.ipSrcUrl, listNum: this.listNum, allowAll:rowData.ipForRedirect.includes('0.0.0.0/0')||rowData.ipForRedirect.includes('::/0') })
    }
    else{
      if (allowAll) {
        this.redirectByIpDataList.push({
          ipForRedirect: '0.0.0.0/0',
          ipSrcUrl: '',
          listNum: this.listNum,
          allowAll: true,
        });
        this.isAllowAll = true;
      } else {
        this.redirectByIpDataList.push({
          ipForRedirect: '',
          ipSrcUrl: '',
          listNum: this.listNum,
        });
      }
    }

    componentRef.instance.ref = componentRef;
    componentRef.instance.no = this.listNum;
    componentRef.instance.data = this.redirectByIpDataList[this.listNum]
    componentRef.instance.disabled = this.disabled;
    this.listNum++;
    if(this.onChange) this.onChange( this.redirectByIpDataList.map(res=> {return {ipForRedirect:res.ipForRedirect, ipSrcUrl:res.ipSrcUrl }}))

    componentRef.instance.change.subscribe((res: { ipForRedirect: string, ipSrcUrl: string, no: number }) => {
      let idx = this.redirectByIpDataList.findIndex(x => x.listNum === res.no);

      this.redirectByIpDataList[idx].ipForRedirect = res.ipForRedirect;
      this.redirectByIpDataList[idx].ipSrcUrl = res.ipSrcUrl;
      this.redirectByIpDataList[idx].listNum = res.no;

      // console.log(this.redirectByIpDataList)
      this.isAllowAll = this.redirectByIpDataList.some(res=> res.ipForRedirect.includes('0.0.0.0/0')||res.ipForRedirect.includes('::/0') )
      this.onChange(this.redirectByIpDataList.map(res=> {return {ipForRedirect:res.ipForRedirect, ipSrcUrl:res.ipSrcUrl }}))

    })

    componentRef.instance.remove.subscribe(no => {
      let idx = this.redirectByIpDataList.findIndex(host => host.listNum === no);
      this.redirectByIpDataList.splice(idx, 1);
      this.listNum = this.redirectByIpDataList.length;
      if (this.redirectByIpDataList.length == 0) {
        this.listNum = 0;
        this.onChange([]);
        this.addIdData();
      }
      else{
        this.onChange(this.redirectByIpDataList.map(res=> {return {ipForRedirect:res.ipForRedirect, ipSrcUrl:res.ipSrcUrl }}))
      }
      this.isAllowAll = this.redirectByIpDataList.some(res=> res.ipForRedirect.includes('0.0.0.0/0')||res.ipForRedirect.includes('::/0') )

    })


  }


  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('HostnameConfigComponent 接收disabled狀態', isDisabled)
    this.disabled = isDisabled;
  }
}
