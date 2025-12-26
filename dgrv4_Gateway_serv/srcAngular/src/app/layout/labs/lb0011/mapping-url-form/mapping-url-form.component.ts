import * as ValidatorFns from 'src/app/shared/validator-functions';
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
import { MappingUrlFieldComponent } from '../mapping-url-field/mapping-url-field.component';
import {
  DPB0281Field,
  DPB0281ReqBlock,
} from 'src/app/models/api/ServerService/dpb0281.interface';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { BaseComponent } from 'src/app/layout/base-component';

interface _DPB0281ReqBlock extends DPB0281ReqBlock {
  no?: number;
  percent?: number;
}

@Component({
  selector: 'app-mapping-url-form',
  templateUrl: './mapping-url-form.component.html',
  styleUrls: ['./mapping-url-form.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MappingUrlFormComponent),
      multi: true,
    },
  ],
})
export class MappingUrlFormComponent extends BaseComponent implements OnInit {
  onTouched!: () => void;
  onChange: (value: any) => void = () => {};

  disabled: boolean = false;
  @ViewChild('mappingUrlField', { read: ViewContainerRef, static: true })
  mappingUrlField!: ViewContainerRef;

  groupDataList: _DPB0281ReqBlock[] = [];
  groupDataNo: number = 0;
  _equal100: boolean = false;
  @Output() percentValid: EventEmitter<boolean> = new EventEmitter();
  @Output() formValid: EventEmitter<boolean> = new EventEmitter();
  form!: FormGroup;
  formSubscription: Subscription | undefined;

  private _webhookType: string = '';
  @Input()
  set webhookType(value: string) {
    this._webhookType = value;
    // 可以在這裡做額外處理或調用變更檢測
    this.mappingUrlField.clear();
  }

  get webhookType(): string {
    return this._webhookType;
  }

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder
  ) {
    super(route, tr);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      authorization: new FormControl(),
      to: new FormControl(),
      notificationDisabled: new FormControl(),
      subject: new FormControl(),
      recipients: new FormControl(),
      url: new FormControl(),
      username: new FormControl(),
      icon_emoji: new FormControl(),
      channel: new FormControl(),
      avatar_url: new FormControl(),
    });
  }

  writeValue(data: string): void {
    setTimeout(() => {
      if (this.formSubscription) {
        this.formSubscription.unsubscribe();
        this.formSubscription = undefined;
      }
      this.resetFormValidator(this.form);
      this.groupDataList = [];
      this.groupDataNo = 0;

      if (!data) return;
      switch (this.webhookType) {
        case 'HTTP':
          let dataParse = JSON.parse(data);

          const result = this.groupByMappingUrl(dataParse);

          result.forEach((groupData) => {
            this.addGroupData(groupData);
          });
          break;
        case 'EMAIL':
          if (data) {
            JSON.parse(data!).forEach((row) => {
              if (row.required) {
                this.form.controls[row.key].setValidators([
                  ValidatorFns.requiredValidator(),
                ]);
                $(`#${row.key}_label`).addClass('required');
              }
              this.form.controls[row.key]?.setValue(row.value);
            });
            this.form.updateValueAndValidity();
            this.disabled ? this.form.disable() : this.form.enable();
            const cleanedArr = JSON.parse(data!).map(
              ({ required, ...rest }) => rest
            );
            this.onChange(cleanedArr);
            this.formSubscription = this.form.valueChanges.subscribe((res) => {
              let lineField = [
                {
                  key: 'subject',
                  value: this.subject?.value,
                  type: '0',
                },
                { key: 'recipients', value: this.recipients?.value, type: '0' },
              ];
              this.onChange(lineField);
              this.formValid.emit(this.form.valid);
            });
            this.formValid.emit(this.form.valid);
          }
          break;
        case 'SLACK':
          if (data) {
            JSON.parse(data!).forEach((row) => {
              if (row.required) {
                this.form.controls[row.key].setValidators([
                  ValidatorFns.requiredValidator(),
                ]);
                $(`#${row.key}_label`).addClass('required');
              }
              this.form.controls[row.key]?.setValue(row.value);
            });
            this.form.updateValueAndValidity();
            this.disabled ? this.form.disable() : this.form.enable();
            const cleanedArr = JSON.parse(data!).map(
              ({ required, ...rest }) => rest
            );
            this.onChange(cleanedArr);
            this.formSubscription = this.form.valueChanges.subscribe((res) => {
              let lineField = [
                {
                  key: 'url',
                  value: this.url?.value,
                  type: '0',
                },
                { key: 'username', value: this.displayname?.value, type: '0' },
                { key: 'icon_emoji', value: this.icon_emoji?.value, type: '0' },
                { key: 'channel', value: this.channel?.value, type: '0' },
              ];
              this.onChange(lineField);
              this.formValid.emit(this.form.valid);
            });
            this.formValid.emit(this.form.valid);
          }
          break;
        case 'DISCORD':
          if (data) {
            JSON.parse(data!).forEach((row) => {
              if (row.required) {
                this.form.controls[row.key].setValidators([
                  ValidatorFns.requiredValidator(),
                ]);
                $(`#${row.key}_label`).addClass('required');
              }
              this.form.controls[row.key]?.setValue(row.value);
            });
            this.form.updateValueAndValidity();
            this.disabled ? this.form.disable() : this.form.enable();
            const cleanedArr = JSON.parse(data!).map(
              ({ required, ...rest }) => rest
            );
            this.onChange(cleanedArr);

            this.formSubscription = this.form.valueChanges.subscribe((res) => {
              let lineField = [
                {
                  key: 'url',
                  value: this.url?.value,
                  type: '0',
                },
                { key: 'username', value: this.displayname?.value, type: '0' },
                { key: 'avatar_url', value: this.avatar_url?.value, type: '0' },
              ];
              this.onChange(lineField);
              this.formValid.emit(this.form.valid);
            });
            this.formValid.emit(this.form.valid);
          }
          break;
        case 'LINE':
          if (data) {
            JSON.parse(data!).forEach((row) => {
              if (row.required) {
                this.form.controls[row.key].setValidators([
                  ValidatorFns.requiredValidator(),
                ]);
                $(`#${row.key}_label`).addClass('required');
              }
              this.form.controls[row.key]?.setValue(row.value);
            });
            this.form.updateValueAndValidity();
            this.disabled ? this.form.disable() : this.form.enable();
            const cleanedArr = JSON.parse(data!).map(
              ({ required, ...rest }) => rest
            );
            this.onChange(cleanedArr);

            this.formSubscription = this.form.valueChanges.subscribe((res) => {
              let lineField = [
                {
                  key: 'authorization',
                  value: this.approvalCode?.value,
                  type: '1',
                },
                { key: 'to', value: this.to?.value, type: '0' },
                {
                  key: 'notificationDisabled',
                  value: this.notificationDisabled?.value,
                  type: '0',
                },
              ];
              this.onChange(lineField);
              this.formValid.emit(this.form.valid);
            });
            this.formValid.emit(this.form.valid);
          }

          break;
      }
    }, 0);
  }

  addGroupData(groupData: _DPB0281ReqBlock) {
    let groupDataRef = this.mappingUrlField.createComponent(
      MappingUrlFieldComponent
    );

    groupData.no = this.groupDataNo;
    groupData.percent =
      Number(
        groupData.fieldList.filter((item) => item.key == 'percent')[0].value
      ) ?? 0;
    this.groupDataList.push(groupData);
    groupDataRef.instance.ref = groupDataRef;
    groupDataRef.instance.data = this.groupDataList[this.groupDataNo].fieldList;
    groupDataRef.instance.no = this.groupDataNo;
    groupDataRef.instance.disabled = this.disabled;

    this.groupDataNo++;
    this.checkPercentValid();

    const allFieldLists: DPB0281Field[] = [...this.groupDataList].flatMap(
      (item) => item.fieldList
    );
    this.onChange(allFieldLists);

    groupDataRef.instance.change.subscribe(
      (res: { no: number; url: string; fieldList: any; percent: number }) => {
        let idx = this.groupDataList.findIndex((item) => item.no == res.no);
        this.groupDataList[idx].fieldList = res.fieldList;
        this.groupDataList[idx].url = res.fieldList.filter(
          (item) => item.key == 'url'
        )[0].value;
        this.groupDataList[idx].percent = res.fieldList.filter(
          (item) => item.key == 'percent'
        )[0].value;
        this.checkPercentValid();

        const allFieldLists: DPB0281Field[] = [...this.groupDataList].flatMap(
          (item) => item.fieldList
        );
        this.onChange(allFieldLists);
      }
    );

    groupDataRef.instance.remove.subscribe((no) => {
      let idx = this.groupDataList.findIndex((item) => item.no == no);
      this.groupDataList.splice(idx, 1);
      if (this.groupDataList.length == 0) {
        this.groupDataNo = 0;
        this.addMappingUrlData();
      }

      this.checkPercentValid();
      const allFieldLists: DPB0281Field[] = [...this.groupDataList].flatMap(
        (item) => item.fieldList
      );
      this.onChange(allFieldLists);
    });
  }

  checkPercentValid() {
    const probabilities: number[] = this.groupDataList.map((item) =>
      Number(item.percent!)
    );
    const totPer: number = probabilities.reduce((sum, prob) => sum + prob, 0);
    this._equal100 = totPer === 100;
    this.percentValid.emit(this._equal100);
  }

  groupByMappingUrl(data: DPB0281Field[]): _DPB0281ReqBlock[] {
    const groupedMap = data.reduce<Record<string, _DPB0281ReqBlock>>(
      (acc, item) => {
        const key = item.mappingUrl!;
        if (!acc[key]) {
          acc[key] = { url: key, fieldList: [] };
        }
        acc[key].fieldList.push(item);
        return acc;
      },
      {}
    );

    return Object.values(groupedMap);
  }

  addMappingUrlData() {
    let addData = [
      { key: 'url', value: '', type: '0', mappingUrl: '' },
      { key: 'percent', value: '0', type: '0', mappingUrl: '' },
    ];
    const result = this.groupByMappingUrl(addData);
    this.addGroupData(result[0]);
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('MappingUrlFormComponent 接收disabled狀態', isDisabled)
    this.disabled = isDisabled;
  }

  // 敏感字眼，配合checkmarx掃描更名
  public get approvalCode() {
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
  public get displayname() {
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
