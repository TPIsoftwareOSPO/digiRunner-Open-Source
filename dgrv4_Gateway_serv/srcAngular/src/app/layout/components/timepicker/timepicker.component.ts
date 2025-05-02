import { Component, OnInit, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { isObject } from 'util';

@Component({
    selector: 'app-timepicker',
    templateUrl: './timepicker.component.html',
    styleUrls: ['./timepicker.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TimepickerComponent),
            multi: true
        }]
})
export class TimepickerComponent implements OnInit, ControlValueAccessor {

    onTouched!: () => void;
    onChange!: (value: any) => void;

    disabled?: boolean;
    form: FormGroup;

    constructor(
        private fb: FormBuilder,
        private tool: ToolService
    ) {
        this.form = this.fb.group({
            hour: new FormControl(''),
            minute: new FormControl('')
        });
    }

    writeValue(param: { hour: number, minute: number }): void {
        this.form.get('hour')!.setValue(isObject(param) ? this.tool.padLeft(param.hour, 2) : '');
        this.form.get('minute')!.setValue(isObject(param) ? this.tool.padLeft(param.minute, 2) : '');
    }

    registerOnChange(fn: (value: { hour: number, minute: number }) => void): void {
        this.onChange = fn
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        if (isDisabled) {
            this.form.get('hour')!.disable();
            this.form.get('minute')!.disable();
        }
        else {
            this.form.get('hour')!.enable();
            this.form.get('minute')!.enable();
        }
    }

    inputChange($event) {
        let value = $event.target.value;
        if ($event.target.name === 'hour') {
            if (parseInt(value) >= 24) value = '23';
            this.onChange({
                hour: parseInt(value),
                minute: parseInt(this.form.get('minute')!.value)
            });
            if (value != '') {
                this.form.get('hour')!.setValue(this.tool.padLeft(value, 2));
            }
        }
        else {
            if (parseInt(value) >= 60) value = '59';
            this.onChange({
                hour: parseInt(this.form.get('hour')!.value),
                minute: parseInt(value)
            });
            if (value != '') {
                this.form.get('minute')!.setValue(this.tool.padLeft(value, 2));
            }
        }
    }

    numberOnly(event): boolean {
        const charCode = (event.which) ? event.which : event.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57)) {
            return false;
        }
        return true;
    }

    ngOnInit() {
    }

}
