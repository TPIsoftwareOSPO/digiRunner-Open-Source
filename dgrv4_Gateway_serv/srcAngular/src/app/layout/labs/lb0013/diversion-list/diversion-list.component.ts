import {
  Component,
  EventEmitter,
  OnInit,
  Output,
  ViewChild,
  ViewContainerRef,
  forwardRef,
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { DPB0201RespItem } from 'src/app/models/api/ServerService/dpb0201.interface';
import { DiversionListRowComponent } from '../diversion-list-row/diversion-list-row.component';
import { SmartOnFhirProxyDiversionDto } from 'src/app/models/api/ServerService/dpb0311.interface';

interface _diversionList extends SmartOnFhirProxyDiversionDto {
  no: number;
  rowValid: boolean;
}

@Component({
  selector: 'app-diversion-list',
  templateUrl: './diversion-list.component.html',
  styleUrls: ['./diversion-list.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DiversionListComponent),
      multi: true,
    },
  ],
  standalone: false,
})
export class DiversionListComponent implements OnInit {
  onTouched!: () => void;
  onChange!: (value: any) => void;
  disabled: boolean = false;

  @ViewChild('diversionList', { read: ViewContainerRef, static: true })
  diversionListRef!: ViewContainerRef;

  _equal100: boolean = false;

  _diversionList: _diversionList[] = [];
  hostnums: number = 0;

  @Output() formValid: EventEmitter<boolean> = new EventEmitter();

  constructor() {}

  ngOnInit(): void {}

  addRow(rowData?: {
    sofProxyId?: string;
    sofProxyDiversionId?: string;
    sofProxyDiversionProbability: number;
    sofProxyDiversionUrl: string;
    version?: string;
  }) {
    let componentRef = this.diversionListRef.createComponent(DiversionListRowComponent);
    if (rowData) {
      this._diversionList.push({
        sofProxyDiversionId: rowData.sofProxyDiversionId,
        sofProxyId: rowData.sofProxyId,
        sofProxyDiversionProbability: rowData.sofProxyDiversionProbability,
        sofProxyDiversionUrl: rowData.sofProxyDiversionUrl,
        version: rowData.version,
        no: this.hostnums,
        rowValid: true,
      });
    } else {
      this._diversionList.push({
        sofProxyDiversionProbability: 0,
        sofProxyDiversionUrl: '',
        no: this.hostnums,
        rowValid: false,
      });
      this.formValid.emit(false);
    }

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.hostnums;
    componentRef.instance.data = this._diversionList[this.hostnums];
    componentRef.instance.readonly = this.disabled;

    this.hostnums++;
    componentRef.instance.change.subscribe(
      (res: {
        sofProxyDiversionProbability: number;
        sofProxyDiversionUrl: string;
        no: number;
        rowValid: boolean;
      }) => {
        let idx = this._diversionList.findIndex((x) => x.no === res.no);

        if (!idx && this._diversionList.length == 0) {
          this._diversionList.push({
            sofProxyDiversionProbability: 100,
            sofProxyDiversionUrl: '',
            no: this.hostnums,
            rowValid: false,
          });
        } else {
          let idx = this._diversionList.findIndex((host) => host.no === res.no);
          this._diversionList[idx].sofProxyDiversionProbability =
            res.sofProxyDiversionProbability;
          this._diversionList[idx].sofProxyDiversionUrl =
            res.sofProxyDiversionUrl;
          this._diversionList[idx].no = res.no;
          this._diversionList[idx].rowValid = res.rowValid;
        }

        this.onChange(this._diversionList);
        let totPer: number = 0;
        let rowValid = this._diversionList.every((item) => item.rowValid);
        this._diversionList.forEach((item) => {
          totPer += item.sofProxyDiversionProbability!;
        });
        this._equal100 = totPer == 100;
        this.formValid.emit(rowValid && this._equal100);
      }
    );
    componentRef.instance.remove.subscribe((no) => {
      let idx = this._diversionList.findIndex((host) => host.no === no);
      this._diversionList.splice(idx, 1);

      if (this._diversionList.length == 0) {
        this.hostnums = 0;
        this.onChange([]);
        this.addRow();
      } else {
        this.onChange(this._diversionList);
      }
    });
  }

  writeValue(data: any): void {
    this.hostnums = 0;
    this._diversionList = [];
    this.diversionListRef.clear();

    if (data && data.length > 0) {
      data.forEach((rowData) => {
        let addItem = {
          sofProxyDiversionProbability: rowData.sofProxyDiversionProbability,
          sofProxyDiversionUrl: rowData.sofProxyDiversionUrl,
          sofProxyDiversionId: rowData.sofProxyDiversionId,
          sofProxyId: rowData.sofProxyId,
          version: rowData.version,
        };

        this.addRow(addItem);
      });
      this.formValid.emit(true);
    } else {
      this.addRow({
        sofProxyDiversionProbability: 100,
        sofProxyDiversionUrl: '',
      });
      this._equal100 = true;
    }
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('接收disabled狀態', isDisabled)
    this.disabled = isDisabled;
  }
}
