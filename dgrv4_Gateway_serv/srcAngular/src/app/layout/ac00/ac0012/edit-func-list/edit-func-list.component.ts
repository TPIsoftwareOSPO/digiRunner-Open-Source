import { FuncService } from './../../../../shared/services/api-func.service';
import { Component, OnInit, Input } from '@angular/core';
import { FormParams } from '../../../../models/api/form-params.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AA0013Req } from 'src/app/models/api/UserService/aa0013.interface';
import { SidebarService } from 'src/app/layout/components/sidebar/sidebar.service';
import { Menu } from 'src/app/models/menu.model';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { RoleService } from 'src/app/shared/services/api-role.service';

@Component({
  selector: 'app-edit-func-list',
  templateUrl: './edit-func-list.component.html',
  styleUrls: ['./edit-func-list.component.css'],
  providers: [FuncService]
})
export class EditFuncListComponent implements OnInit {
  @Input() data?: FormParams;
  @Input() close?: Function;
  @Input() closeWithPram?: Function;
  @Input() disableCheckbox: boolean = false;

  selectedCities: string[] = [];
  selecteds: { name: string; code: string; }[] = [];
  funcs: { name: string; code: string; }[] = [];
  menus: Menu[] = [];
  form: FormGroup;
  newRoleAliasLimitChar = { value: 30 };
  selectedFuncList: any = [];

  constructor(
    private roleService: RoleService,
    private toolService: ToolService,
    private sidebarService: SidebarService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      newRoleAlias: new FormControl(this.data?.data.roleAlias, [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.newRoleAliasLimitChar.value)])
    });
  }

  ngOnInit() {
    let tFuncsDetails = this.toolService.getFuncList();
    let menus = this.sidebarService.transform(tFuncsDetails);
    this.menus = menus;
    let _funcCheckbox = {};
    this.data?.data.funcCodeList.map(func => {
      _funcCheckbox[func] = true;
    });
    this.update(_funcCheckbox);

  }

  update(menus) {
    this.selectedFuncList = Object.keys(menus).filter(menu => menu.length > 4 && menus[menu] == true).map(m => m);
  }

  submitForm() {
    let req = {
      roleId: this.data?.data.roleID,
      roleName: this.data?.data.roleName,
      newRoleAlias: this.newRoleAlias!.value,
      newFuncCodeList: this.selectedFuncList
    } as AA0013Req
    let updateObservable = this.roleService.updateTRoleFunc(req);
    if (this.close) this.close(updateObservable);
  }

  public get newRoleAlias() { return this.form.get('newRoleAlias'); };
}
