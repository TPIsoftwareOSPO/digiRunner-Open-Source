import { KeyValueGridDetailComponent } from './key-value--grid-detail/key-value-grid-detail.component';
import { IKeyValueGrid } from './key-value-grid.interface';
import { Component, OnInit, ViewChild, ViewContainerRef, ComponentFactoryResolver, forwardRef, Input, ElementRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';


@Component({
  selector: 'app-key-value-grid',
  templateUrl: './key-value-grid.component.html',
  styleUrls: ['./key-value-grid.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => KeyValueGridComponent),
      multi: true
    }]
})
export class KeyValueGridComponent implements OnInit, ControlValueAccessor {


  onTouched!: () => void;
  onChange!: (value: any) => void;
  @Input() label: string = '';
  @Input() keyLabel: string = '';
  @Input() valueLabel: string = '';
  @ViewChild('content') content!: ElementRef;
  @ViewChild('keyvalue', { read: ViewContainerRef, static: true }) keyValueRef!: ViewContainerRef;


  disabled: boolean = false;
  nums: number = 0;
  keyvalues: Array<IKeyValueGrid> = new Array<IKeyValueGrid>();
  value: { key: string, value: any }[] = [];

  constructor(
    // private factoryResolver: ComponentFactoryResolver,
  ) {

  }

  writeValue(hosts: Array<{ key: string, value: any }>): void {
    this.value = hosts;
    if (this.value)
      this.value.forEach(val => this.add(val));
    else {
      if (this.value == null) {
        this.keyValueRef.clear();
        this.keyvalues = [];
      }
      this.add();
    }

  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (this.content) {
      if (isDisabled) {
        // $(this.content.nativeElement).find('input , button').prop('disabled', true).off('click');
        $(this.content.nativeElement).find('input , button').prop('disabled', true);
      } else {
        // $(this.content.nativeElement).find('input , button').prop('disabled', false).on('click');
        $(this.content.nativeElement).find('input , button').prop('disabled', false);
      }
    }
  }

  ngAfterViewInit(): void {
    if (this.disabled) {
      // $(this.content.nativeElement).find('input , button').prop('disabled', true).off('click');
      $(this.content.nativeElement).find('input , button').prop('disabled', true);
    } else {
      // $(this.content.nativeElement).find('input , button').prop('disabled', false).on('click');
      $(this.content.nativeElement).find('input , button').prop('disabled', false);
    }
  }



  ngOnInit() { }

  add(keyvalue?: { key: string, value: any  }) {
    // this.hostInputRef.clear();
    if (keyvalue && this.keyvalues.length) {
      this.keyvalues.push({ key: keyvalue.key, value: keyvalue.value, no: this.nums, selected:true });
    }
    else {
      this.keyvalues.push({ key: '', value: '', no: this.nums, selected:true});
    }
    // var componentFactory = this.factoryResolver.resolveComponentFactory(KeyValueDetailComponent);
    // this.keyValueRef.createComponent(KeyValueDetailComponent);

    let componentRef = this.keyValueRef.createComponent(KeyValueGridDetailComponent);
    componentRef.instance._ref = componentRef;
    componentRef.instance.keyLabel = this.keyLabel || 'Key';
    componentRef.instance.valueLabel = this.valueLabel || 'Value';
    componentRef.instance.no = this.nums;
    componentRef.instance.data = keyvalue;
    this.nums++;
    componentRef.instance.change.subscribe((res: IKeyValueGrid) => {
      // console.log(this.keyvalues);
      // console.log(res.no);
      let idx = this.keyvalues.findIndex(x => x.no === res.no);
      if (!idx && this.keyvalues.length == 0) {
        this.keyvalues.push({ key: '', value: '', no: this.nums })
      } else {
        let idx = this.keyvalues.findIndex(host => host.no === res.no);
        this.keyvalues[idx].key = res.key;
        this.keyvalues[idx].value = res.file? res.file : res.value;
        this.keyvalues[idx].no = res.no;
        this.keyvalues[idx].selected = res.selected;
      }
      let newKeyValue: Array<{ key: string, value: any }> = this.keyvalues.map(x => {
        return { key: x.key, value: x.value , selected: x.selected , }
      });
      this.onChange(newKeyValue);
    });
    componentRef.instance.remove.subscribe(no => {
      let idx = this.keyvalues.findIndex(host => host.no === no);
      this.keyvalues.splice(idx, 1);
      this.onChange(this.keyvalues);
      if (this.keyvalues.length == 0) {
        this.add();
      }
    });
  }

}
