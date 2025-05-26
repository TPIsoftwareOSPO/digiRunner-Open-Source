import { Component, OnInit, forwardRef, ViewChild, ViewContainerRef, ElementRef, ComponentFactoryResolver, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ScheduleContentFormComponent } from '../schedule-content-form/schedule-content-form.component';
import { DPB0101ItemsAddNo, DPB0101Items } from 'src/app/models/api/CycleScheduleService/dpb0101.interface';

@Component({
  selector: 'app-schedule-content',
  templateUrl: './schedule-content.component.html',
  styleUrls: ['./schedule-content.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => ScheduleContentComponent),
    multi: true
  }]
})
export class ScheduleContentComponent implements OnInit {

  @ViewChild('scheduleContent', { static: true, read: ViewContainerRef }) scheduleContentRef!: ViewContainerRef;
  @ViewChild('content') content!: ElementRef;
  @Input() action: string = '';
  // @Output() valueChange = new EventEmitter;
  onTouched = () => { };
  onChange = (value: any) => { };

  _scheduleNo: number = 0;
  scheduleItems: Array<DPB0101ItemsAddNo> = new Array<DPB0101ItemsAddNo>();
  value: Array<DPB0101Items> = [];

  constructor(
    // private factoryResolver: ComponentFactoryResolver,
  ) { }

  ngOnInit() {

  }

  writeValue(_scheduleItems: Array<any>): void {
    this.value = _scheduleItems;

    if (this.value.length > 0) {
      this.value.forEach(val => this.addScheduleContent(val));
    }
    else {
      this.addScheduleContent();
    }
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {

  }

  addScheduleContent(_scheduleItems?: any) {
    // var componentFactory = this.factoryResolver.resolveComponentFactory(ScheduleContentFormComponent);
    let componentRef = this.scheduleContentRef.createComponent(ScheduleContentFormComponent);
    if (_scheduleItems) {
      this.scheduleItems.push({ refItemNo: _scheduleItems.refItemNo, refSubitemNo: _scheduleItems.refSubitemNo, inParams: _scheduleItems.inParams, identifData: _scheduleItems.identifData, sortBy: _scheduleItems.sortBy, no: this._scheduleNo });
    }
    else {
      this.scheduleItems.push({ refItemNo: '', refSubitemNo: '', inParams: '', identifData: '', sortBy: 0, no: this._scheduleNo });
    }
    componentRef.instance.rowcount = this.scheduleItems.length;
    componentRef.instance._ref = componentRef;
    componentRef.instance._no = this._scheduleNo;
    componentRef.instance.data = _scheduleItems;
    componentRef.instance.action = this.action;
    this._scheduleNo++;
    componentRef.instance.change.subscribe((res: DPB0101ItemsAddNo) => {
      let idx = this.scheduleItems.findIndex(x => x.no === res.no);
      if (!idx && this.scheduleItems.length == 0) {
        this.scheduleItems.push({ refItemNo: '', refSubitemNo: '', inParams: '', identifData: '', sortBy: 0, no: this._scheduleNo });
      } else {
        let idx = this.scheduleItems.findIndex(item => item.no === res.no);
        this.scheduleItems[idx].refItemNo = res.refItemNo;
        this.scheduleItems[idx].refSubitemNo = res.refSubitemNo;
        this.scheduleItems[idx].inParams = res.inParams;
        this.scheduleItems[idx].identifData = res.identifData;
        this.scheduleItems[idx].sortBy = res.sortBy;
        this.scheduleItems[idx].no = res.no;
        this.scheduleItems[idx].isValid = res.isValid;

      }
      let newScheduleItems: DPB0101Items[] = this.scheduleItems.map(item => {
        return { refItemNo: item.refItemNo, refSubitemNo: item.refSubitemNo, identifData: item.identifData, sortBy: item.sortBy, inParams: item.inParams, isValid: item.isValid }
      });
      this.onChange(newScheduleItems);
    });

    componentRef.instance.remove.subscribe(no => {
      if (this.scheduleItems.length == 1) {
        componentRef.instance._ref.destroy();
        let idx = this.scheduleItems.findIndex(host => host.no === no);
        this.scheduleItems.splice(idx, 1);
        this.onChange(this.scheduleItems);
        this.addScheduleContent();
      }
      else {
        componentRef.instance._ref.destroy();
        let idx = this.scheduleItems.findIndex(host => host.no === no);
        this.scheduleItems.splice(idx, 1);
        this.onChange(this.scheduleItems);
      }
    });
  }

}
