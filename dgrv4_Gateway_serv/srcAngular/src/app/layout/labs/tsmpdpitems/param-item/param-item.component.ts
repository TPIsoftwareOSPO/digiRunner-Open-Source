import { ToolService } from 'src/app/shared/services/tool.service';

import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import * as dayjs from 'dayjs';


@Component({
    selector: 'app-logitem',
    templateUrl: './param-item.component.html',
    styleUrls: ['./param-item.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ParamItemComponent implements OnInit {

    @Input() itemValue!: {index:number, value:string};
    @Output() change: EventEmitter<{index:number, value:string}> = new EventEmitter;

    form!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private tool: ToolService
    ) {}

    ngOnInit() {
        this.form = this.fb.group({
            index: new FormControl(null),
            value: new FormControl(null),
        });

        if(this.itemValue)
        {
            this.form.get('index')!.setValue(this.itemValue.index);
            this.form.get('value')!.setValue(this.itemValue.value);
        }

        this.form.valueChanges.subscribe(res => {

            this.change.emit({
                index: this.index.value,
                value: this.value.value,
            });

        });

    }

    public get index() { return this.form.get('index')!; };
    public get value() { return this.form.get('value')!; };

}
