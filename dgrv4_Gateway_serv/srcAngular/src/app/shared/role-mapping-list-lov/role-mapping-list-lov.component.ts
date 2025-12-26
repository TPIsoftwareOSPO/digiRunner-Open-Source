import { DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, Input, OnInit } from '@angular/core';
import { of } from 'rxjs';
import { FormParams } from 'src/app/models/api/form-params.interface';
import {
  AA0023Req,
  AA0023RoleInfo,
} from 'src/app/models/api/RoleService/aa0023.interface';
import { RoleService } from '../services/api-role.service';
import { ToolService } from '../services/tool.service';

@Component({
  selector: 'app-role-mapping-list-lov',
  templateUrl: './role-mapping-list-lov.component.html',
  styleUrls: ['./role-mapping-list-lov.component.css'],
})
export class RoleMappingListLovComponent implements OnInit {
  // @Input() data?: FormParams;
  @Input() close?: Function;

  cols: { field: string; header: string }[] = [];
  rowcount: number = 0;
  selected: Array<AA0023RoleInfo> = [];
  keyword: string = '';
  roleInfoList: Array<AA0023RoleInfo> = new Array<AA0023RoleInfo>();

  constructor(
    private tool: ToolService,
    private roleService: RoleService,
    private ref: DynamicDialogRef
  ) {}

  ngOnInit() {
    this.init();
  }

  async init() {
    const code = ['role_id', 'role_name', 'role_alias'];
    const dict = await this.tool.getDict(code);
    this.cols = [
      { field: 'roleId', header: dict['role_id'] },
      { field: 'roleName', header: dict['role_name'] },
      { field: 'roleAlias', header: dict['role_alias'] },
    ];
    this.queryRoleMappingList('init');
  }

  queryRoleMappingList(action: string) {
    if (action === 'search' && !this.keyword.trim()) return;
    this.roleInfoList = [];
    this.rowcount = this.roleInfoList.length;
    let ReqBody = {
      keyword: this.keyword,
    } as AA0023Req;
    this.roleService.queryRoleRoleList(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleInfoList = res.RespBody.roleRoleMappingList;
        this.rowcount = this.roleInfoList.length;
      }
    });
  }

  moreData() {
    let ReqBody = {
      roleId: this.roleInfoList[this.roleInfoList.length - 1].roleId,
      keyword: this.keyword,
    } as AA0023Req;
    this.roleService.queryRoleRoleList(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleInfoList = this.roleInfoList.concat(
          res.RespBody.roleRoleMappingList
        );
        this.rowcount = this.roleInfoList.length;
      }
    });
  }

  chooseRole() {
    // if (this.close) this.close(of(this.selected));
    this.ref.close(this.selected);
  }
}
