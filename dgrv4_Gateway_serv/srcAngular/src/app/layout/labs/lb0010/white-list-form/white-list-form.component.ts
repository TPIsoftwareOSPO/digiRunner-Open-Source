import { ToolService } from 'src/app/shared/services/tool.service';
import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  forwardRef,
  ViewChild,
  ViewContainerRef,
  Output,
  EventEmitter,
  Input,
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import * as base64 from 'js-base64';
import { WhiteListDetailComponent } from '../white-list-detail/white-list-detail.component';
import { DPB0233WhitelistItem } from 'src/app/models/api/ServerService/dpb0233.interface';

export interface _DPB0233WhitelistItem extends DPB0233WhitelistItem {
  no: number;
}

@Component({
  selector: 'app-white-list-form',
  templateUrl: './white-list-form.component.html',
  styleUrls: ['./white-list-form.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => WhiteListFormComponent),
      multi: true,
    },
  ],
})
export class WhiteListFormComponent implements OnInit {
  onTouched!: () => void;
  onChange!: (value: any) => void;

  @ViewChild('whitelistDetail', { read: ViewContainerRef, static: true })
  whitelistDetailRef!: ViewContainerRef;

  @Input() _disabled: boolean = false;

  _whiteList: Array<_DPB0233WhitelistItem> = [];
  hostnums: number = 0;

  constructor(private toolService: ToolService) {}

  ngOnInit(): void {}

  writeValue(whitelistItem?: Array<DPB0233WhitelistItem>): void {
    this.whitelistDetailRef.clear();
    this._whiteList = [];
    this.hostnums = 0;
    if (whitelistItem && whitelistItem.length>0) {
      whitelistItem.forEach((item) => {
        this.addWhitelistItem(item);
      });
    }
  }

  addWhitelistItem(item?: DPB0233WhitelistItem) {
    let componentRef = this.whitelistDetailRef.createComponent(
      WhiteListDetailComponent
    );
    if (item) {
      this._whiteList.push({ id: item.id, rule: item.rule, no: this.hostnums });
    } else {
      this._whiteList.push({ id: '', rule: '', no: this.hostnums });
    }

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.hostnums;
    componentRef.instance.data = this._whiteList[this.hostnums];
    this.hostnums++;

    componentRef.instance.change.subscribe((res: _DPB0233WhitelistItem) => {
      const idx = this._whiteList.findIndex((row) => row.no === res.no);
      if (!idx && this._whiteList.length == 0) {
        this._whiteList.push({ id: '', rule: '', no: this.hostnums });
      } else {
        this._whiteList[idx].no = res.no;
        this._whiteList[idx].id = res.id;
        this._whiteList[idx].rule = res.rule;
      }

      this.onChange(
        this._whiteList.map((item) => {
          return {
            id: item.id,
            rule: item.rule,
          };
        })
      );
    });

    componentRef.instance.remove.subscribe((no) => {
      const idx = this._whiteList.findIndex((row) => row.no === no);
      this._whiteList.splice(idx, 1);

      if (this._whiteList.length == 0) {
        this.hostnums = 0;
        this.onChange(null)
      } else {
        this.onChange(
          this._whiteList.map((item) => {
            return {
              id: item.id,
              rule: item.rule,
            };
          })
        );
      }
    });
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
}
