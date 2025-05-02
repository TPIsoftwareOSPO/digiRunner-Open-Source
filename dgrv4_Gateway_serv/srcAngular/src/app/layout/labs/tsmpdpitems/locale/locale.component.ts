import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import * as dayjs from 'dayjs';
import { DPB9907Item } from 'src/app/models/api/ServerService/dpb9907.interface';

interface localeItem extends DPB9907Item {
  valid: boolean;
}

@Component({
    selector: 'app-logitem',
    templateUrl: './locale.component.html',
    styleUrls: ['./locale.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class LocaleComponent implements OnInit {

    @Input() itemValue!: DPB9907Item;
    @Output() change: EventEmitter<localeItem> = new EventEmitter;

    form!: FormGroup;

    constructor(
        private fb: FormBuilder,
    ) {

    }

    ngOnInit() {
        this.form = this.fb.group({
            locale: new FormControl(''),
            itemName: new FormControl('',ValidatorFns.requiredValidator()),
        });

        if(this.itemValue)
        {
            this.form.get('locale')!.setValue(this.itemValue.locale);
            this.form.get('itemName')!.setValue(this.itemValue.itemName);
        }

        this.form.valueChanges.subscribe(res => {

            this.change.emit({
                locale: this.locale.value,
                itemName: this.itemName.value,
                valid: this.form.valid
            });

        });

    }
    public get locale() { return this.form.get('locale')!; };
    public get itemName() { return this.form.get('itemName')!; };

}
