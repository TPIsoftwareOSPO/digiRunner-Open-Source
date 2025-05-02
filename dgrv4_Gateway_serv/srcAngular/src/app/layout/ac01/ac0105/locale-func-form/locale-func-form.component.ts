import {
  Component,
  OnInit,
  forwardRef,
  ViewChild,
  ViewContainerRef,
  Input,
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { AA0106ReqItem } from 'src/app/models/api/FuncService/aa0106.interface';
import { LocaleFuncDetailComponent } from '../locale-func-detail/locale-func-detail.component';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';

interface _AA0106ReqItem extends AA0106ReqItem {
  valid: boolean;
  no: number;
}

@Component({
  selector: 'app-locale-func-form',
  templateUrl: './locale-func-form.component.html',
  styleUrls: ['./locale-func-form.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => LocaleFuncFormComponent),
      multi: true,
    },
  ],
})
export class LocaleFuncFormComponent implements OnInit {
  @ViewChild('funcData', { read: ViewContainerRef, static: true })
  funcDataRef!: ViewContainerRef;

  onTouched!: () => void;
  onChange!: (value: any) => void;

  @Input() action: string = '';
  // @Input() localeData: { label: string; value: string }[] = [];

  // 用來接收 setDisabledState 的狀態
  disabled: boolean = false;

  no: number = 0;

  _funDataList: Array<_AA0106ReqItem> = [];
  localeData: { label: string; value: string }[] = [];

  constructor(
    private listService: ListService,
    private toolService: ToolService
  ) {}

  ngOnInit(): void {}

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  writeValue(data?: [] | String): void {
    this.funcDataRef.clear();
    this._funDataList = [];
    this.no = 0;
    let ReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('RTN_CODE_LOCALE')
        ) +
        ',' +
        22,
      isDefault: 'N',
    } as DPB0047Req;
    this.listService.querySubItemsByItemNo(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.localeData = res.RespBody.subItems
          ? res.RespBody.subItems?.map((item) => {
              return {
                label: item.subitemName,
                value: item.subitemNo,
              };
            })
          : [];
      }
    });
    if (data == '') {
      this.addFuncData();
    }
  }

  addFuncData(data?: AA0106ReqItem) {
    let componentRef = this.funcDataRef.createComponent(
      LocaleFuncDetailComponent
    );
    let addData = {} as _AA0106ReqItem;
    if (data) {
      // addData.orderNo = data.orderNo;
      // addData.ldapUrl = data.ldapUrl;
      // addData.ldapBaseDn = data.ldapBaseDn;
      // addData.ldapDn = data.ldapDn;
      // addData.no = this.no;
      // if(data?.detailId) addData.detailId = data.detailId
      // addData.valid = true;
    } else {
      addData.funcDesc = '';
      addData.funcName = '';
      addData.locale = '';
      addData.no = this.no;
      addData.valid = false;
    }
    this._funDataList.push(addData);

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.no;
    componentRef.instance.data = this._funDataList[this.no];
    componentRef.instance.localeData = this.localeData;

    this.onChange('');
    this.no++;

    componentRef.instance.change.subscribe((res: _AA0106ReqItem) => {
      let idx = this._funDataList.findIndex((item) => item.no === res.no);

      if (this._funDataList.length >= 0) {
        this._funDataList[idx].funcDesc = res.funcDesc;
        this._funDataList[idx].funcName = res.funcName;
        this._funDataList[idx].locale = res.locale;
        this._funDataList[idx].valid = res.valid;
      }

      if (this._funDataList.every((item) => item.valid === true)) {
        this.onChange(this._funDataList);
      } else {
        this.onChange('');
      }
    });

    componentRef.instance.remove.subscribe((no) => {
      let idx = this._funDataList.findIndex((item) => item.no === no);

      this._funDataList.splice(idx, 1);

      if (this._funDataList.length == 0) {
        this.no = 0;
        this.onChange('');
        this.addFuncData();
      }
      else{
        this.onChange(this._funDataList)
      }
    });
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('接收disabled狀態', isDisabled)
    this.disabled = isDisabled;
  }
}
