import * as ValidatorFns from 'src/app/shared/validator-functions';
import {
  Component,
  OnInit,
  forwardRef,
  ViewChild,
  ViewContainerRef,
  Input,
  AfterViewInit,
  EventEmitter,
  Output,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { KeyValueFieldComponent } from '../key-value-field/key-value-field.component';
import { Subscription } from 'rxjs';
import { BaseComponent } from 'src/app/layout/base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';

interface _keyValueField {
  key: string;
  value: string;
  no: number;
  valid?: boolean;
  required?: boolean;
}

@Component({
  selector: 'app-key-value-form',
  templateUrl: './key-value-form.component.html',
  styleUrls: ['./key-value-form.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => KeyValueFormComponent),
      multi: true,
    },
  ],
})
export class KeyValueFormComponent extends BaseComponent implements OnInit {
  // @ViewChild('keyValue', { read: ViewContainerRef, static: false }) keyValueRef!: ViewContainerRef;
  @ViewChild('headerValue', { read: ViewContainerRef, static: true })
  headerValueRef!: ViewContainerRef;
  @ViewChild('fieldValue', { read: ViewContainerRef, static: true })
  fieldValueRef!: ViewContainerRef;

  onTouched!: () => void;
  onChange!: (value: any) => void;
  @Output() formValid: EventEmitter<boolean> = new EventEmitter();

  disabled: boolean = false;

  headerValueList: Array<_keyValueField> = [];
  headerNo: number = 0;
  fieldValueList: Array<_keyValueField> = [];
  fieldNo: number = 0;

  form!: FormGroup;
  formSubscription: Subscription | undefined;
  private _webhookType: string = '';
  @Input()
  set webhookType(value: string) {
    this._webhookType = value;
    // 可以在這裡做額外處理或調用變更檢測
  }

  get webhookType(): string {
    return this._webhookType;
  }

  @Input() required: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder
  ) {
    super(route, tr);
  }

  ngOnInit(): void {}

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

  writeValue(data?: string): void {
    setTimeout(() => {
      if (this.formSubscription) {
        this.formSubscription.unsubscribe();
        this.formSubscription = undefined;
      }

      if (this.headerValueRef) this.headerValueRef.clear();
      if (this.fieldValueRef) this.fieldValueRef.clear();
      this.headerValueList = [];
      this.fieldValueList = [];
      this.headerNo = this.fieldNo = 0;
      if (!this.disabled) {
        this.form.controls['url'].setValidators([
          ValidatorFns.requiredValidator(),
        ]);
        $(`#url_label`).addClass('required');
      }

      this.formSubscription = this.form.valueChanges.subscribe((res) => {
        this.asyncData();
        this.formValid.emit(this.form.valid);
      });

      this.disabled ? this.url?.disable() : this.url?.enable();

      if (!data) return;
      // console.log(data)
      if (data !== '') {
        let tmpData = JSON.parse(data);
        if (Array.isArray(tmpData)) {
          tmpData.forEach((item) => {
            if (this.webhookType == 'HTTP') {
              if (item?.type == '1') {
                this.addHeaderValue(item);
              } else {
                this.addFieldValue(item);
              }
            }
          });
        }
      }
    }, 0);
  }

  isValidJSON(str: string) {
    try {
      JSON.parse(str);
      return true;
    } catch (e) {
      return false;
    }
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
      // console.log(data)
      this.url?.setValue(data?.value);
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
      ...this.headerValueList.map((item) => ({ ...item, type: '1' })),
      ...this.fieldValueList.map((item) => ({ ...item, type: '0' })),
    ];
    let _valid = mergeList
      .filter((x) => {
        return typeof x.valid !== 'undefined';
      })
      .every((item) => item.valid);
    this.formValid.emit(_valid);

    const cleanedArr = mergeList.map(
      ({ no, valid, required, ...rest }) => rest
    );

    this.onChange(cleanedArr);
  }

  public get authorization() {
    return this.form.get('authorization');
  }
  public get to() {
    return this.form.get('to');
  }
  public get notificationDisabled() {
    return this.form.get('notificationDisabled');
  }
  public get subject() {
    return this.form.get('subject');
  }
  public get recipients() {
    return this.form.get('recipients');
  }
  public get url() {
    return this.form.get('url');
  }
  public get username() {
    return this.form.get('username');
  }
  public get icon_emoji() {
    return this.form.get('icon_emoji');
  }
  public get channel() {
    return this.form.get('channel');
  }
  public get avatar_url() {
    return this.form.get('avatar_url');
  }
}
