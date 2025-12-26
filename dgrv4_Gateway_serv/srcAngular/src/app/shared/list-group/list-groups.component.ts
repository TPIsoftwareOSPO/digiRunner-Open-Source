import { FormGroup, FormBuilder, FormControl, FormArray } from '@angular/forms';
import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy, SimpleChange, OnChanges, AfterViewInit } from '@angular/core';
import { Menu } from 'src/app/models/menu.model';
import { ListGroupComponent } from './list-group.component';

@Component({
    selector: 'app-list-groups',
    templateUrl: './list-groups.component.html',
    styleUrls: ['./list-group.component.css'],
    // changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListGroupsComponent extends ListGroupComponent implements OnInit, OnChanges, AfterViewInit {

    /**
     * 處理API清單，用於群組設定API權限
     */

    @Input() Search: string = "";
    @Input() override disableCheckbox: boolean = false;

    constructor(
         fb: FormBuilder
    ) {
        super(fb);
        this.form = this.fb.group([]);
        console.log('list groups')
    }

    ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
        if (changes['disableCheckbox'])
            Object.keys(this.form.controls).forEach(m => {
                if (changes['disableCheckbox'].currentValue)
                    this.form.controls[m].disable();
                else
                    this.form.controls[m].enable();
            })
    }

    ngAfterViewInit(): void {
        this.updateHandler.emit(this.form.value);
    }

   override ngOnInit() {
    console.log('list groups init')
        if (this.menus) {
            this.newMenus = JSON.parse(JSON.stringify(this.menus));
            this.newMenus?.forEach((newMenu, i) => {
                this.form.addControl(newMenu.main.replace(/-/g, '|'), new FormControl({ value: false, disabled: this.disableCheckbox }));
                newMenu.subs?.forEach((subMenu, j) => {
                    let defaultChecked = this.selected.findIndex(sel => sel.moduleName === newMenu.main && sel.apiKey === subMenu.value) != -1;
                    this.form.addControl(newMenu.main.replace(/-/g, '|') + '-' + j, new FormControl({ value: defaultChecked, disabled: this.disableCheckbox }));
                })
            });
            Object.keys(this.form.controls).filter(ctl => ctl.split('-').length == 1).forEach(m => {
                let defMainChecked = this.checkedGroup(m);
                this.form.controls[m].setValue(defMainChecked);
            });
        }
    }

    override reset(): void {
        this.selected = [];
        this.form.reset();
    }

    menuMainReplace(menuMainName: string): string {
        return menuMainName.replace(/-/g, '|');
    }

    override menuChange(ev, idx: string) {
        let arr = idx.split('-');

        if (arr.length == 1) {
            Object.keys(this.form.controls).filter(ctl => ctl.split('-')[0] === idx).forEach(key => {
                this.form.controls[key].setValue(ev.target.checked);
            });
        } else {
            this.form.controls[idx.split('-')[0]].setValue(this.checkedGroup(idx, ev.target.checked));
        }
        this.updateHandler.emit(this.form.value);
    }

    override checkedGroup(idx: string, checked?: boolean) {
        let groupChecked:boolean = true;
        let idxs = idx.split('-');
        if (idxs.length > 1) {
            if (checked) {
                Object.keys(this.form.controls).filter(ctl => ctl.split('-').length > 1 && ctl.split('-')[0] == idxs[0]).forEach(key => {
                    let val = this.form.controls[key].value;
                    if (!val) groupChecked = val;
                });
            } else {
                groupChecked = checked ? checked : false;
            }
            return groupChecked;
        } else {
            Object.keys(this.form.controls).filter(ctl => ctl.split('-')[0] == idx && ctl.split('-').length > 1).forEach(sub => {
                let val = this.form.controls[sub].value;
                if (!val) groupChecked = val;
            });
            return groupChecked;
        }
    }

   override click() {
        this.updateHandler.emit(this.form.value);
    }

    public filter(menus: Menu[]): Menu[] {
        // console.log(menus.filter(m => m.main.indexOf(this.Search) != -1));

        return menus.filter(m => m.main.indexOf(this.Search) != -1);
    }
}
