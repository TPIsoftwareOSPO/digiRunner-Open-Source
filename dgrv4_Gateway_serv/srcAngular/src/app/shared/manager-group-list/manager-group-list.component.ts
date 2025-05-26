import { Component, OnInit } from "@angular/core";
import { ToolService } from "../services/tool.service";
import { DynamicDialogRef } from "primeng/dynamicdialog";



import { FormBuilder, FormControl, FormGroup } from "@angular/forms";

import { ClientService } from "../services/api-client.service";
import { AA0238Req, GroupInfo_0238 } from "src/app/models/api/ClientService/aa0238.interface";

@Component({
  selector: 'app-manager-group-list',
  templateUrl: './manager-group-list.component.html',
  styleUrls: ['./manager-group-list.component.css'],
})
export class ManagerGroupListComponent implements OnInit {

  form!: FormGroup;
  groupInfoListCols: { field: string; header: string; }[] = [];
  groupInfoList: Array<GroupInfo_0238> = [];
  selectedGroups: Array<GroupInfo_0238> = [];

  clientID: string = '';
  securityLevelID: string = '';

  constructor(
    private toolService: ToolService,
    // private serverService: ServerService,
    private ref: DynamicDialogRef,
    // private config: DynamicDialogConfig,
    // private translate: TranslateService,
    private fb: FormBuilder,
    private clientService: ClientService
  ) { }

  async ngOnInit() {
    this.form = this.fb.group({
      keyword: new FormControl('')
    })
    const code = ['group_id', 'group_name', 'group_alias', 'group_desc', 'security_level']
    const dict = await this.toolService.getDict(code);
    this.groupInfoListCols = [
      { field: 'groupID', header: `${dict['group_id']}` },
      { field: 'groupName', header: `${dict['group_name']}` },
      { field: 'groupAlias', header: `${dict['group_alias']}` },
      { field: 'groupDesc', header: dict['group_desc'] },
      // { field: 'securityLevelName', header: dict['security_level'] }
    ];
    // this.clientID = this.config.data.clientID;
    // this.securityLevelID = this.config.data.securityLevelID;

    this.queryGroupList();
  }

  queryGroupList() {
    this.groupInfoList = [];
    this.selectedGroups = [];

    let reqBody = {
      keyword: this.keyword.value,
      securityLevelID: "",
      groupAuthoritiesID:[]
    } as AA0238Req;
    // console.log(reqBody);
    this.clientService.queryGroupList_0238(reqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.groupInfoList = res.RespBody.groupInfoList;
      }
    });
  }

  moreGroupList() {
    let ReqBody = {
      groupId: this.groupInfoList[this.groupInfoList.length - 1].groupID,
      keyword: this.keyword!.value,
    } as AA0238Req;
    this.clientService.queryGroupList_0238(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.groupInfoList = this.groupInfoList.concat(res.RespBody.groupInfoList);
      }
    });
  }



  chooseGroup() {
    this.ref.close(this.selectedGroups);
  }

  cancelGroup() {
    this.ref.close(null);
  }

  // formateDate(date: Date) {
  //   if (!date) return '';
  //   const procDate = Number(date);
  //   return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') : '';
  // }

  public get keyword() { return this.form.get('keyword')!; };
}
