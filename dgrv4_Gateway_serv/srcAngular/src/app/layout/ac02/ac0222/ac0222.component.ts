import { MessageService } from 'primeng/api';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { VGroupInfo, AA0222Req } from 'src/app/models/api/ClientService/aa0222.interface';
import { AA0237Req } from 'src/app/models/api/ClientService/aa0237.interface';
import { AA1115GroupAuthorities, AA1115Req } from 'src/app/models/api/GroupAuthService/aa1115.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { Req_0213 } from 'src/app/models/api/ClientService/aa0213.interface';
import { AA0223Req } from 'src/app/models/api/ClientService/aa0223.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { GroupAuthService } from 'src/app/shared/services/api-group-auth.service';
import { DatetimeFormatPipe } from 'src/app/shared/pipes/datetime.format.pipe';
import { SelectItem } from 'primeng/api';
import { Listbox } from 'primeng/listbox';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { AA0234Req } from 'src/app/models/api/ClientService/aa0234.interface';
import { AA0233Req } from 'src/app/models/api/ClientService/aa0233.interface';
import { AA0224Req } from 'src/app/models/api/ClientService/aa0224.interface';
import { AA0225Req } from 'src/app/models/api/ClientService/aa0225.interface';
import { AA0221Req } from 'src/app/models/api/ClientService/aa0221.interface';

@Component({
  selector: 'app-ac0222',
  templateUrl: './ac0222.component.html',
  styleUrls: ['./ac0222.component.css'],
  providers: [ClientService, MessageService, DatetimeFormatPipe]
})
export class Ac0222Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;
  @ViewChild('data_0235Listbox') data_0235ListboxComponent!: Listbox;
  @ViewChild('data_APIListListbox') data_APIListListboxComponent!: Listbox;
  cols: { field: string; header: string; width: string; type?: DatetimeFormatPipe; }[] = [];
  colsAuthorities: { field: string; header: string; width: string; }[] = [];
  colsModuleList: { field: string; header: string; width: string; }[] = [];
  colsModuleList_page9: { field: string; header: string; width: string; }[] = [];
  ChooseApiList: { field: string; header: string; width: string; }[] = [];
  NoneChooseApiList: { field: string; header: string; width: string; }[] = [];
  ChooseModuleList: { field: string; header: string; width: string; }[] = [];
  NoneChooseModuleList: { field: string; header: string; width: string; }[] = [];
  ChooseApiList_page12: { field: string; header: string; width: string; }[] = [];
  NoneChooseApiList_page12: { field: string; header: string; width: string; }[] = [];
  ChooseModuleList_page11: { field: string; header: string; width: string; }[] = [];
  NoneChooseModuleList_page11: { field: string; header: string; width: string; }[] = [];
  data: VGroupInfo[] = [];
  dataAuthorities: AA1115GroupAuthorities[] = [];
  dataAuthorities_page4: AA1115GroupAuthorities[] = [];
  dataAuthorities_page9: AA1115GroupAuthorities[] = [];
  dialogTitle: string = '';
  formOperate = FormOperate;
  rowcount: number = 0;
  rowcount_Authorities: number = 0;
  rowcount_Authorities_page4: number = 0;
  rowcount_Authorities_page9: number = 0;
  rowcount_page6: number = 0;
  rowcount_page12: number = 0;
  rowcount_page7: number = 0;
  rowcount_page11: number = 0;
  form!: FormGroup;
  form_page2!: FormGroup;
  form_page3!: FormGroup;
  form_page4!: FormGroup;
  form_page5!: FormGroup;
  form_page6!: FormGroup;
  form_page7!: FormGroup;
  form_page8!: FormGroup;
  form_page9!: FormGroup;
  form_page10!: FormGroup;
  form_page11!: FormGroup;
  form_page12!: FormGroup;
  pageNum: number = 1;
  securityLevels: { label: string; value: string; }[] = [];
  new_securityLevels: { label: string; value: string; }[] = [];
  selected: Array<AA1115GroupAuthorities> = [];
  selected_page4: Array<AA1115GroupAuthorities> = [];
  selected_page9: Array<AA1115GroupAuthorities> = [];
  data_0235: SelectItem[] = [];;
  data_0235_page4: SelectItem[] = [];;
  data_0235_page9: SelectItem[] = [];;
  been_choose_api_list: any;
  none_choose_api_list: any;
  been_choose_api_list_page12: any;
  none_choose_api_list_page12: any;
  last_api_page6: String = '';
  last_api_page12: String = '';
  last_module_page7: String = '';
  last_module_page11: String = '';
  been_choose_module_list: any;
  none_choose_module_list: any;
  been_choose_module_list_page11: any;
  none_choose_module_list_page11: any;
  data_APIList: SelectItem[] = [];;
  selectedAPI_key: any;
  dayOptions: { label: string; value: string }[] = [];
  dayOptions_page9: { label: string; value: string }[] = [];
  groupAuthorities_page4: any[] = [];
  groupAuthorities_page9: any[] = [];
  orimoduleList: string = '';
  oriData: string = '';
  showDialog_memory: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private clientService: ClientService,
    private tool: ToolService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private groupAuthService: GroupAuthService,
    private toolService: ToolService,
    private list: ListService,
    private datetime_format: DatetimeFormatPipe,
  ) {
    super(route, tr);
    this.messageService.clear();
  }

  async ngOnInit() {
    this.form = this.fb.group({
      groupkeyword: new FormControl(''),
      groupAuthoritiesID: new FormControl({ value: [], disabled: true }),
      securityLevelID: new FormControl(''),
      authoritieskeyword: new FormControl('')
    });
    this.form_page2 = this.fb.group({
      authoritieskeyword: new FormControl('')
    });
    this.form_page3 = this.fb.group({
      groupID: new FormControl(''),
      groupName: new FormControl(''),
      groupAlias: new FormControl(''),
      groupDesc: new FormControl(''),
      groupAccess: new FormControl(''),
      securityLevelName: new FormControl(''),
      allowAccessUseTimes: new FormControl(''),
      createDate: new FormControl(''),
      createUser: new FormControl(''),
      updateDate: new FormControl(''),
      updateUser: new FormControl(''),
      _allowAccessDays: new FormControl(''),
      groupAuthoritiesName: new FormControl('')
    });
    this.form_page4 = this.fb.group({
      groupID: new FormControl(''),
      groupName_page4: new FormControl(''),
      groupAlias_page4: new FormControl(''),
      groupDesc_page4: new FormControl(''),
      groupAccess: new FormControl(''),
      securityLevelName_page4: new FormControl(''),
      dayOptionsName_page4: new FormControl(''),
      allowAccessUseTimes_page4: new FormControl(''),
      createDate: new FormControl(''),
      createUser: new FormControl(''),
      updateDate: new FormControl(''),
      updateUser: new FormControl(''),
      allowAccessDays_page4: new FormControl(''),
      groupAuthoritiesName: new FormControl({ value: [], disabled: true })
    });
    this.form_page5 = this.fb.group({
      authoritieskeyword: new FormControl('')
    });
    this.form_page6 = this.fb.group({
      groupkeyword: new FormControl(''),
      moduleName: new FormControl('')
    });
    this.form_page7 = this.fb.group({
      groupkeyword: new FormControl('')
    });
    this.form_page8 = this.fb.group({
      groupID: new FormControl(''),
      groupName: new FormControl(''),
      groupAlias: new FormControl(''),
      groupDesc: new FormControl(''),
      groupAccess: new FormControl(''),
      securityLevelName: new FormControl(''),
      allowAccessUseTimes: new FormControl(''),
      createDate: new FormControl(''),
      createUser: new FormControl(''),
      updateDate: new FormControl(''),
      updateUser: new FormControl(''),
      _allowAccessDays: new FormControl(''),
      groupAuthoritiesName: new FormControl('')
    });
    this.form_page9 = this.fb.group({
      groupID: new FormControl(''),
      groupName_page9: new FormControl(''),
      groupAlias_page9: new FormControl(''),
      groupDesc: new FormControl(''),
      groupAccess: new FormControl(''),
      securityLevelName: new FormControl(''),
      dayOptionsName: new FormControl(''),
      allowAccessUseTimes: new FormControl(''),
      createDate: new FormControl(''),
      createUser: new FormControl(''),
      updateDate: new FormControl(''),
      updateUser: new FormControl(''),
      allowAccessDays: new FormControl(''),
      groupAuthoritiesName: new FormControl({ value: [], disabled: true })
    });
    this.form_page10 = this.fb.group({
      authoritieskeyword: new FormControl('')
    });
    this.form_page11 = this.fb.group({
      groupkeyword: new FormControl('')
    });
    this.form_page12 = this.fb.group({
      groupkeyword: new FormControl(''),
      moduleName: new FormControl('')
    });

    this.groupAuthService.queryTSecurityLV({}).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {

        res.RespBody.securityLevelList?.map(slv => {
          let _lbl = slv.securityLevelId == 'SYSTEM' ? slv.securityLevelName + ` (default)`: slv.securityLevelName;
          this.securityLevels.push({ label: _lbl, value: slv.securityLevelId });
          this.new_securityLevels.push({ label: _lbl, value: slv.securityLevelId });
        });
      }
    });
    const codes = ['virtul_group_name', 'virtul_group_alias', 'virtul_group_desc', 'group_id', 'group_name', 'alias', 'virtul_group_desc', 'authorities', 'security_level', 'create_time', 'group_auth_id', 'group_auth_name', 'group_auth_desc', 'token_exp_time_options.day', 'token_exp_time_options.hour', 'token_exp_time_options.minute', 'token_exp_time_options.second', 'module_list', 'api_list'];
    const dict = await this.tool.getDict(codes);
    this.cols = [
      { field: 'vgroupName', header: dict['virtul_group_name'], width: '15%', },
      { field: 'vgroupAlias', header: `${dict['virtul_group_alias']}`, width: '15%' },
      { field: 'vgroupDesc', header: dict['virtul_group_desc'], width: '20%' },
      { field: 'vgroupAuthorities', header: dict['authorities']+"(v3)", width: '20%' },
      { field: 'securityLevelName', header: dict['security_level'], width: '10%' },
      { field: 'createTime', header: dict['create_time'], width: '10%' }
    ];
    this.colsAuthorities = [
      { field: 'groupAuthoritiesId', header: dict['group_auth_id'], width: '20%', },
      { field: 'groupAuthoritiesName', header: `${dict['group_auth_name']}`, width: '40%' },
      { field: 'groupAuthoritiesDesc', header: dict['group_auth_desc'], width: '40%' }
    ];
    this.colsModuleList = [
      { field: 'label', header: dict['module_list'], width: '20%', },
      { field: 'apilist', header: dict['api_list'], width: '70%', },
    ];
    this.colsModuleList_page9 = [
      { field: 'label', header: dict['module_list'], width: '20%', },
      { field: 'apilist', header: dict['api_list'], width: '70%', },
    ];
    this.ChooseApiList = [
      { field: 'apiKey', header: dict['api_list'], width: '70%', },
    ];
    this.NoneChooseApiList = [
      { field: 'apiKey', header: dict['api_list'], width: '70%', },
    ];
    this.ChooseModuleList = [
      { field: 'moduleName', header: dict['module_list'], width: '70%', },
    ];
    this.NoneChooseModuleList = [
      { field: 'moduleName', header: dict['module_list'], width: '70%', },
    ];
    this.ChooseModuleList_page11 = [
      { field: 'moduleName', header: dict['module_list'], width: '70%', },
    ];
    this.NoneChooseModuleList_page11 = [
      { field: 'moduleName', header: dict['module_list'], width: '70%', },
    ];
    this.ChooseApiList_page12 = [
      { field: 'apiKey', header: dict['api_list'], width: '70%', },
    ];
    this.NoneChooseApiList_page12 = [
      { field: 'apiKey', header: dict['api_list'], width: '70%', },
    ];
    this.data = [];
    this.rowcount = 0;
    //預埋api
    let ReqBody = {
      keyword: this.form.get('groupkeyword')!.value,
      securityLevelId: this.form.get('securityLevelID')!.value,
      vgroupAuthoritiesIds: this.form.get('groupAuthoritiesID')!.value
    } as AA0222Req;
    this.clientService.queryVirtulGroupList_ignore1298(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rowcount = res.RespBody.dataList.length;
        this.data = res.RespBody.dataList;
      }
    });
  }

  submitForm() {
    this.data = [];
    this.rowcount = 0;
    //預埋api
    let ReqBody = {
      keyword: this.form.get('groupkeyword')!.value,
      securityLevelId: this.form.get('securityLevelID')!.value,
      vgroupAuthoritiesIds: this.form.get('groupAuthoritiesID')!.value
    } as AA0222Req;
    this.clientService.queryVirtulGroupList(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rowcount = res.RespBody.dataList.length;
        this.data = res.RespBody.dataList;
      }
    });
  }
  moreData() {
    let ReqBody = {
      vgroupId: this.data[this.data.length - 1].vgroupId,
      keyword: this.form.get('groupkeyword')!.value,
      securityLevelId: this.form.get('securityLevelID')!.value,
      vgroupAuthoritiesIds: this.form.get('groupAuthoritiesID')!.value
    } as AA0222Req;
    this.clientService.queryVirtulGroupList(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.data = this.data.concat(res.RespBody.dataList);
        this.rowcount = this.data.length;
      }
    });
  }

  queryauthorities() {
    let ReqBody = {
      keyword: this.form_page2.get('authoritieskeyword')!.value,
    } as AA1115Req;

    this.groupAuthService.queryScopeAuthorities(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rowcount_Authorities = res.RespBody.groupAuthoritiesList.length;
        this.dataAuthorities = res.RespBody.groupAuthoritiesList;
      }
    });
  }

  queryauthorities_page4() {

    this.rowcount_Authorities_page4 = 0;
    this.dataAuthorities_page4 = [];

    let ReqBody = {
      keyword: this.form_page5.get('authoritieskeyword')!.value,
    } as AA1115Req;

    this.groupAuthService.queryScopeAuthorities(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rowcount_Authorities_page4 = res.RespBody.groupAuthoritiesList.length;
        this.dataAuthorities_page4 = res.RespBody.groupAuthoritiesList;
      }
    });
  }

  async queryAuthoritiesList() {
    const codes = ['authorities'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title} > ${dict['authorities']}`;
    this.pageNum = 2;
  }

  async queryAuthoritiesList_page4() {
    const codes = ['authorities'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title} > ${dict['authorities']}`;
    this.pageNum = 5;
    this.queryauthorities_page4()
  }
  return_to_list() {
    this.data_0235ListboxComponent.filter = false
    this.data_0235ListboxComponent.filterValue = '';
    this.data_APIListListboxComponent.filter = false
    this.data_APIListListboxComponent.filterValue = '';
    this.data_APIList = []
    this.selectedAPI_key = {}
    this.form_page9.get("groupName_page9")!.setValue("")
    this.form_page9.get("groupAlias_page9")!.setValue("")
    this.form_page9.get("allowAccessDays")!.setValue("")
    this.form_page9.get("dayOptionsName")!.setValue("")
    this.form_page9.get("allowAccessUseTimes")!.setValue("")
    this.form_page9.get("groupDesc")!.setValue("")
    this.form_page9.get("securityLevelName")!.setValue("")
    this.form_page9.get("groupAuthoritiesName")!.setValue([])
    this.title = `${this.title.split('>')[0]}`;
    this.pageNum = 1;
  }
  async return_to_list_page4() {
    const codes = ['dialog.virtul_group_update'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']}`;
    this.rowcount_Authorities_page4 = 0;
    this.dataAuthorities_page4 = [];
    this.selected_page4 = [];
    this.pageNum = 4;
  }
  chooseAuthorities() {
    this.title = `${this.title.split('>')[0]}`;
    if (this.selected) {
      var arr: any[] = []
      this.selected.forEach(function (item) {
        arr.push(item.groupAuthoritiesId)
      })
      this.form.get('groupAuthoritiesID')!.setValue(arr)
    }
    else {
      this.form.get('groupAuthoritiesID')!.setValue("")
    }
    this.pageNum = 1;
  }
  async chooseAuthorities_page4() {
    const codes = ['dialog.virtul_group_update'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']}`;
    if (this.selected_page4) {
      var arr_id: any[] = []
      var arr_name: any[] = []
      this.selected_page4.forEach(function (item) {
        arr_id.push(item.groupAuthoritiesId)
        arr_name.push(item.groupAuthoritiesName)
      })
      this.groupAuthorities_page4 = arr_id
      this.form_page4.get('groupAuthoritiesName')!.setValue(arr_name)
    }
    else {
      this.form_page4.get('groupAuthoritiesName')!.setValue("")
    }
    this.rowcount_Authorities_page4 = 0;
    this.dataAuthorities_page4 = [];
    this.selected_page4 = [];
    this.pageNum = 4;
  }
  async showDialog(rowData: VGroupInfo, operation: FormOperate) {
    const codes = ['dialog.detail_query', 'dialog.virtul_group_update', 'dialog.virtul_group_delete', 'message.update', 'message.delete', 'message.virtul_group',
      'message.success'];
    const dict = await this.tool.getDict(codes);
    // this.display=true;
    let req_0223 = {
      vgroupId: rowData.vgroupId,
      vgroupName: rowData.vgroupName
    } as AA0223Req;
    switch (operation) {
      case FormOperate.detail:
        this.clientService.queryVirtulGroupDetail(req_0223).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            let AA0237Req = {
              vgroupId: res.RespBody.vgroupId
            } as AA0237Req;
            this.clientService.queryVirtulGroupApiList(AA0237Req).subscribe(async res_0237 => {
              if (this.tool.checkDpSuccess(res_0237.ResHeader)) {

                this.data_0235ListboxComponent.filter = true
                this.data_0235ListboxComponent.filterValue = '';
                this.data_APIListListboxComponent.filter = true
                this.data_APIListListboxComponent.filterValue = '';

                let data_0237: any[] = []
                res_0237.RespBody.respA.moduleNameList.forEach(element => {
                  let single_data = {}
                  single_data["label"] = element
                  let value = {
                    vgroupId: res.RespBody.vgroupId,
                    reqB: {
                      moduleName: element,
                      p: false
                    }
                  }
                  single_data["value"] = value
                  data_0237.push(single_data)
                });
                this.data_0235 = data_0237
                if (data_0237.length > 0) {
                  this.selectedAPI_key = data_0237[0]["value"]
                  this.showDialog_APIList(data_0237[0]["value"])
                }

                var _allowAccessDays_str = ""
                if (res.RespBody["approximateTimeUnit"] == "") {
                  _allowAccessDays_str = res.RespBody["allowDays"] + res.RespBody["timeUnitName"]
                }
                else {
                  _allowAccessDays_str = res.RespBody["allowDays"] + res.RespBody["timeUnitName"] + `(${res.RespBody["approximateTimeUnit"]})`
                }
                this.form_page3.patchValue({
                  groupID: res.RespBody.vgroupId,
                  groupName: res.RespBody.vgroupName,
                  groupAlias: res.RespBody.vgroupAlias,
                  groupDesc: res.RespBody.vgroupDesc,
                  groupAccess: res.RespBody.vgroupAccess,
                  securityLevelName: res.RespBody["securityLevelName"],
                  allowAccessUseTimes: res.RespBody["allowTimes"],
                  createDate: res.RespBody.createDate,
                  createUser: res.RespBody.createUser,
                  updateDate: res.RespBody.updateDate,
                  updateUser: res.RespBody.updateUser,
                  _allowAccessDays: _allowAccessDays_str,
                  groupAuthoritiesName: this.transformGroupAuthorities(res.RespBody.vgroupAuthorities),
                });
                this.title = `${this.title} > ${dict['dialog.detail_query']}`;
                this.pageNum = 3
              }
            });
          }
        });
        break;
      case FormOperate.update:

        this.dialogTitle = dict['dialog.virtul_group_update'];
        this.clientService.queryVirtulGroupDetail(req_0223).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            // status
            let ReqBody = {
              encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('TIME_UNIT')) + ',' + 25,
              isDefault: 'N'
            } as DPB0047Req;
            this.list.querySubItemsByItemNo(ReqBody).subscribe(res_DPB0047 => {
              if (this.toolService.checkDpSuccess(res_DPB0047.ResHeader)) {
                let Status:{label:string, value: string}[] = [];
                if (res_DPB0047.RespBody.subItems) {
                  for (let item of res_DPB0047.RespBody.subItems) {
                    Status.push({ label: item.subitemName, value: item.subitemNo });
                  }
                }
                this.dayOptions = Status;



                let AA0237Req = {
                  vgroupId: res.RespBody.vgroupId,
                  reqB: {
                    p: false
                  }
                } as AA0237Req;
                this.clientService.queryVirtulGroupApiList(AA0237Req).subscribe(async res_0237 => {
                  if (this.tool.checkDpSuccess(res_0237.ResHeader)) {
                    this.clientService.updateVirtulGroup_before().subscribe(res_updateGroup_before => {
                      if (this.tool.checkDpSuccess(res_updateGroup_before.ResHeader)) {
                        res_updateGroup_before["RespBody"]["constraints"][2]["field"] = "groupName_page4"
                        res_updateGroup_before["RespBody"]["constraints"][3]["field"] = "groupAlias_page4"
                        res_updateGroup_before["RespBody"]["constraints"][4]["field"] = "allowAccessDays_page4"
                        res_updateGroup_before["RespBody"]["constraints"][5]["field"] = "allowAccessUseTimes_page4"
                        res_updateGroup_before["RespBody"]["constraints"][6]["field"] = "securityLevelName_page4"
                        res_updateGroup_before["RespBody"]["constraints"][7]["field"] = "groupDesc_page4"
                        res_updateGroup_before["RespBody"]["constraints"] = res_updateGroup_before["RespBody"]["constraints"].slice(2, 8)
                        this.addFormValidator(this.form_page4, res_updateGroup_before["RespBody"]["constraints"]);
                        let data_0237:any[] = []
                        res_0237.RespBody.respB2.dataList.forEach(item => {
                          let single_data = {}
                          let apilist = ""
                          let apilist_value:any[] = [];
                          item.apiKeyList.forEach(element => {
                            if (element["apiName"]) {
                              apilist += `${element["apiKey"]}(${element["apiName"]})` + "、"
                            }
                            else {
                              apilist += `${element["apiKey"]}()` + "、"
                            }
                            apilist_value.push(element)
                          })
                          single_data["label"] = item.moduleName
                          single_data["value"] = apilist_value
                          single_data["apilist"] = apilist.slice(0, apilist.length - 1)
                          data_0237.push(single_data)

                        })
                        this.data_0235_page4 = data_0237
                        this.orimoduleList = JSON.stringify(res_0237.RespBody.respB2)
                        this.oriData = JSON.stringify(res.RespBody)

                        this.form_page4.patchValue({
                          groupID: res.RespBody.vgroupId,
                          groupName_page4: res.RespBody.vgroupName,
                          groupAlias_page4: res.RespBody.vgroupAlias,
                          groupDesc_page4: res.RespBody.vgroupDesc,
                          groupAccess: res.RespBody.vgroupAccess,
                          securityLevelName_page4: res.RespBody["securityLevelId"],
                          allowAccessUseTimes_page4: res.RespBody["allowTimes"],
                          createDate: res.RespBody.createDate,
                          createUser: res.RespBody.createUser,
                          updateDate: res.RespBody.updateDate,
                          updateUser: res.RespBody.updateUser,
                          allowAccessDays_page4: res.RespBody["allowDays"],
                          dayOptionsName_page4: res.RespBody["timeUnit"],
                          groupAuthoritiesName: this.transformGroupAuthorities(res.RespBody.vgroupAuthorities),
                        });

                        this.title = `${this.title} > ${dict['dialog.virtul_group_update']}`;
                        this.pageNum = 4
                      }
                    })
                  }
                })
              }
            });
          }
        })
        break;
      case FormOperate.delete: {
        this.clientService.queryVirtulGroupDetail(req_0223).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            let AA0237Req = {
              vgroupId: res.RespBody.vgroupId
            } as AA0237Req;
            this.clientService.queryVirtulGroupApiList(AA0237Req).subscribe(async res_0237 => {
              if (this.tool.checkDpSuccess(res_0237.ResHeader)) {
                this.title = `${this.title} > ${dict['dialog.virtul_group_delete']}`;
                this.pageNum = 8
                this.data_0235ListboxComponent.filter = true
                this.data_0235ListboxComponent.filterValue = '';
                this.data_APIListListboxComponent.filter = true
                this.data_APIListListboxComponent.filterValue = '';

                let data_0237:any[] = []
                res_0237.RespBody.respA.moduleNameList.forEach(element => {
                  let single_data = {}
                  single_data["label"] = element
                  let value = {
                    vgroupId: res.RespBody.vgroupId,
                    reqB: {
                      moduleName: element,
                      p: false
                    }
                  }
                  single_data["value"] = value
                  data_0237.push(single_data)
                });
                this.data_0235 = data_0237
                if (data_0237.length > 0) {
                  this.selectedAPI_key = data_0237[0]["value"]
                  this.showDialog_APIList(data_0237[0]["value"])
                }
                var _allowAccessDays_str = ""
                if (res.RespBody["approximateTimeUnit"] == "") {
                  _allowAccessDays_str = res.RespBody["allowDays"] + res.RespBody["timeUnitName"]
                }
                else {
                  _allowAccessDays_str = res.RespBody["allowDays"] + res.RespBody["timeUnitName"] + `(${res.RespBody["approximateTimeUnit"]})`
                }
                this.form_page8.patchValue({
                  groupID: res.RespBody.vgroupId,
                  groupName: res.RespBody.vgroupName,
                  groupAlias: res.RespBody.vgroupAlias,
                  groupDesc: res.RespBody.vgroupDesc,
                  groupAccess: res.RespBody.vgroupAccess,
                  securityLevelName: res.RespBody["securityLevelName"],
                  allowAccessUseTimes: res.RespBody["allowTimes"],
                  createDate: res.RespBody.createDate,
                  createUser: res.RespBody.createUser,
                  updateDate: res.RespBody.updateDate,
                  updateUser: res.RespBody.updateUser,
                  _allowAccessDays: _allowAccessDays_str,
                  groupAuthoritiesName: this.transformGroupAuthorities(res.RespBody.vgroupAuthorities),
                });
              }
            });
          }
        });
        break;
      }
    }
  }
  async convertTimes(time: number) {
    const code = ['token_exp_time_options.day', 'token_exp_time_options.hour', 'token_exp_time_options.minute', 'token_exp_time_options.second'];
    const dict = await this.tool.getDict(code);
    if (time >= 86400) {
      if (time % 86400 == 0) {
        return `${time / 86400} ${dict['token_exp_time_options.day']}`;
      }
      else if (time % 3600 == 0) {
        return `${time / 3600} ${dict['token_exp_time_options.hour']}`;
      }
      else if (time % 60 == 0) {
        return `${time / 60} ${dict['token_exp_time_options.minute']}`;
      }
      else {
        return `${time} ${dict['token_exp_time_options.second']}`;
      }
    }
    else if (86400 > time && time >= 3600) {
      if (time % 3600 == 0) {
        return `${time / 3600} ${dict['token_exp_time_options.hour']}`;
      }
      else if (time % 60 == 0) {
        return `${time / 60} ${dict['token_exp_time_options.minute']}`;
      }
      else {
        return `${time} ${dict['token_exp_time_options.second']}`;
      }
    }
    else if (3600 > time && time >= 60) {
      if (time % 60 == 0) {
        return `${time / 60} ${dict['token_exp_time_options.minute']}`;
      }
      else {
        return `${time} ${dict['token_exp_time_options.second']}`;
      }
    }
    else {
      return `${time} ${dict['token_exp_time_options.second']}`;
    }
  }
  transformGroupAuthorities(groupAuthorities: Array<Object>) {
    this.groupAuthorities_page4 = []
    let Authorities_str = ''
    if (groupAuthorities.length > 0) {
      groupAuthorities.forEach(item => {
        Authorities_str += item["groupAuthoritieName"] + ","
        this.groupAuthorities_page4.push(item["groupAuthoritieId"])
      })
      Authorities_str = Authorities_str.slice(0, Authorities_str.length - 1)
    }
    return Authorities_str
  }
  async showDialog_APIList(Data: AA0237Req) {
    if (Data.vgroupId) {
      this.clientService.queryVirtulGroupApiList(Data).subscribe(async res_0237 => {
        if (this.tool.checkDpSuccess(res_0237.ResHeader)) {
          let data:any[] = []
          res_0237.RespBody.respB1.apiKeyList.forEach(element => {
            let single_data = {}
            single_data["label"] = `${element["apiKey"]}(${element["apiName"]})`
            single_data["value"] = element
            data.push(single_data)
          });
          this.data_APIList = data
        }
      })
    }
  }
  async Choose_API(Data: Object[]) {
    const codes = ['dialog.virtul_group_update', 'button.chs_api'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']} > ${dict['button.chs_api']}`;
    this.been_choose_api_list = Data["value"]
    this.form_page6.get("moduleName")!.setValue(Data["label"])
    this.pageNum = 6;
    this.call_0234()
  }
  call_0234() {
    this.none_choose_api_list = []
    this.rowcount_page6 = 0
    let selectedApiKeyList:any[] = []
    if (this.been_choose_api_list["apiList"] && this.been_choose_api_list["apiList"].length == 0)
      this.been_choose_api_list = []
    this.been_choose_api_list.forEach(function (item) {
      selectedApiKeyList.push(item["apiKey"])
    })
    let ReqBody = {
      moduleName: this.form_page6.get('moduleName')!.value,
      keyword: this.form_page6.get('groupkeyword')!.value,
      selectedApiKeyList: selectedApiKeyList
    } as AA0234Req;
    this.clientService.queryapi_0234(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.none_choose_api_list = res.RespBody["apiList"]
        this.last_api_page6 = res.RespBody["apiList"][res.RespBody["apiList"].length - 1]["apiKey"]
        this.rowcount_page6 = this.none_choose_api_list.length
      }
    });
  }
  Select_API(Data: Object[]) {
    this.been_choose_api_list.push(Data)
    this.none_choose_api_list.splice(this.none_choose_api_list.indexOf(Data), 1)
    this.rowcount_page6 = this.none_choose_api_list.length
  }
  Unselect_API(Data: Object[]) {
    if (!this.none_choose_api_list)
      this.none_choose_api_list = []
    this.none_choose_api_list.push(Data)
    this.been_choose_api_list.splice(this.been_choose_api_list.indexOf(Data), 1)
    this.rowcount_page6 = this.none_choose_api_list.length
  }
  moreData_page6() {
    let selectedApiKeyList:any[] = []
    this.been_choose_api_list.forEach(function (item) {
      selectedApiKeyList.push(item["apiKey"])
    })
    let ReqBody = {
      apiKey: this.last_api_page6,
      moduleName: this.form_page6.get('moduleName')!.value,
      keyword: this.form_page6.get('groupkeyword')!.value,
      selectedApiKeyList: selectedApiKeyList
    } as AA0234Req;
    this.clientService.queryapi_0234(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.none_choose_api_list = this.none_choose_api_list.concat(res.RespBody["apiList"])
        this.last_api_page6 = res.RespBody["apiList"][res.RespBody["apiList"].length - 1]["apiKey"]
        this.rowcount_page6 = this.none_choose_api_list.length
      }
    });
  }
  select_all_page6() {
    this.none_choose_api_list.forEach(element => {
      this.been_choose_api_list.push(element)
    });
    this.none_choose_api_list = []
    this.rowcount_page6 = this.none_choose_api_list.length
  }
  async save_choose_API() {
    const codes = ['dialog.virtul_group_update'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']}`;
    this.rowcount_Authorities_page4 = 0;
    this.dataAuthorities_page4 = [];
    this.selected_page4 = [];

    this.data_0235_page4.forEach(item => {
      if (item.label == this.form_page6.get('moduleName')!.value) {
        item["value"] = this.been_choose_api_list
        var apilist = ""
        this.been_choose_api_list.forEach(api_element => {
          if (api_element["apiName"]) {
            apilist += `${api_element["apiKey"]}(${api_element["apiName"]})` + "、"
          }
          else {
            apilist += `${api_element["apiKey"]}()` + "、"
          }
        });
        if (apilist != "")
          apilist = apilist.slice(0, apilist.length - 1)
        item["apilist"] = apilist
      }
    })
    this.been_choose_api_list = []
    this.none_choose_api_list = []
    this.form_page6.get('groupkeyword')!.setValue("")
    this.pageNum = 4;
  }


  async Choose_Module() {
    const codes = ['dialog.virtul_group_update', 'button.chs_api_module'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']} > ${dict['button.chs_api_module']}`;
    let selectedModuleList:any[] = []
    this.data_0235_page4.forEach(function (item) {
      selectedModuleList.push({ "moduleName": item["label"] })
    })
    this.been_choose_module_list = selectedModuleList
    this.pageNum = 7;
    this.call_0233()
  }
  call_0233() {
    this.none_choose_module_list = []
    this.rowcount_page7 = 0
    let selectedModuleKeyList:any[] = []
    this.been_choose_module_list.forEach(function (item) {
      selectedModuleKeyList.push(item["moduleName"])
    })
    let ReqBody = {
      keyword: this.form_page7.get('groupkeyword')!.value,
      selectedModuleNameList: selectedModuleKeyList
    } as AA0233Req;
    this.clientService.queryModule_0233(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let UnselectedModuleKeyList:any[] = []
        res.RespBody.moduleNameList.forEach(item => {
          UnselectedModuleKeyList.push({ "moduleName": item })
        })
        this.none_choose_module_list = UnselectedModuleKeyList
        this.last_module_page7 = res.RespBody.moduleNameList[res.RespBody["moduleNameList"].length - 1]
        this.rowcount_page7 = this.none_choose_module_list.length
      }
    });
  }
  Select_Module(Data: Object[]) {
    this.been_choose_module_list.push(Data)
    this.none_choose_module_list.splice(this.none_choose_module_list.indexOf(Data), 1)
    this.rowcount_page7 = this.none_choose_module_list.length
  }
  Unselect_Module(Data: Object[]) {
    this.none_choose_module_list.push(Data)
    this.been_choose_module_list.splice(this.been_choose_module_list.indexOf(Data), 1)
    this.rowcount_page7 = this.none_choose_module_list.length
  }
  select_all_page7() {
    this.none_choose_module_list.forEach(element => {
      this.been_choose_module_list.push(element)
    });
    this.none_choose_module_list = []
    this.rowcount_page7 = this.none_choose_module_list.length
  }
  moreData_page7() {
    let selectedModuleKeyList:any[] = []
    this.been_choose_module_list.forEach(function (item) {
      selectedModuleKeyList.push(item["moduleName"])
    })
    let ReqBody = {
      moduleName: this.last_module_page7,
      keyword: this.form_page7.get('groupkeyword')!.value,
      selectedModuleNameList: selectedModuleKeyList
    } as AA0233Req;
    this.clientService.queryModule_0233(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let UnselectedModuleKeyList:any[] = []
        res.RespBody.moduleNameList.forEach(item => {
          UnselectedModuleKeyList.push({ "moduleName": item })
        })
        this.none_choose_module_list = this.none_choose_module_list.concat(UnselectedModuleKeyList)
        this.last_module_page7 = res.RespBody.moduleNameList[res.RespBody["moduleNameList"].length - 1]
        this.rowcount_page7 = this.none_choose_module_list.length
      }
    });
  }
  async save_choose_Module() {
    const codes = ['dialog.virtul_group_update'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']}`;
    this.rowcount_Authorities_page4 = 0;
    this.dataAuthorities_page4 = [];
    this.selected_page4 = [];

    var temp_data:any[] = []
    this.been_choose_module_list.forEach(item => {
      if (this.data_0235_page4.length > 0) {
        for (var i = 0; i < this.data_0235_page4.length; i++) {
          if (item.moduleName == this.data_0235_page4[i]["label"]) {
            temp_data.push(this.data_0235_page4[i])
            break;
          }
          if (i == this.data_0235_page4.length - 1) {
            temp_data.push({ label: item.moduleName, value: { apiList: [], moduleName: item.moduleName }, apilist: "" })
          }
        }
      }
      else {
        temp_data.push({ label: item.moduleName, value: { apiList: [], moduleName: item.moduleName }, apilist: "" })
      }
    })
    this.data_0235_page4 = temp_data
    this.been_choose_module_list = []
    this.none_choose_module_list = []
    this.form_page7.get('groupkeyword')!.setValue("")
    this.pageNum = 4;
  }

  async submitForm_page4() {
    const codes = ['message.update', 'message.virtul_group', 'message.update', 'message.success'];
    const dict = await this.tool.getDict(codes);
    var ApiList:any[] = []
    this.data_0235_page4.forEach(item => {
      if (item["apilist"] != "") {
        var arr:any[] = []
        item["value"].forEach(element => {
          arr.push(element["apiKey"])
        });
        ApiList.push({ moduleName: item["label"], apiKeyList: arr })
      }
      // else{
      //     ApiList.push({ moduleName: item["label"], apiKeyList: [] })
      // }
    })

    var oriApiList:any[] = []

    JSON.parse(this.orimoduleList)["dataList"].forEach(item => {
      var apiList:any[] = []
      item.apiKeyList.forEach(element => {
        apiList.push(element.apiKey)
      })
      oriApiList.push({ moduleName: item.moduleName, apiKeyList: apiList })
    })

    var oriData = JSON.parse(this.oriData)

    var orivgroupAuthorities:any[] = []
    oriData.vgroupAuthorities.forEach(element => {
      orivgroupAuthorities.push(element.groupAuthoritieId)
    });
    let ReqBody = {
      vgroupId: this.form_page4.get("groupID")!.value,
      oriVgroupName: oriData.vgroupName,
      newVgroupName: this.form_page4.get("groupName_page4")!.value,
      oriVgroupAlias: oriData.vgroupAlias,
      newVgroupAlias: this.form_page4.get("groupAlias_page4")!.value,
      oriAllowDays: oriData.allowDays,
      newAllowDays: this.form_page4.get("allowAccessDays_page4")!.value,
      oriTimeUnit: this.translatetime(oriData.timeUnit),
      newTimeUnit: this.translatetime(this.form_page4.get("dayOptionsName_page4")!.value),
      oriAllowTimes: oriData.allowTimes,
      newAllowTimes: this.form_page4.get("allowAccessUseTimes_page4")!.value,
      oriVgroupDesc: oriData.vgroupDesc,
      newVgroupDesc: this.form_page4.get("groupDesc_page4")!.value,
      oriSecurityLevelId: oriData.securityLevelId,
      newSecurityLevelId: this.form_page4.get("securityLevelName_page4")!.value,
      oriVgroupAuthoritieIds: orivgroupAuthorities,
      newVgroupAuthoritieIds: this.groupAuthorities_page4,
      oriApiList: oriApiList,
      newApiList: ApiList,
    } as AA0224Req;

    this.clientService.updateVirtulGroup(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.messageService.add({ severity: 'success', summary: `${dict['message.update']} ${dict['message.virtul_group']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
        this.return_to_list()
        this.submitForm()
      }
    })
  }
  translatetime(select) {
    var str = ""
    if (select == "d") {
      str = this.toolService.Base64Encoder(this.toolService.BcryptEncoder(select)) + ',' + 3
    }
    else if (select == "H") {
      str = this.toolService.Base64Encoder(this.toolService.BcryptEncoder(select)) + ',' + 2
    }
    else if (select == "m") {
      str = this.toolService.Base64Encoder(this.toolService.BcryptEncoder(select)) + ',' + 1
    }
    else if (select == "s") {
      str = this.toolService.Base64Encoder(this.toolService.BcryptEncoder(select)) + ',' + 0
    }
    return str
  }
  async create() {

    const codes = ['button.virtual_group'];
    const dict = await this.tool.getDict(codes);

    this.title = `${this.title} > ${dict['button.virtual_group']}`;
    this.pageNum = 9
    this.dialogTitle = dict['button.virtual_group'];
    this.data_0235_page9 = []

    let ReqBody = {
      encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('TIME_UNIT')) + ',' + 25,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(res_DPB0047 => {
      if (this.toolService.checkDpSuccess(res_DPB0047.ResHeader)) {
        this.clientService.addVirtulGroup_before().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {

            res["RespBody"]["constraints"][0]["field"] = "groupName_page9"
            res["RespBody"]["constraints"][1]["field"] = "groupAlias_page9"
            res["RespBody"]["constraints"][2]["field"] = "allowAccessDays"
            res["RespBody"]["constraints"][3]["field"] = "allowAccessUseTimes"
            res["RespBody"]["constraints"][4]["field"] = "securityLevelName"
            res["RespBody"]["constraints"][5]["field"] = "groupDesc"
            this.addFormValidator(this.form_page9, res.RespBody["constraints"]);
          }
        })
        let Status:{label:string, value:string}[] = [];
        if(res_DPB0047.RespBody.subItems)
        for (let item of res_DPB0047.RespBody.subItems) {
          Status.push({ label: item.subitemName, value: item.subitemNo });
        }
        this.dayOptions_page9 = Status;
        this.form_page9.get("dayOptionsName")!.setValue(Status[0]["value"])
        this.form_page9.get("allowAccessDays")!.setValue('0');
        this.form_page9.get("allowAccessUseTimes")!.setValue('0');
        this.form_page9.get("securityLevelName")!.setValue('SYSTEM');
      }
    })
  }
  async return_to_list_page9() {
    const codes = ['button.virtual_group'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']}`;
    this.rowcount_Authorities_page9 = 0;
    this.dataAuthorities_page9 = [];
    this.selected_page9 = [];
    this.pageNum = 9;
  }
  async Choose_Module_paage9() {
    const codes = ['button.virtual_group', 'button.chs_api_module'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']} > ${dict['button.chs_api_module']}`;
    if (this.data_0235_page9.length > 0) {
      let selectedModuleList:any[] = []
      this.data_0235_page9.forEach(function (item) {
        selectedModuleList.push({ "moduleName": item["label"] })
      })
      this.been_choose_module_list_page11 = selectedModuleList
    }
    else {
      this.been_choose_module_list_page11 = []
    }
    this.pageNum = 11;
    this.call_0233_page11()
  }
  call_0233_page11() {
    this.none_choose_module_list_page11 = []
    this.rowcount_page11 = 0
    let selectedModuleKeyList:any[] = []
    this.been_choose_module_list_page11.forEach(function (item) {
      selectedModuleKeyList.push(item["moduleName"])
    })
    let ReqBody = {
      keyword: this.form_page11.get('groupkeyword')!.value,
      selectedModuleNameList: selectedModuleKeyList
    } as AA0233Req;
    this.clientService.queryModule_0233(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let UnselectedModuleKeyList:any[] = []
        res.RespBody.moduleNameList.forEach(item => {
          UnselectedModuleKeyList.push({ "moduleName": item })
        })
        this.none_choose_module_list_page11 = UnselectedModuleKeyList
        this.last_module_page11 = res.RespBody.moduleNameList[res.RespBody["moduleNameList"].length - 1]
        this.rowcount_page11 = this.none_choose_module_list_page11.length
      }
    });
  }
  Select_Module_page11(Data: Object[]) {
    this.been_choose_module_list_page11.push(Data)
    this.none_choose_module_list_page11.splice(this.none_choose_module_list_page11.indexOf(Data), 1)
    this.rowcount_page11 = this.none_choose_module_list_page11.length
  }
  Unselect_Module_page11(Data: Object[]) {
    this.none_choose_module_list_page11.push(Data)
    this.been_choose_module_list_page11.splice(this.been_choose_module_list_page11.indexOf(Data), 1)
    this.rowcount_page11 = this.none_choose_module_list_page11.length
  }
  select_all_page11() {
    this.none_choose_module_list_page11.forEach(element => {
      this.been_choose_module_list_page11.push(element)
    });
    this.none_choose_module_list_page11 = []
    this.rowcount_page11 = this.none_choose_module_list_page11.length
  }
  moreData_page11() {
    let selectedModuleKeyList:any[] = []
    this.been_choose_module_list_page11.forEach(function (item) {
      selectedModuleKeyList.push(item["moduleName"])
    })
    let ReqBody = {
      moduleName: this.last_module_page11,
      keyword: this.form_page11.get('groupkeyword')!.value,
      selectedModuleNameList: selectedModuleKeyList
    } as AA0233Req;
    this.clientService.queryModule_0233(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const UnselectedModuleKeyList:any[] = []
        res.RespBody.moduleNameList.forEach(item => {
          UnselectedModuleKeyList.push({ "moduleName": item })
        })
        this.none_choose_module_list_page11 = this.none_choose_module_list_page11.concat(UnselectedModuleKeyList)
        this.last_module_page11 = res.RespBody.moduleNameList[res.RespBody["moduleNameList"].length - 1]
        this.rowcount_page11 = this.none_choose_module_list_page11.length
      }
    });
  }
  async save_choose_Module_page11() {
    const codes = ['button.virtual_group'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']}`;
    this.rowcount_Authorities_page9 = 0;
    this.dataAuthorities_page9 = [];
    this.selected_page9 = [];

    var temp_data:any[] = []
    if (this.been_choose_module_list_page11.length > 0) {
      this.been_choose_module_list_page11.forEach(item => {
        if (this.data_0235_page9.length > 0) {
          for (var i = 0; i < this.data_0235_page9.length; i++) {
            if (item.moduleName == this.data_0235_page9[i]["label"]) {
              temp_data.push(this.data_0235_page9[i])
              break;
            }
            if (i == this.data_0235_page9.length - 1) {
              temp_data.push({ label: item.moduleName, value: { apiList: [], moduleName: item.moduleName }, apilist: "" })
            }
          }
        }
        else {
          temp_data.push({ label: item.moduleName, value: { apiList: [], moduleName: item.moduleName }, apilist: "" })
        }
      })
    }
    else {
      this.been_choose_module_list_page11.forEach(item => {
        temp_data.push({ label: item.moduleName, value: { apiList: [], moduleName: item.moduleName }, apilist: "" })
      })
    }
    this.data_0235_page9 = temp_data
    this.been_choose_module_list_page11 = []
    this.none_choose_module_list_page11 = []
    this.form_page11.get('groupkeyword')!.setValue("")
    this.pageNum = 9;
  }
  async Choose_API_page9(Data: Object[]) {
    const codes = ['button.virtual_group', 'button.chs_api'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']} > ${dict['button.chs_api']}`;
    this.been_choose_api_list_page12 = Data["value"]
    this.form_page12.get("moduleName")!.setValue(Data["label"])
    this.pageNum = 12;
    this.call_0234_page12()
  }
  call_0234_page12() {
    this.none_choose_api_list_page12 = [];
    this.rowcount_page12 = 0
    let selectedApiKeyList:any[] = []
    if (this.been_choose_api_list_page12["apiList"] && this.been_choose_api_list_page12["apiList"].length == 0)
      this.been_choose_api_list_page12 = []

    if (this.been_choose_api_list_page12.length > 0) {
      this.been_choose_api_list_page12.forEach(function (item) {
        selectedApiKeyList.push(item["apiKey"])
      })
    }
    let ReqBody = {
      moduleName: this.form_page12.get('moduleName')!.value,
      keyword: this.form_page12.get('groupkeyword')!.value,
      selectedApiKeyList: selectedApiKeyList
    } as AA0234Req;
    this.clientService.queryapi_0234(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.none_choose_api_list_page12 = res.RespBody["apiList"]
        this.last_api_page12 = res.RespBody["apiList"][res.RespBody["apiList"].length - 1]["apiKey"]
        this.rowcount_page12 = this.none_choose_api_list_page12.length
      }
    });
  }
  Select_API_page12(Data: Object[]) {
    this.been_choose_api_list_page12.push(Data)
    this.none_choose_api_list_page12.splice(this.none_choose_api_list_page12.indexOf(Data), 1)
    this.rowcount_page12 = this.none_choose_api_list_page12.length
  }
  Unselect_API_page12(Data: Object[]) {
    if (!this.none_choose_api_list_page12)
      this.none_choose_api_list_page12 = []
    this.none_choose_api_list_page12.push(Data)
    this.been_choose_api_list_page12.splice(this.been_choose_api_list_page12.indexOf(Data), 1)
    this.rowcount_page12 = this.none_choose_api_list_page12.length
  }
  select_all_page12() {
    this.none_choose_api_list_page12.forEach(element => {
      this.been_choose_api_list_page12.push(element)
    });
    this.none_choose_api_list_page12 = []
    this.rowcount_page12 = this.none_choose_api_list_page12.length
  }
  moreData_page12() {
    let selectedApiKeyList:any[] = []
    this.been_choose_api_list_page12.forEach(function (item) {
      selectedApiKeyList.push(item["apiKey"])
    })
    let ReqBody = {
      apiKey: this.last_api_page12,
      moduleName: this.form_page12.get('moduleName')!.value,
      keyword: this.form_page12.get('groupkeyword')!.value,
      selectedApiKeyList: selectedApiKeyList
    } as AA0234Req;
    this.clientService.queryapi_0234(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.none_choose_api_list_page12 = this.none_choose_api_list_page12.concat(res.RespBody["apiList"])
        this.last_api_page12 = res.RespBody["apiList"][res.RespBody["apiList"].length - 1]["apiKey"]
        this.rowcount_page12 = this.none_choose_api_list_page12.length
      }
    });
  }
  async save_choose_API_page12() {
    const codes = ['button.virtual_group'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']}`;
    this.rowcount_Authorities_page9 = 0;
    this.dataAuthorities_page9 = [];
    this.selected_page9 = [];

    this.data_0235_page9.forEach(item => {
      if (item.label == this.form_page12.get('moduleName')!.value) {
        item["value"] = this.been_choose_api_list_page12
        var apilist = ""
        this.been_choose_api_list_page12.forEach(api_element => {
          if (api_element["apiName"]) {
            apilist += `${api_element["apiKey"]}(${api_element["apiName"]})` + "、"
          }
          else {
            apilist += `${api_element["apiKey"]}()` + "、"
          }
        });
        if (apilist != "")
          apilist = apilist.slice(0, apilist.length - 1)
        item["apilist"] = apilist
      }
    })
    this.been_choose_api_list_page12 = []
    this.none_choose_api_list_page12 = []
    this.form_page12.get('groupkeyword')!.setValue("")
    this.pageNum = 9;
  }


  async queryAuthoritiesList_page9() {
    const codes = ['authorities'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title} > ${dict['authorities']}`;
    this.pageNum = 10;
    this.queryauthorities_page9()
  }
  queryauthorities_page9() {

    this.rowcount_Authorities_page9 = 0;
    this.dataAuthorities_page9 = [];

    let ReqBody = {
      keyword: this.form_page10.get('authoritieskeyword')!.value,
    } as AA1115Req;

    this.groupAuthService.queryScopeAuthorities(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rowcount_Authorities_page9 = res.RespBody.groupAuthoritiesList.length;
        this.dataAuthorities_page9 = res.RespBody.groupAuthoritiesList;
      }

    });
  }

  async chooseAuthorities_page9() {
    const codes = ['button.virtual_group'];
    const dict = await this.tool.getDict(codes);
    this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']}`;
    if (this.selected_page9) {
      var arr_id:any[] = []
      var arr_name:any[] = []
      this.selected_page9.forEach(function (item) {
        arr_id.push(item.groupAuthoritiesId)
        arr_name.push(item.groupAuthoritiesName)
      })
      this.groupAuthorities_page9 = arr_id
      this.form_page9.get('groupAuthoritiesName')!.setValue(arr_name)
    }
    else {
      this.form_page9.get('groupAuthoritiesName')!.setValue("")
    }
    this.rowcount_Authorities_page9 = 0;
    this.dataAuthorities_page9 = [];
    this.selected_page9 = [];
    this.pageNum = 9;
  }
  async submitForm_page9() {
    const codes = ['message.virtul_group', 'message.create', 'message.success'];
    const dict = await this.tool.getDict(codes);
    let ApiList:any[] = []

    this.data_0235_page9.forEach(item => {
      let apiKeyList:any[] = []
      if (item["apilist"] != "") {
        item["value"].forEach(element => {
          apiKeyList.push(element["apiKey"])
        });
        ApiList.push({ moduleName: item["label"], apiKeyList: apiKeyList })
      }
      // else{
      //     ApiList.push({ moduleName: item["label"], apiKeyList: [] })
      // }
    })

    let ReqBody = {
      vgroupName: this.form_page9.get("groupName_page9")!.value,
      vgroupAlias: this.form_page9.get("groupAlias_page9")!.value,
      allowDays: this.form_page9.get("allowAccessDays")!.value,
      timeUnit: this.translatetime(this.form_page9.get("dayOptionsName")!.value),
      allowTimes: this.form_page9.get("allowAccessUseTimes")!.value,
      vgroupDesc: this.form_page9.get("groupDesc")!.value,
      securityLevelId: this.form_page9.get("securityLevelName")!.value,
      vgroupAuthoritieIds: this.groupAuthorities_page9,
      dataList: ApiList
    } as AA0221Req;

    this.clientService.addVirtulGroup(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.form_page9.get("groupName_page9")!.setValue("")
        this.form_page9.get("groupAlias_page9")!.setValue("")
        this.form_page9.get("allowAccessDays")!.setValue("")
        this.form_page9.get("dayOptionsName")!.setValue("")
        this.form_page9.get("allowAccessUseTimes")!.setValue("")
        this.form_page9.get("groupDesc")!.setValue("")
        this.form_page9.get("securityLevelName")!.setValue("")
        this.form_page9.get("groupAuthoritiesName")!.setValue([])
        this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.virtul_group']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
        this.return_to_list()
        this.submitForm()
      }
    })
  }

  //刪除群組
  delete_group() {
    let ReqBody = {
      vgroupId: this.form_page8.get("groupID")!.value,
    } as AA0225Req;

    this.clientService.deleteVirtulGroup(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['message.delete', 'message.success', 'message.user', 'message.virtul_group'];
        const dict = await this.toolService.getDict(code);

        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.messageService.add({
            severity: 'success', summary: `${dict['message.delete']} ${dict['message.virtul_group']}`,
            detail: `${dict['message.delete']} ${dict['message.success']}!`
          });

          this.return_to_list()
          this.submitForm()
        }
      }
    })
  }

  async copyInfoMsg(infoMsg: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.tool.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = infoMsg;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.messageService.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  async headerReturn() {
    if(this.pageNum == 5){
      this.return_to_list_page4();
    }
    else if(this.pageNum == 6 || this.pageNum == 7){
      const codes = ['dialog.virtul_group_update'];
      const dict = await this.tool.getDict(codes);
      this.title = `${this.title.split('>')[0]} > ${dict['dialog.virtul_group_update']}`;
      this.pageNum = 4;
    }
    else if(this.pageNum == 10)
    {
      this.return_to_list_page9();
    }
    else if(this.pageNum == 11 || this.pageNum == 12)
    {
      const codes = ['button.virtual_group'];
      const dict = await this.tool.getDict(codes);
      this.title = `${this.title.split('>')[0]} > ${dict['button.virtual_group']}`;
      this.pageNum = 9;
    }
    else
      this.return_to_list();


  }

  public get groupName_page9() { return this.form_page9.get('groupName_page9'); };
  public get groupAlias_page9() { return this.form_page9.get('groupAlias_page9'); };
  public get allowAccessDays() { return this.form_page9.get('allowAccessDays'); };
  public get allowAccessUseTimes() { return this.form_page9.get('allowAccessUseTimes'); };
  public get securityLevelName() { return this.form_page9.get('securityLevelName'); };
  public get groupDesc() { return this.form_page9.get('groupDesc'); };
  public get dayOptionsName() { return this.form_page9.get('dayOptionsName'); };

  public get groupName_page4() { return this.form_page4.get('groupName_page4'); };
  public get groupAlias_page4() { return this.form_page4.get('groupAlias_page4'); };
  public get allowAccessDays_page4() { return this.form_page4.get('allowAccessDays_page4'); };
  public get allowAccessUseTimes_page4() { return this.form_page4.get('allowAccessUseTimes_page4'); };
  public get securityLevelName_page4() { return this.form_page4.get('securityLevelName_page4'); };
  public get groupDesc_page4() { return this.form_page4.get('groupDesc_page4'); };
  public get dayOptionsName_page4() { return this.form_page4.get('dayOptionsName_page4'); };
  // public get groupID() { return this.form_page4.get('vgroupId'); };
  // public get oriVgroupName() { return this.form_page4.get('oriVgroupName'); };
}
