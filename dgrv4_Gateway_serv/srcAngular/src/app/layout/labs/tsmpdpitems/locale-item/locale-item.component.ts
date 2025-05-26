import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB9909Item } from './../../../../models/api/ServerService/dpb9909.interface';
import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import * as dayjs from 'dayjs';



@Component({
    selector: 'app-logitem',
    templateUrl: './locale-item.component.html',
    styleUrls: ['./locale-item.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class LocaleItemComponent implements OnInit {

    @Input() itemValue!: DPB9909Item;
    @Output() change: EventEmitter<DPB9909Item> = new EventEmitter;

    form!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private tool: ToolService
    ) {

    }

    ngOnInit() {
        this.form = this.fb.group({
            version: new FormControl(null),
            locale: new FormControl(null),
            subitemName: new FormControl(null),
        });

        if(this.itemValue)
        {
            console.log(this.itemValue)
            this.form.get('version')!.setValue(this.itemValue.version);
            this.form.get('locale')!.setValue(this.itemValue.locale);
            this.form.get('subitemName')!.setValue(this.itemValue.subitemName);
        }

        this.form.valueChanges.subscribe(res => {

            // this.change.emit({
            //     version: this.version.value,
            //     locale: this.locale.value,
            //     subitemName: this.subitemName.value
            // });

        });

    }

    public get version() { return this.form.get('version')!; };
    public get locale() { return this.form.get('locale')!; };
    public get subitemName() { return this.form.get('subitemName')!; };

}
