import { Component, OnInit, forwardRef, Input } from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  FormGroup,
  FormBuilder,
  FormControl,
} from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { ToolService } from 'src/app/shared/services/tool.service';

interface checkbox {
  label: string;
  value: number;
  checked: boolean;
  indeterminate: boolean;
}
@Component({
  selector: 'app-datepicker',
  templateUrl: './datepicker.component.html',
  styleUrls: ['./datepicker.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatepickerComponent),
      multi: true,
    },
  ],
})
export class DatepickerComponent implements OnInit, ControlValueAccessor {
  dateList: checkbox[] = [];
  _dateType = '';
  @Input()
  get dateType(): string {
    return this._dateType;
  }

  set dateType(value: string) {
    this._dateType = value;
    switch (this._dateType) {
      case 'week':
        const code = [
          'week_option.mon',
          'week_option.tue',
          'week_option.wed',
          'week_option.thu',
          'week_option.fri',
          'week_option.sat',
          'week_option.sun',
        ];
        this.translate.get(code).subscribe((dict) => {
          this.dateList = [
            {
              label: dict['week_option.mon'],
              value: 1,
              checked: false,
              indeterminate: false,
            },
            {
              label: dict['week_option.tue'],
              value: 2,
              checked: false,
              indeterminate: false,
            },
            {
              label: dict['week_option.wed'],
              value: 3,
              checked: false,
              indeterminate: false,
            },
            {
              label: dict['week_option.thu'],
              value: 4,
              checked: false,
              indeterminate: false,
            },
            {
              label: dict['week_option.fri'],
              value: 5,
              checked: false,
              indeterminate: false,
            },
            {
              label: dict['week_option.sat'],
              value: 6,
              checked: false,
              indeterminate: false,
            },
            {
              label: dict['week_option.sun'],
              value: 0,
              checked: false,
              indeterminate: false,
            },
          ];
        });
        break;
      case 'month':
        for (let i = 1; i <= 31; i++) {
          this.dateList.push({
            label: i.toString(),
            value: i,
            checked: false,
            indeterminate: false,
          });
          // console.log(this.dateList);
        }
        break;

      default:
        break;
    }
  }

  get AllChecked() {
    return this.dateList.every((_) => _.checked);
  }
  get AllIndeterminate() {
    return !this.AllChecked && this.dateList.some((_) => _.checked);
  }
  allSelect() {
    const isAllChecked = this.AllChecked;
    this.dateList.forEach((_) => (_.checked = !isAllChecked));
    this.emitDataChange();
  }

  onTouched!: () => void;
  onChange!: (value: any) => void;

  disabled?: boolean;
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private tool: ToolService,
    private translate: TranslateService
  ) {
    this.form = this.fb.group({
      date: new FormControl([]),
    });
  }

  writeValue(param: Array<number>): void {
    if (param) {
      this.dateList = this.dateList.map((item) => ({
        ...item,
        checked: param.includes(item.value),
      }));
    }
  }

  registerOnChange(fn: (value: { date: [] }) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  ngOnInit() {

  }

  emitDataChange() {
    const checkedValues = this.dateList
      .filter((item) => item.checked)
      .map((item) => item.value);
    this.onChange(checkedValues);
  }
}
