import { Component, OnInit, Input } from '@angular/core';
import { ToolService } from '../services/tool.service';
import { RoleService } from '../services/api-role.service';
import {
  AA0020List,
  AA0020Req,
} from 'src/app/models/api/RoleService/aa0020.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { of } from 'rxjs';
import { FormOperate } from 'src/app/models/common.enum';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { AA0023Req, AA0023RoleInfo } from 'src/app/models/api/RoleService/aa0023.interface';

@Component({
  selector: 'app-role-list-lov',
  templateUrl: './role-list-lov.component.html',
  styleUrls: ['./role-list-lov.component.css'],
})
export class RoleListLovComponent implements OnInit {
  @Input() data?: FormParams;
  @Input() close?: Function;

  cols: { field: string; header: string }[] = [];
  roleDetailList: Array<AA0023RoleInfo> = new Array<AA0023RoleInfo>();
  // rowcount: number = 0;
  selected: any; // AA0020List || Array<AA0020List>

  keyword: string = '';

  constructor(
    private tool: ToolService,
    private roleService: RoleService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig
  ) {}

  ngOnInit() {
    this.init();
  }

  async init() {
    const code = ['role_id', 'role_name', 'role_desc'];
    const dict = await this.tool.getDict(code);
    this.cols = [
      { field: 'roleId', header: dict['role_id'] },
      { field: 'roleName', header: dict['role_name'] },
      { field: 'roleAlias', header: dict['role_desc'] },
    ];
    this.searchRoleDetailList('init');
    // console.log(this.config.data.selectionMode);
  }

  searchRoleDetailList(action: string) {
    if (action === 'search' && !this.keyword.trim()) return;
    this.roleDetailList = [];
    // this.rowcount = this.roleDetailList.length;
    switch (this.config.data.selectionMode) {
      case 'multiple':
        this.selected = new Array<AA0020List>();
        break;
      case 'single':
        this.selected = {} as AA0020List;
        break;
    }
    // let ReqBody = {
    //   keyword: this.keyword,
    // } as AA0020Req;
    // if (this.data?.operate == FormOperate.update) {
    //   ReqBody.funcFlag = false;
    //   ReqBody.authorityFlag = true;
    //   ReqBody.roleName = this.data.data.roleName;
    // }
    // this.roleService.queryTRoleList_v3(ReqBody).subscribe((res) => {
    //   if (this.tool.checkDpSuccess(res.ResHeader)) {
    //     this.roleDetailList = res.RespBody.roleDetailList;
    //     this.rowcount = this.roleDetailList.length;
    //   }
    // });
    let ReqBody = {
      keyword: this.keyword,
    } as AA0023Req;
    this.roleService.queryRoleRoleList(ReqBody).subscribe(res=>{
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleDetailList = res.RespBody.roleRoleMappingList;
      }
    })
  }

  moreData() {
    // let ReqBody = {
    //   roleId: this.roleDetailList[this.roleDetailList.length - 1].roleID,
    //   keyword: this.keyword,
    // } as AA0020Req;
    // if (this.data?.operate == FormOperate.update) {
    //   ReqBody.funcFlag = false;
    //   ReqBody.authorityFlag = true;
    //   ReqBody.roleName = this.data.data.roleName;
    // }
    // this.roleService.queryTRoleList_v3(ReqBody).subscribe((res) => {
    //   if (this.tool.checkDpSuccess(res.ResHeader)) {
    //     this.roleDetailList = this.roleDetailList.concat(
    //       res.RespBody.roleDetailList
    //     );
    //     this.rowcount = this.roleDetailList.length;
    //   }
    // });
    let ReqBody = {
      roleId: this.roleDetailList[this.roleDetailList.length - 1].roleId,
      keyword: this.keyword,
    } as AA0023Req;

    this.roleService.queryRoleRoleList(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.roleDetailList = this.roleDetailList.concat(
          res.RespBody.roleRoleMappingList
        );
        // this.rowcount = this.roleDetailList.length;
      }
    });
  }

  chooseRole() {
    // if (this.close) this.close(of(this.selected));
    this.ref.close(this.selected);
  }

  cancelRole() {
    // if (this.close) this.close(of(null));
    this.ref.close(null);
  }
}
