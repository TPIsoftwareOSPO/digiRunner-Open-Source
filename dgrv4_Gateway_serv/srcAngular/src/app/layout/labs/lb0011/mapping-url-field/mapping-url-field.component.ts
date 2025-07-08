import {
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { DPB0281Field } from 'src/app/models/api/ServerService/dpb0281.interface';
import { KeyValueFieldComponent } from '../key-value-field/key-value-field.component';

interface _keyValueField extends DPB0281Field {
  no: number;
  valid?: boolean;
  required?: boolean;
}

@Component({
  selector: 'app-mapping-url-field',
  templateUrl: './mapping-url-field.component.html',
  styleUrls: ['./mapping-url-field.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MappingUrlFieldComponent),
      multi: true,
    },
  ],
})
export class MappingUrlFieldComponent implements OnInit {
  @ViewChild('headerValue', { read: ViewContainerRef, static: false })
  headerValueRef!: ViewContainerRef;
  @ViewChild('fieldValue', { read: ViewContainerRef, static: false })
  fieldValueRef!: ViewContainerRef;

  onTouched!: () => void;
  onChange: (value: any) => void = () => {};

  @Input() data?: DPB0281Field[];
  @Input() ref: any;
  @Input() no: number = 0;
  @Input() disabled: boolean = false;

  @Output() change: EventEmitter<any> = new EventEmitter();
  @Output() remove: EventEmitter<number> = new EventEmitter();

  form!: FormGroup;
  headerValueList: Array<_keyValueField> = [];
  headerNo: number = 0;
  fieldValueList: Array<_keyValueField> = [];
  fieldNo: number = 0;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      url: new FormControl(''),
      percent: new FormControl(0),
    });

    this.disabled? this.form.disable(): this.form.enable();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.data?.forEach((item) => {
        if (item?.type == '1') {
          this.addHeaderValue(item);
        } else {
          this.addFieldValue(item);
        }
      });

      this.form.valueChanges.subscribe((res) => {
        this.asyncData();
      });
    }, 0);
  }

  deleteItem() {
    this.ref.destroy();
    this.remove.emit(this.no);
  }

  // HTTP模式 增加header欄位
  addHeaderValue(data?: { key: string; value: string }) {
    let headerRef = this.headerValueRef.createComponent(KeyValueFieldComponent);
    let addKeyValue = {
      key: data ? data.key : '',
      value: data ? data.value : '',
      no: this.headerNo,
    } as _keyValueField;

    this.headerValueList.push(addKeyValue);

    headerRef.instance._ref = headerRef;
    headerRef.instance.no = this.headerNo;
    headerRef.instance.data = this.headerValueList[this.headerNo];
    headerRef.instance._disabled = this.disabled;
    this.headerNo++;
    this.asyncData();

    headerRef.instance.change.subscribe((res: _keyValueField) => {
      let idx = this.headerValueList.findIndex((item) => item.no === res.no);
      this.headerValueList[idx].key = res.key;
      this.headerValueList[idx].value = res.value;
      if (res.valid != undefined) this.headerValueList[idx].valid = res.valid;
      this.asyncData();
    });

    headerRef.instance.remove.subscribe((no) => {
      let idx = this.headerValueList.findIndex((item) => item.no === no);

      this.headerValueList.splice(idx, 1);
      this.asyncData();
    });
  }
  // HTTP模式 增加Field欄位
  addFieldValue(data?: { key: string; value: string }) {
    if (data?.key == 'url') {
      this.url?.setValue(data?.value);
      this.asyncData();
    } else if (data?.key == 'percent') {
      this.percent?.setValue(data?.value);
      this.asyncData();
    } else {
      let fieldRef = this.fieldValueRef.createComponent(KeyValueFieldComponent);
      let addKeyValue = {
        key: data ? data.key : '',
        value: data ? data.value : '',
        no: this.fieldNo,
      } as _keyValueField;

      this.fieldValueList.push(addKeyValue);

      fieldRef.instance._ref = fieldRef;
      fieldRef.instance.no = this.fieldNo;
      fieldRef.instance.data = this.fieldValueList[this.fieldNo];
      fieldRef.instance._disabled = this.disabled;
      this.fieldNo++;

      this.asyncData();

      fieldRef.instance.change.subscribe((res: _keyValueField) => {
        let idx = this.fieldValueList.findIndex((item) => item.no === res.no);
        this.fieldValueList[idx].key = res.key;
        this.fieldValueList[idx].value = res.value;
        if (res.valid != undefined) this.fieldValueList[idx].valid = res.valid;
        this.asyncData();
      });

      fieldRef.instance.remove.subscribe((no) => {
        let idx = this.fieldValueList.findIndex((item) => item.no === no);
        this.fieldValueList.splice(idx, 1);
        this.asyncData();
      });
    }
  }

  asyncData() {
    let mergeList = [
      ...this.headerValueList.map((item) => ({
        ...item,
        type: '1',
        mappingUrl: this.url!.value,
      })),
      ...this.fieldValueList.map((item) => ({
        ...item,
        type: '0',
        mappingUrl: this.url!.value,
      })),
    ];

    const cleanedArr = mergeList.map(
      ({ no, valid, required, ...rest }) => rest
    );

    cleanedArr.push(
      {
        key: 'url',
        value: this.url!.value,
        type: '0',
        mappingUrl: this.url!.value,
      },
      {
        key: 'percent',
        value: String(this.percent!.value),
        type: '0',
        mappingUrl: this.url!.value,
      }
    );

    this.change.emit({
      no: this.no,
      fieldList: cleanedArr,
    });
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  public get url() {
    return this.form.get('url');
  }
  public get percent() {
    return this.form.get('percent');
  }
}
