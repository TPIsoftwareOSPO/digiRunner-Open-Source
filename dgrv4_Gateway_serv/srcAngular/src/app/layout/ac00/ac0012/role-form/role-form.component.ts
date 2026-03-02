import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, FormGroupDirective } from '@angular/forms';
import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { FormParams } from '../../../../models/api/form-params.interface';
import { ListGroupsComponent } from 'src/app/shared/list-group/list-groups.component';
import { Menu } from 'src/app/models/menu.model';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TranslateService } from '@ngx-translate/core';
import { SidebarService } from 'src/app/layout/components/sidebar/sidebar.service';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { AA0011Req } from 'src/app/models/api/UserService/aa0011.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';

@Component({
    selector: 'app-role-form',
    templateUrl: './role-form.component.html',
    styleUrls: ['./role-form.component.css'],
    standalone: false
})
export class RoleFormComponent implements OnInit {

    @ViewChild('listgroup') listGroup!: ListGroupsComponent;
    @Input() data?: FormParams;
    @Input() close?: Function;

    form: UntypedFormGroup;
    selected: any = [];
    menus: Menu[] = [];
    roleNameLimitChar = { value: 30 };
    roleAliasLimitChar = { value: 30 };

    constructor(
        private fb: UntypedFormBuilder,
        private roleService: RoleService,
        private toolService: ToolService,
        private translate: TranslateService,
        private sidebarService: SidebarService
    ) {
        this.form = this.fb.group({
            roleName: '',
            roleAlias: '',
            funcCodeList: ''
        });
    }

    ngOnInit() {
        this.form = this.fb.group({
            roleName: new UntypedFormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringNameValidator(this.roleNameLimitChar.value)]),
            roleAlias: new UntypedFormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.roleAliasLimitChar.value)]),
            funcCodeList: new UntypedFormControl('', [ValidatorFns.requiredValidator()])
        });
        // this.form.statusChanges.subscribe(r => console.log(this.form.value))
        //預埋api
        let tFuncsDetails = this.toolService.getFuncList();
        let menus = this.sidebarService.transform(tFuncsDetails);
        this.menus = menus;
    }

    submitForm(formDirective: FormGroupDirective) {
        const codes = ['message.create', 'message.role', 'message.success',];
        this.translate.get(codes).subscribe(dict => {
            //預埋api
            let ReqBody = {
                roleName: this.roleName!.value,
                roleAlias: this.roleAlias!.value,
                funcCodeList: this.funcCodeList!.value
            } as AA0011Req;
            let createObservable = this.roleService.addTRole(ReqBody);
            if (this.close) this.close(createObservable);
        });
    }

    update(_selected) {
        this.funcCodeList!.markAsTouched();
        this.selected = Object.keys(_selected).filter(key => key.length > 4 && _selected[key] == true);
        if (!this.selected || this.selected.length == 0) {
            this.selected = '';
            this.funcCodeList!.setErrors({ error: 'required' })
            // console.log(this.funcCodeList);
        }
        this.funcCodeList!.setValue(this.selected);
    }

    public get roleName() { return this.form.get('roleName'); };
    public get roleAlias() { return this.form.get('roleAlias'); };
    public get funcCodeList() { return this.form.get('funcCodeList'); };

}
