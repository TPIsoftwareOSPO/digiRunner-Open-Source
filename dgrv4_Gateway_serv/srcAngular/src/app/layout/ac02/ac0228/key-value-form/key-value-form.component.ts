import { Component, OnInit, forwardRef, ViewChild, ViewContainerRef, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { KeyValueFieldComponent } from '../key-value-field/key-value-field.component';

interface _keyValueField {
  key: string,
  value: string,
  no: number
}

@Component({
  selector: 'app-key-value-form',
  templateUrl: './key-value-form.component.html',
  styleUrls: ['./key-value-form.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => KeyValueFormComponent),
    multi: true
  }]
})
export class KeyValueFormComponent implements OnInit {
  @ViewChild('keyValue', { read: ViewContainerRef, static: true }) keyValueRef!: ViewContainerRef;

  onTouched!: () => void;
  onChange!: (value: any) => void;

  disabled: boolean = false;
  keyValueList: Array<_keyValueField> = [];
  no: number = 0;

  @Input() required: boolean = false;

  // keyValueList: { [key: string]: string }[] = [];

  constructor() { }

  ngOnInit(): void {
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

  writeValue(data?: string): void {
    // console.log(this.action)


    // console.log('is Array', Array.isArray(data))
    if (this.keyValueRef) this.keyValueRef.clear();
    this.keyValueList = [];
    this.no = 0;
    if (!data) return;
    if (data == '') {
      // this.addKeyValue();
    }
    else {
      if (this.isValidJSON(data)) {


        const kvObj = JSON.parse(data);
        if (Array.isArray(kvObj)) {
          kvObj.forEach(item => {
            Object.keys(item).map(key => {
              // console.log(key, item[key])
              let kvField = {
                key: key,
                value: item[key]
              }
              this.addKeyValue(kvField, true)
            })
          });
        }
      }
    }
  }

  isValidJSON(str: string) {
    try {
      JSON.parse(str);
      return true;
    } catch (e) {
      return false;
    }
  }

  addKeyValue(data?: { key: string, value: string }, mutiplt: boolean = false) {
    let componentRef = this.keyValueRef.createComponent(KeyValueFieldComponent);
    let addKeyValue = {
      key: data ? data.key : '',
      value: data ? data.value : '',
      no: this.no
    } as _keyValueField;


    this.keyValueList.push(addKeyValue);

    if (!mutiplt) {
      let parseKV = this.keyValueList.map(item => {
        let tmp = {};
        tmp[item.key] = item.value
        return tmp;
      })
      this.onChange(JSON.stringify(parseKV))
    }

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.no;
    componentRef.instance.data = this.keyValueList[this.no]
    componentRef.instance._disabled = this.disabled;
    componentRef.instance.required = this.required;

    this.no++;


    // if (this.keyValueList.length === 1) {
    //   let parseKV = this.keyValueList.map(item => {
    //     let tmp = {};
    //     tmp[item.key] = item.value
    //     return tmp;
    //   })
    //   this.onChange(JSON.stringify(parseKV))
    // }


    componentRef.instance.change.subscribe((res: _keyValueField) => {
      let idx = this.keyValueList.findIndex(item => item.no === res.no)
      this.keyValueList[idx].key = res.key;
      this.keyValueList[idx].value = res.value;

      let parseKV = this.keyValueList.map(item => {
        let tmp = {};
        tmp[item.key] = item.value
        return tmp;
      })
      this.onChange(JSON.stringify(parseKV))
    })

    componentRef.instance.remove.subscribe(no => {
      let idx = this.keyValueList.findIndex(item => item.no === no)

      this.keyValueList.splice(idx, 1);

      if (this.keyValueList.length == 0) {
        this.no = 0;
        this.onChange(null);
        // this.addKeyValue();
      }
      let parseKV = this.keyValueList.map(item => {
        let tmp = {};
        tmp[item.key] = item.value
        return tmp;
      })
      this.onChange(JSON.stringify(parseKV))

    })

  }



}
