import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB9909Item } from './../../../../models/api/ServerService/dpb9909.interface';
import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import * as dayjs from 'dayjs';



@Component({
    selector: 'app-logitem',
    templateUrl: './locale-item.component.html',
    styleUrls: ['./locale-item.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})

export class LocaleItemComponent implements OnInit {

    @Input() itemValue!: DPB9909Item;
    @Output() change: EventEmitter<DPB9909Item> = new EventEmitter;

    form!: UntypedFormGroup;

    constructor(
        private fb: UntypedFormBuilder,
        private tool: ToolService
    ) {

    }

    ngOnInit() {
        this.form = this.fb.group({
            version: new UntypedFormControl(null),
            locale: new UntypedFormControl(null),
            subitemName: new UntypedFormControl(null),
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
