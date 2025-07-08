import { UserService } from 'src/app/shared/services/api-user.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ClientService } from './../../../shared/services/api-client.service';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { AA0202List, AA0202Req } from '../../../models/api/ClientService/aa0202.interface';
import { FormOperate } from '../../../models/common.enum';
import { AA0203GroupInfo, AA0203Req, AA0203Resp, AA0203VgroupInfo } from '../../../models/api/ClientService/aa0203.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import * as ValidatorFns from '../../../shared/validator-functions';
import { AA0201HostReq, AA0201Req } from 'src/app/models/api/ClientService/aa0201.interface';
import * as dayjs from 'dayjs';
import * as utc from 'dayjs/plugin/utc'
import { isArray, isDate, isNullOrUndefined } from 'util';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AA0204Req } from 'src/app/models/api/ClientService/aa0204.interface';
import { AA0205Req } from 'src/app/models/api/ClientService/aa0205.interface';
import { GroupAuthService } from 'src/app/shared/services/api-group-auth.service';
import { AA0217Req } from 'src/app/models/api/ClientService/aa0217.interface';
import { AA0228GroupInfo, AA0228Req } from 'src/app/models/api/ClientService/aa0228.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import { AA0227Req } from 'src/app/models/api/ClientService/aa0227.interface';
import { AA0229Req, AA0229VgroupInfo } from 'src/app/models/api/ClientService/aa0229.interface';
import { AA0226Req } from 'src/app/models/api/ClientService/aa0226.interface';
import { AA0230Req } from 'src/app/models/api/ClientService/aa0230.interface';
import { AA0219Req } from 'src/app/models/api/ClientService/aa0219.interface';
import { AA0218Req } from 'src/app/models/api/ClientService/aa0218.interface';
import { AA0220Req } from 'src/app/models/api/ClientService/aa0220.interface';
import { AA0231Req } from 'src/app/models/api/ClientService/aa0231.interface';
import { AA0216Req } from 'src/app/models/api/ClientService/aa0216.interface';
import { AA0238Req, GroupInfo_0238 } from 'src/app/models/api/ClientService/aa0238.interface';
import { of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { DPB0207Req, DPB0207RespItem } from 'src/app/models/api/ServerService/dpb0207.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ManagerGroupListComponent } from 'src/app/shared/manager-group-list/manager-group-list.component';
import { DialogService } from 'primeng/dynamicdialog';
import { DPB0208Req } from 'src/app/models/api/ServerService/dpb0208.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
    selector: 'app-ac0202',
    templateUrl: './ac0202.component.html',
    styleUrls: ['./ac0202.component.css'],
    providers: [ClientService, UserService, ConfirmationService],
})
export class Ac0202Component extends BaseComponent implements OnInit {

    form: FormGroup;
    formOperate = FormOperate;
    queryStatusOpt: { label: string; value: string; }[] = [];
    data: Array<AA0202List> = [];
    detail?: AA0203Resp;
    canQuery: boolean = false;
    canCreate: boolean = false;
    canDetail: boolean = false;
    canUpdate: boolean = false;
    // canSecurity: boolean = false;
    canDelete: boolean = false;
    cols: { field: string; header: string }[] =[];
    rowcount: number= 0;
    pageNum: number = 1; // 1: 搜尋、2: 基本資料建立、3: 基本資料更新、4: 刪除、5: 安全更新、6: 詳細資料、7: 群組列表、8: 虛擬群組列表、9: 搜尋時的群組列表
    currentTitle: string = this.title;
    currentAction: string = 'query';
    currentClient?: AA0202List;
    clientDetail?: AA0203Resp;
    clientDetailGroupList: Array<string> = new Array<string>();
    publicFlags: { label: string; value: string; }[] = [];
    clientDetailStatusOpt: { label: string; value: string; }[] = [];
    clientDetailHostCols: { field: string; header: string; }[] = [];
    maxlength50 = { value: 50 };
    maxlength100 = { value: 100 };
    maxlength256 = { value: 256 };
    maxlength255 = { value: 255 };
    maxlength500 = { value: 500 };
    priorities: Array<{ label: string; value: number; }> = new Array();
    createStatusOpt: { label: string; value: string; }[] = [];
    securityLevelOptions: { label: string; value: string; disabled: boolean; }[] = [];
    clientGroupListCols: { field: string; header: string; }[] = [];
    clientGroupList: Array<AA0203GroupInfo> = new Array<AA0203GroupInfo>();
    groupInfoListCols: { field: string; header: string; }[] = [];
    groupInfoList: Array<AA0228GroupInfo> = new Array<AA0228GroupInfo>();
    groupInfoListRowcount: number = 0;
    selectedGroups: Array<AA0228GroupInfo> = new Array<AA0228GroupInfo>();
    currentDeleteGroup?: AA0228GroupInfo;
    clientVGroupListCols: { field: string; header: string; }[] = [];
    clientVGroupList: Array<AA0203VgroupInfo> = new Array<AA0203VgroupInfo>();
    vgroupInfoListCols: { field: string; header: string; }[] = [];
    vgroupInfoList: Array<AA0229VgroupInfo> = new Array<AA0229VgroupInfo>();
    vgroupInfoListRowcount: number = 0;
    selectedVGroups: Array<AA0229VgroupInfo> = new Array<AA0229VgroupInfo>();
    currentDeleteVGroup?: AA0229VgroupInfo;
    grantTypeOptions: { label: string; value: string }[] = [];
    expireTimeOptions: { label: string; value: number }[] = [];
    tokenExpiryTimeOpt: { label: string; value: string }[] = [];
    updateStatusOpt: { label: string; value: string; }[] = [];
    securityTab: boolean = true;
    groupTab: boolean = false;
    vgroupTab: boolean = false;
    tokenTab: boolean = false;
    statusTab: boolean = false;
    pwdTab: boolean = false;
    xapikeyTab:boolean = false;
    groupListDataCols: { field: string; header: string; }[] = [];
    groupListData: Array<GroupInfo_0238> = new Array<GroupInfo_0238>();
    groupListDataRowcount: number = 0;
    selectedGroup?: GroupInfo_0238;

    timeZoneList: { label: string; value: string }[] = [];

    xApiKeyList:Array<DPB0207RespItem> = [];
    xApiKeyCreate:boolean = false;
    formX: FormGroup;

    minDateEff:Date = new Date();
    minDateExp:Date = new Date();


    newClientBlockLimitChar = { value: 256 };


    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private clientService: ClientService,
        private toolService: ToolService,
        private messageService: MessageService,
        private translate: TranslateService,
        private list: ListService,
        private alert: AlertService,
        private groupAuthService: GroupAuthService,
        private roleService: RoleService,
        private confirmationService: ConfirmationService,
        private alertService:AlertService,
        private serverService: ServerService,
        private dialogService: DialogService,
        private ngxSrvice: NgxUiLoaderService
    ) {
        super(route, tr);
        dayjs.extend(utc);

        this.formX = this.fb.group({
          clientId: new FormControl(''),
          apiKeyAlias: new FormControl(''),
          effectiveAt: new FormControl(''),
          expiredAt: new FormControl(''),
          groupIdList: new FormControl([]),
        })

        this.form = this.fb.group({
          keyword: new FormControl(''),
          groupID: new FormControl(''),
          groupName: new FormControl({ value: '', disabled: true }),
          encodeStatus: new FormControl('-1'),
          clientID: new FormControl(''),
          clientName: new FormControl(''),
          signupNum: new FormControl(''),
          clientAlias: new FormControl(''),
          clientBlock: new FormControl(''),
          confirmClientBlock: new FormControl(''),
          hostList: new FormControl([]),
          clientStartDate: new FormControl(),
          clientEndDate: new FormControl(),
          svcST: new FormControl({ hour: 0, minute: 0 }),
          svcET: new FormControl({ hour: 0, minute: 0 }),
          apiQuota: new FormControl(0),
          tps: new FormControl(10),
          cPriority: new FormControl(5),
          owner: new FormControl(''),
          createStatus: new FormControl(''),
          publicFlag: new FormControl('2'),
          emails: new FormControl(''),
          newClientName: new FormControl(''),
          newClientAlias: new FormControl(''),
          newSignupNum: new FormControl(''),
          newHostList: new FormControl(''),
          newClientStartDate: new FormControl(''),
          newClientEndDate: new FormControl(''),
          newSvcST: new FormControl({ hour: 0, minute: 0 }),
          newSvcET: new FormControl({ hour: 0, minute: 0 }),
          newApiQuota: new FormControl(0),
          newTps: new FormControl(10),
          newCPriority: new FormControl(5),
          newOwner: new FormControl(''),
          newEncodePublicFlag: new FormControl(''),
          newEmails: new FormControl(''),
          newSecurityID: new FormControl(''),
          authorizedGrantType: new FormControl(),
          accessTokenValidity: new FormControl(),
          raccessTokenValidity: new FormControl(''),
          webServerRedirectUri: new FormControl(''),
          webServerRedirectUri1: new FormControl(''),
          webServerRedirectUri2: new FormControl(''),
          webServerRedirectUri3: new FormControl(''),
          webServerRedirectUri4: new FormControl(''),
          webServerRedirectUri5: new FormControl(''),
          accessTokenValidityTimeUnit: new FormControl('s'),
          raccessTokenValidityTimeUnit: new FormControl('s'),
          updateStatus: new FormControl(''),
          resetFailLoginTreshhold: new FormControl(''),
          resetPwdFailTimes: new FormControl(false),
          newClientBlock: new FormControl(''),
          resetBlock: new FormControl(false),
          confirmNewClientBlock: new FormControl(''),
          accessTokenQuota: new FormControl(0),
          refreshTokenQuota: new FormControl(0),
          remark: new FormControl(''),
          newRemark: new FormControl(''),
          clientStartTimePerDay: new FormControl(),
          newClientStartTimePerDay: new FormControl(),
          clientEndTimePerDay: new FormControl(),
          newClientEndTimePerDay: new FormControl(),
          timeZone: new FormControl(),
          newTimeZone: new FormControl(),
      });
    }

    async getDicts() {
        return await this.translate.get(['active', 'inactive', 'disable', 'lock', 'client_id', 'client_name', 'client_alias', 'security_level', 'status', 'dialog.client_update', 'dialog.client_delete',
            'dialog.client_security_setting', 'message.create', 'message.client', 'message.setting', 'message.security', 'message.delete', 'message.success', 'api_audience', 'message.update', 'no', 'host_name',
            'host_ip', 'message.service_time_error', 'service_time', 'data', 'error', 'button.create', 'button.update', 'button.detail', 'button.delete', 'button.security', 'group_alias', 'group_desc', 'security_level',
            'group_list', 'virtul_group_list', 'virtul_group_alias', 'virtul_group_desc', 'cfm_del_client', 'grant_type_options.authorization_code', 'grant_type_options.client_credentials', 'grant_type_options.refresh_token', 'grant_type_options.password', 'grant_type_options.implicit', 'grant_type_options.public', 'token_exp_time_options.day', 'token_exp_time_options.hour', 'token_exp_time_options.minute', 'token_exp_time_options.second', 'grant_type_options.smsotp', 'grant_type_options.group',
             'group_name', 'virtul_group_name', 'button.manager_oauth_group', 'button.manager_oauth_virtual_group','api_scope_desc', 'date_range_required_if_exist', 'message.service_time_error',
             'sdate_over_edate', 'stime_over_etime','timezone_required','button.disable'
        ]).toPromise()
    }

    async ngOnInit() {
        // status
        let ReqBody = {
            encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('ENABLE_FLAG')) + ',' + 9,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                let _queryStatusOpt:{label:string, value:string}[] = [];
                let _createStatusOpt:{label:string, value:string}[] = [];
                let _updateStatusOpt:{label:string, value:string}[] = [];

                res.RespBody.subItems?.map(item => {
                    _queryStatusOpt.push({ label: item.subitemName, value: item.subitemNo });
                    if (item.subitemNo != '-1' && item.subitemNo != '2') { // 只留啟用、停用
                        _createStatusOpt.push({ label: item.subitemName, value: item.param1??'' });
                    }
                    if (item.subitemNo != '-1') { // 不要全部
                        _updateStatusOpt.push({ label: item.subitemName, value: item.param1??'' });
                    }
                });

                this.queryStatusOpt = _queryStatusOpt;
                this.createStatusOpt = _createStatusOpt;
                this.updateStatusOpt = _updateStatusOpt;
            }
        });
        // publicFlag
        let publicFlagReqBody = {
            encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('API_AUTHORITY')) + ',' + 7,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(publicFlagReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                let _publicFlags:{label:string, value:string}[] = [];
                res.RespBody.subItems?.map(item => {
                    if (item.subitemNo != '-1') {
                        _publicFlags.push({ label: item.subitemName, value: item.subitemNo });
                    }
                });
                this.publicFlags = _publicFlags;
            }
        });
        // 處理priority
        // 只需要0/5/9三個等級  20221018修改
        for (let i = 0; i < 10; i++) {
            let key = i.toString();
            if (i == 0) { key = i + " (high) "; this.priorities.push({ label: key, value: i });}
            if (i == 5) { key = i + " (default) ";   this.priorities.push({ label: key, value: i });}
            if (i == 9) { key = i + " (low) ";  this.priorities.push({ label: key, value: i }); }
        }
        // 功能權限
        this.roleService.queryRTMapByUk({ txIdList: ['AA0202', 'AA0201', 'AA0203', 'AA0204', 'AA0205'] } as DPB0115Req).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.canQuery = res.RespBody.dataList.find(item => item.txId === 'AA0202') ?  res.RespBody.dataList.find(item => item.txId === 'AA0202')!.available : false;
                this.canCreate = res.RespBody.dataList.find(item => item.txId === 'AA0201') ? res.RespBody.dataList.find(item => item.txId === 'AA0201')!.available : false;
                this.canDetail = res.RespBody.dataList.find(item => item.txId === 'AA0203') ? res.RespBody.dataList.find(item => item.txId === 'AA0203')!.available : false;
                this.canUpdate = res.RespBody.dataList.find(item => item.txId === 'AA0204') ? res.RespBody.dataList.find(item => item.txId === 'AA0204')!.available : false;
                // this.canSecurity = res.RespBody.dataList.find(item => item.txId === 'DPB0099').available;
                this.canDelete = res.RespBody.dataList.find(item => item.txId === 'AA0205') ? res.RespBody.dataList.find(item => item.txId === 'AA0205')!.available : false;
            }
        });
        // token expiry
        let tokenExpiryTimeOptReqBody = {
            encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('TIME_UNIT')) + ',' + 25,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(tokenExpiryTimeOptReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                let _tokenExpiryTimes:{label:string, value:string}[] = [];
                res.RespBody.subItems?.map(item => {
                    _tokenExpiryTimes.push({ label: item.subitemName, value: item.subitemNo })
                });
                this.tokenExpiryTimeOpt = _tokenExpiryTimes;
            }
        });
        this.resetBlock!.valueChanges.subscribe(flag => {
            if (flag) {
                this.clientBlock!.setValue('');
                this.newClientBlock!.setValue('');
                this.confirmNewClientBlock!.setValue('');
                this.clientBlock!.disable();
                this.newClientBlock!.disable();
                this.confirmNewClientBlock!.disable();
            }
            else {
                this.clientBlock!.enable();
                this.newClientBlock!.enable();
                this.confirmNewClientBlock!.enable();
            }
        });
        this.init();

        this.effectiveAt.valueChanges.subscribe( res => {

          if(res){
            this.minDateExp = new Date(res);
          }else{
            this.minDateEff = new Date();
            this.minDateExp = new Date();
          }
          if(this.minDateExp > this.expiredAt.value){
            this.expiredAt.setValue('');
          }
        })

        // this.expiredAt.valueChanges.subscribe( res => {
        //   if(res){

        //   }else{

        //   }
        // })
    }

    async init() {
        const dicts = await this.getDicts();
        this.clientDetailHostCols = [
            { field: 'hostSeq', header: dicts['no'] },
            { field: 'hostName', header: dicts['host_name'] },
            { field: 'hostIP', header: dicts['host_ip'] },
        ];
        this.clientDetailStatusOpt = [
            { label: dicts['active'], value: '1' },
            { label: dicts['button.disable'], value: '2' },
            { label: dicts['lock'], value: '3' },
        ];

        this.cols = [
            { field: 'clientId', header: dicts['client_id'] },
            { field: 'clientName', header: dicts['client_name'] },
            { field: 'clientAlias', header: dicts['client_alias'] },
            { field: 'securityLevelName', header: dicts['security_level'] },
            { field: 'publicFlagName', header: dicts['api_audience'] },
            { field: 'statusName', header: dicts['status'] }
        ];
        this.groupListDataCols = [
            { field: 'groupName', header: `${dicts['group_name']} - (${dicts['group_alias']})` },
            { field: 'groupDesc', header: dicts['group_desc'] },
            { field: 'securityLevelName', header: dicts['security_level'] }
        ];
        this.clientGroupListCols = [
            { field: 'groupName', header: `${dicts['group_name']} - (${dicts['group_alias']})` },
            { field: 'groupDesc', header: dicts['group_desc'] },
            { field: 'securityLevelName', header: dicts['security_level'] }
        ];
        this.groupInfoListCols = [
            { field: 'groupName', header: `${dicts['group_name']} - (${dicts['group_alias']})` },
            { field: 'groupDesc', header: dicts['group_desc'] },
            { field: 'securityLevelName', header: dicts['security_level'] }
        ];
        this.clientVGroupListCols = [
            { field: 'vgroupName', header: `${dicts['virtul_group_name']} - (${dicts['virtul_group_alias']})` },
            { field: 'vgroupDesc', header: dicts['api_scope_desc'] },
            { field: 'securityLevelName', header: dicts['security_level'] }
        ];
        this.vgroupInfoListCols = [
            { field: 'vgroupName', header: `${dicts['virtul_group_name']} - (${dicts['virtul_group_alias']})` },
            { field: 'vgroupDesc', header: dicts['virtul_group_desc'] },
            { field: 'securityLevelName', header: dicts['security_level'] }
        ];
        this.grantTypeOptions = [
            { label: dicts['grant_type_options.authorization_code'], value: 'authorization_code' },
            { label: dicts['grant_type_options.client_credentials'], value: 'client_credentials' },
            { label: dicts['grant_type_options.refresh_token'], value: 'refresh_token' },
            { label: dicts['grant_type_options.password'], value: 'password' },
            { label: dicts['grant_type_options.public'], value: 'public' },
            { label: dicts['grant_type_options.implicit'], value: 'implicit' },
            { label: dicts['grant_type_options.smsotp'], value: 'smsotp' },
            { label: dicts['grant_type_options.group'], value: 'group' },
        ];
        this.expireTimeOptions = [
            { label: dicts['token_exp_time_options.day'], value: 86400 },
            { label: dicts['token_exp_time_options.hour'], value: 3600 },
            { label: dicts['token_exp_time_options.minute'], value: 60 },
            { label: dicts['token_exp_time_options.second'], value: 1 }
        ];
        this.data = [];
        this.rowcount = this.data.length;
        if (this.encodeStatus!.value == '') {
            this.encodeStatus!.setValue('-1');
        }
        let ReqBody = {
            keyword: this.keyword!.value,
            groupID: this.groupID!.value,
            encodeStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.encodeStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.encodeStatus!.value, 'query')
        } as AA0202Req;
        if (ReqBody.groupID == null) ReqBody.groupID = '';
        //預埋api
        this.clientService.queryClientList_ignore1298(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.data = res.RespBody.clientInfoList;
                this.rowcount = this.data.length;
            }
        });
    }

    queryGroupListData() {
        this.selectedGroup = {} as GroupInfo_0238;
        this.groupListData = [];
        this.groupListDataRowcount = this.groupListData.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as AA0238Req;
        this.clientService.queryGroupList_0238(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.groupListData = res.RespBody.groupInfoList;
                this.groupListDataRowcount = this.groupListData.length;
            }
        });
    }

    moreGroupListData() {
        let ReqBody = {
            groupId: this.groupListData[this.groupListData.length - 1].groupID,
            keyword: this.keyword!.value
        } as AA0238Req;
        this.clientService.queryGroupList_0238(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.groupListData = this.groupListData.concat(res.RespBody.groupInfoList);
                this.groupListDataRowcount = this.groupListData.length;
            }
        });
    }

    async chooseGroup() {
        const dicts = await this.getDicts();
        this.currentTitle = this.title;
        this.pageNum = 1;
        this.encodeStatus!.setValue('-1');
        if (this.selectedGroup) {
            this.groupID!.setValue(this.selectedGroup.groupID);
            this.groupName!.setValue(this.selectedGroup.groupName);
        }
    }

    // 查詢用戶端列表
    submitForm() {
        this.data = [];
        this.rowcount = this.data.length;
        if (!this.encodeStatus!.value || this.encodeStatus!.value == '') {
            this.encodeStatus!.setValue('-1');
        }
        let ReqBody = {
            keyword: this.keyword!.value,
            groupID: this.groupID!.value,
            encodeStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.encodeStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.encodeStatus!.value, 'query')
        } as AA0202Req;
        if (ReqBody.groupID == null) ReqBody.groupID = '';
        //預埋api
        this.clientService.queryClientList(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.data = res.RespBody.clientInfoList;
                this.rowcount = this.data.length;
            }
        });
    }

    moreData() {
        let req = {
            clientId: this.data[this.data.length - 1].clientId,
            keyword: this.keyword!.value,
            groupID: this.groupID!.value,
            encodeStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.encodeStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.encodeStatus!.value, 'query')
        } as AA0202Req;
        if (req.groupID == null) req.groupID = '';
        //預埋api
        this.clientService.queryClientList(req).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.data = this.data.concat(res.RespBody.clientInfoList);
                this.rowcount = this.data.length;
            }
        });
    }

    isNullUndefinedNoneCheck(tar:any){
      return (tar===null||tar === undefined||tar === '')
    }

    checkHostListValue(_hostList: AA0201HostReq[]){
      let _resultHostList: AA0201HostReq[] = [];

      _resultHostList = _hostList
        .filter((item) => !(item['hostName'] == '' && item['hostIP'] == ''))
        .map((item) => {
          return {
            hostName: item.hostName,
            hostIP: item.hostIP,
          } as AA0201HostReq;
        });


      return _resultHostList;
    }

    // 建立用戶端
    async createClientData() {

        const dicts = await this.getDicts();
        let ReqBody = {
            clientID: this.clientID!.value != '' ? this.clientID!.value : this.toolService.Base64Encoder(this.clientName!.value),
            clientName: this.clientName!.value,
            clientAlias: this.clientAlias!.value,
            signupNum: this.signupNum!.value,
            clientBlock: this.toolService.Base64Encoder(this.clientBlock!.value),
            emails: this.emails!.value,
            tps: this.tps!.value ? this.tps!.value.toString() : '',
            encodeStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.createStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.createStatus!.value, 'create'),
            publicFlag: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.publicFlag!.value)) + ',' + this.publicFlag!.value,
            // hostList: isArray(this.hostList!.value) ? this.hostList!.value : [],
            hostList: this.checkHostListValue(this.hostList!.value),
            // clientSD: isDate(this.clientSD!.value) ? dayjs(this.clientSD!.value).format('YYYY-MM-DD') : '',
            // clientED: isDate(this.clientED!.value) ? dayjs(this.clientED!.value).format('YYYY-MM-DD') : '',
            apiQuota: this.apiQuota!.value ? this.apiQuota!.value.toString() : '',
            owner: this.owner!.value,
            cPriority: this.cPriority!.value.toString(),
            remark: this.remark!.value,
        } as AA0201Req;

        if( !this.isNullUndefinedNoneCheck(this.clientStartDate!.value) || !this.isNullUndefinedNoneCheck(this.clientEndDate!.value))
      {

        if( this.isNullUndefinedNoneCheck(this.clientStartDate!.value)|| this.isNullUndefinedNoneCheck(this.clientEndDate!.value))
        {
          this.alertService.ok(dicts['date_range_required_if_exist'],'');
          return;
        }

        if(new Date(this.clientEndDate!.value).getTime()<new Date(this.clientStartDate!.value).getTime())
        {
          this.alertService.ok(dicts['sdate_over_edate'],'');
          return;
        }

        ReqBody.clientStartDate = dayjs.utc(this.clientStartDate!.value).format();
        ReqBody.clientEndDate = dayjs.utc(this.clientEndDate!.value).format();
        ReqBody.timeZone = this.toolService.getTimeZone();

      }

      if(!isNaN(this.svcST!.value.hour) || !isNaN(this.svcST!.value.minute) || !isNaN(this.svcET!.value.hour) || !isNaN(this.svcET!.value.minute)){
        if(isNaN(this.svcST!.value.hour) || isNaN(this.svcST!.value.minute) || isNaN(this.svcET!.value.hour) || isNaN(this.svcET!.value.minute))
        {
         this.alertService.ok(dicts['message.service_time_error'],'');
         return;
        }

        let sTime = new Date();
        sTime.setHours(this.svcST!.value.hour);
        sTime.setMinutes(this.svcST!.value.minute);
        sTime.setSeconds(0);

        let eTime = new Date();
        eTime.setHours(this.svcET!.value.hour);
        eTime.setMinutes(this.svcET!.value.minute);
        eTime.setSeconds(0);

        if(new Date(eTime).getTime()<new Date(sTime).getTime())
        {
          this.alertService.ok(dicts['stime_over_etime'],'');
          return;
        }

        ReqBody.clientStartTimePerDay = dayjs.utc(sTime).format();
        ReqBody.clientEndTimePerDay = dayjs.utc(eTime).format();
        ReqBody.timeZone = this.toolService.getTimeZone();

     }

        // if (!isNaN(this.svcST!.value.hour) && !isNaN(this.svcST!.value.minute)) {
        //     if (this.svcST!.value.hour + this.svcST!.value.minute == 0) {
        //         // ReqBody.svcST = '0000';
        //     }
        //     else {
        //         // ReqBody.svcST = this.toolService.padLeft(this.svcST!.value.hour, 2) + this.toolService.padLeft(this.svcST!.value.minute, 2)
        //     }
        // }
        // else {
        //     // ReqBody.svcST = '';
        // }
        // if (!isNaN(this.svcET!.value.hour) && !isNaN(this.svcET!.value.minute)) {
        //     if (this.svcET!.value.hour + this.svcET!.value.minute == 0) {
        //         // ReqBody.svcET = '0000';
        //     }
        //     else {
        //         // ReqBody.svcET = this.toolService.padLeft(this.svcET!.value.hour, 2) + this.toolService.padLeft(this.svcET!.value.minute, 2)
        //     }
        // }
        // else {
        //     // ReqBody.svcET = '';
        // }
        // this.adjHostList(ReqBody.hostList!);
        // 檢查服務時間
        // if (!isNull(ReqBody.svcST)) {
        //     if (((isNaN(parseInt(this.svcST.value.hour)) && !isNaN(parseInt(this.svcST.value.minute))) || (!isNaN(parseInt(this.svcST.value.hour)) && isNaN(parseInt(this.svcST.value.minute))))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        //     if (!isNaN(parseInt(this.svcST.value.hour)) && !isNaN(parseInt(this.svcST.value.minute)) && (isNull(this.svcET.value) || isNaN(parseInt(ReqBody.svcET)))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        // }
        // if (!isNull(ReqBody.svcET)) {
        //     if (((isNaN(parseInt(this.svcET.value.hour)) && !isNaN(parseInt(this.svcET.value.minute))) || (!isNaN(parseInt(this.svcET.value.hour)) && isNaN(parseInt(this.svcET.value.minute))))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        //     if (!isNaN(parseInt(this.svcET.value.hour)) && !isNaN(parseInt(this.svcET.value.minute)) && (isNull(this.svcST.value) || isNaN(parseInt(ReqBody.svcST)))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        // }
        // if ((parseInt(ReqBody.svcST) > parseInt(ReqBody.svcET)) || (parseInt(ReqBody.svcST) == parseInt(ReqBody.svcET))) {
        //     this.showServiceTimeAlert();
        //     return;
        // }
        // console.log('AA0201 ReqBody :', ReqBody)

        this.clientService.addClient(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({ severity: 'success', summary: `${dicts['message.create']} ${dicts['message.client']}`, detail: `${dicts['message.create']} ${dicts['message.success']}!` });
                this.encodeStatus!.setValue('-1');
                this.submitForm();
                this.changePage('query');
            }
        });
    }

    // 更新用戶端
    async updateClientData() {
        const dicts = await this.getDicts();
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            clientName: this.clientDetail!.clientName,
            newClientName: this.newClientName!.value,
            clientAlias: this.clientDetail!.clientAlias,
            newClientAlias: this.newClientAlias!.value,
            signupNum: this.clientDetail!.signupNum,
            newSignupNum: this.newSignupNum!.value,
            hostList: this.clientDetail!.hostList,
            // newHostList: isArray(this.newHostList!.value) ? this.newHostList!.value : [],
            newHostList: this.checkHostListValue(this.newHostList!.value),
            // clientSD: this.clientDetail!.clientSD,
            // newClientSD: isDate(this.newClientSD!.value) ? dayjs(this.newClientSD!.value).format('YYYY-MM-DD') : '',
            // clientED: this.clientDetail!.clientED,
            // newClientED: isDate(this.newClientED!.value) ? dayjs(this.newClientED!.value).format('YYYY-MM-DD') : '',
            // svcST: this.clientDetail!.svcST,
            // svcET: this.clientDetail!.svcET,
            apiQuota: this.clientDetail!.apiQuota,
            newApiQuota: this.newApiQuota!.value ? this.newApiQuota!.value.toString() : '',
            tps: this.clientDetail!.tps,
            newTps: this.newTps!.value ? this.newTps!.value.toString() : '',
            cPriority: this.clientDetail!.cPriority,
            newCPriority: this.newCPriority!.value,
            owner: this.clientDetail!.owner,
            newOwner: this.newOwner!.value,
            encodePublicFlag: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.publicFlag!.value)) + ',' + this.publicFlag!.value,
            newEncodePublicFlag: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.newEncodePublicFlag!.value)) + ',' + this.newEncodePublicFlag!.value,
            emails: this.clientDetail!.emails,
            newEmails: this.newEmails!.value,
            remark: this.clientDetail!.remark,
            newRemark: this.newRemark!.value
        } as AA0204Req;

        // 更新時區
        ReqBody.timeZone = this.timeZone.value;
        ReqBody.newTimeZone = this.newTimeZone.value;

        //更新起訖日
        ReqBody.clientStartDate = this.clientStartDate!.value;
        ReqBody.clientEndDate = this.clientEndDate!.value;

        if( !this.isNullUndefinedNoneCheck(this.newClientStartDate!.value) || !this.isNullUndefinedNoneCheck(this.newClientEndDate!.value) ||
        !isNaN(this.newSvcST!.value.hour) || !isNaN(this.newSvcST!.value.minute) || !isNaN(this.newSvcET!.value.hour) || !isNaN(this.newSvcET!.value.minute))
        {
          if( this.isNullUndefinedNoneCheck(this.newTimeZone.value)){
            this.alertService.ok(dicts['timezone_required'],'');
            return;
          }
        }


        if( !this.isNullUndefinedNoneCheck(this.newClientStartDate!.value) || !this.isNullUndefinedNoneCheck(this.newClientEndDate!.value))
        {

          if( this.isNullUndefinedNoneCheck(this.newClientStartDate!.value)|| this.isNullUndefinedNoneCheck(this.newClientEndDate!.value))
          {
            this.alertService.ok(dicts['date_range_required_if_exist'],'');
            return;
          }

          if(new Date(this.newClientEndDate!.value).getTime()<new Date(this.newClientStartDate!.value).getTime())
          {
            this.alertService.ok(dicts['sdate_over_edate'],'');
            return;
          }


          ReqBody.newClientStartDate = dayjs.utc(dayjs.tz(this.newClientStartDate!.value,this.newTimeZone.value)).format();
          ReqBody.newClientEndDate = dayjs.utc(dayjs.tz(this.newClientEndDate!.value,this.newTimeZone.value)).format();
        }
        else{
          ReqBody.newClientStartDate = '';
          ReqBody.newClientEndDate = '';
        }

        //更新時間
        ReqBody.clientStartTimePerDay = this.clientStartTimePerDay.value;
        ReqBody.clientEndTimePerDay = this.clientEndTimePerDay.value;

        if(!isNaN(this.newSvcST!.value.hour) || !isNaN(this.newSvcST!.value.minute) || !isNaN(this.newSvcET!.value.hour) || !isNaN(this.newSvcET!.value.minute)){
          if(isNaN(this.newSvcST!.value.hour) || isNaN(this.newSvcST!.value.minute) || isNaN(this.newSvcET!.value.hour) || isNaN(this.newSvcET!.value.minute))
          {
           this.alertService.ok(dicts['message.service_time_error'],'');
           return;
          }

          let sTime = new Date();
          sTime.setHours(this.newSvcST!.value.hour);
          sTime.setMinutes(this.newSvcST!.value.minute);
          sTime.setSeconds(0);

          let eTime = new Date();
          eTime.setHours(this.newSvcET!.value.hour);
          eTime.setMinutes(this.newSvcET!.value.minute);
          eTime.setSeconds(0);

          if(new Date(eTime).getTime()<new Date(sTime).getTime())
          {
            this.alertService.ok(dicts['stime_over_etime'],'');
            return;
          }

          ReqBody.newClientStartTimePerDay = dayjs.utc( dayjs.tz(sTime,this.newTimeZone.value) ).format();
          ReqBody.newClientEndTimePerDay = dayjs.utc( dayjs.tz(eTime,this.newTimeZone.value) ).format();

       }
       else{
        ReqBody.newClientStartTimePerDay = '';
        ReqBody.newClientEndTimePerDay = '';
       }

        // if (!isNaN(this.newSvcST!.value.hour) && !isNaN(this.newSvcST!.value.minute)) {
        //     if (this.newSvcST!.value.hour + this.newSvcST!.value.minute == 0) {
        //         ReqBody.newSvcST = '0000';
        //     }
        //     else {
        //         ReqBody.newSvcST = this.toolService.padLeft(this.newSvcST!.value.hour, 2) + this.toolService.padLeft(this.newSvcST!.value.minute, 2)
        //     }
        // }
        // else {
        //     ReqBody.newSvcST = '';
        // }
        // if (!isNaN(this.newSvcET!.value.hour) && !isNaN(this.newSvcET!.value.minute)) {
        //     if (this.newSvcET!.value.hour + this.newSvcET!.value.minute == 0) {
        //         ReqBody.newSvcET = '0000';
        //     }
        //     else {
        //         ReqBody.newSvcET = this.toolService.padLeft(this.newSvcET!.value.hour, 2) + this.toolService.padLeft(this.newSvcET!.value.minute, 2)
        //     }
        // }
        // else {
        //     ReqBody.newSvcET = '';
        // }
        this.adjHostList(ReqBody.newHostList!);
        // 檢查服務時間
        // if (!isNull(ReqBody.newSvcST)) {
        //     if (((isNaN(parseInt(this.newSvcST.value.hour)) && !isNaN(parseInt(this.newSvcST.value.minute))) || (!isNaN(parseInt(this.newSvcST.value.hour)) && isNaN(parseInt(this.newSvcST.value.minute))))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        //     if (!isNaN(parseInt(this.newSvcST.value.hour)) && !isNaN(parseInt(this.newSvcST.value.minute)) && (isNull(this.newSvcET.value) || isNaN(parseInt(ReqBody.newSvcET)))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        // }
        // if (!isNull(ReqBody.newSvcET)) {
        //     if (((isNaN(parseInt(this.newSvcET.value.hour)) && !isNaN(parseInt(this.newSvcET.value.minute))) || (!isNaN(parseInt(this.newSvcET.value.hour)) && isNaN(parseInt(this.newSvcET.value.minute))))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        //     if (!isNaN(parseInt(this.newSvcET.value.hour)) && !isNaN(parseInt(this.newSvcET.value.minute)) && (isNull(this.newSvcST.value) || isNaN(parseInt(ReqBody.newSvcST)))) {
        //         this.showServiceTimeAlert();
        //         return;
        //     }
        // }
        // if ((parseInt(ReqBody.newSvcST) > parseInt(ReqBody.newSvcET)) || (parseInt(ReqBody.newSvcST) == parseInt(ReqBody.newSvcET))) {
        //     this.showServiceTimeAlert();
        //     return;
        // }

        this.clientService.updateClient(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({ severity: 'success', summary: `${dicts['message.update']} ${dicts['message.client']}`, detail: `${dicts['message.update']} ${dicts['message.success']}!` });
                this.encodeStatus!.setValue('-1');
                this.submitForm();
                this.changePage('query');
            }
        });
    }

    // 刪除用戶端
    async deleteClientData() {
        const dicts = await this.getDicts();
        // this.messageService.add({ key: 'deleteClient', sticky: true, severity: 'warn', summary: dicts['cfm_del_client'], detail: `${dicts['client_id']}:${this.clientDetail!.clientID}，${dicts['client_name']}:${this.clientDetail!.clientName}` });

        this.confirmationService.confirm({
          header: dicts['cfm_del_client'],
          message: `${dicts['client_id']}:${this.clientDetail!.clientID}，${dicts['client_name']}:${this.clientDetail!.clientName}`,
          accept: () => {
              this.onDeleteClientConfirm();
          }
        });
    }

    async onDeleteClientConfirm() {
        this.messageService.clear('deleteClient');
        const dicts = await this.getDicts();
        let ReqBody = {
            clientID: this.clientDetail!.clientID
        } as AA0205Req;
        this.clientService.deleteClientByClientId(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({ severity: 'success', summary: `${dicts['message.delete']} ${dicts['message.client']}`, detail: `${dicts['message.delete']} ${dicts['message.success']}!` });
                this.encodeStatus!.setValue('-1');
                this.submitForm();
                this.changePage('query');
            }
        });
    }

    convertEncodeStatusIndex(encodeStatus: string, action: string): number {
        if (action == 'query') {
            switch (encodeStatus) {
                case '1': // 啟用
                    return 0;
                case '0': // 停用
                    return 1;
                case '-1': // 全部
                    return 2;
                case '2': // 鎖定
                    return 3;
            }
        }
        else if (action == 'create') {
            switch (encodeStatus) {
                case '1': // 啟用
                    return 0;
                case '2': // 停用
                    return 1;
                // case '-1': // 全部
                //     return 2;
                // case '2': // 鎖定
                //     return 3;
            }
        }
        else {
            switch (encodeStatus) {
                case '1': // 啟用
                    return 0;
                case '2': // 停用
                    return 1;
                // case '-1': // 全部
                //     return 2;
                case '3': // 鎖定
                    return 3;
            }
        }
        return -1
    }

    adjHostList(hostList: AA0201HostReq[]) {
        if (hostList && hostList.length) {
            for (var i = hostList.length - 1; i >= 0; i--) {
                let host = hostList[i];
                // if (!host.hostIP || !host.hostName)
                //     hostList.splice(i, 1);
                if (Object.keys(hostList[i]).findIndex(key => key === 'no') >= 0) {
                    delete host['no'];
                }
            }
        }
    }

    // async showServiceTimeAlert() {
    //     const dicts = await this.getDicts();
    //     this.alert.ok(`${dicts['service_time']}${dicts['data']}${dicts['error']}`, dicts['message.service_time_error'], AlertType.warning);
    // }

    // 更新用戶安全等級
    async updateClientSecurity() {
        const codes = ['cfm_update_security', 'message.security_update_warning'];
        const dicts = await this.toolService.getDict(codes);
        // this.messageService.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dicts['cfm_update_security'], detail: dicts['message.security_update_warning'] });

        this.confirmationService.confirm({
          header: dicts['cfm_update_security'],
          message: dicts['message.security_update_warning'],
          accept: () => {
              this.onConfirm();
          }
        });
    }

    onConfirm() {
        this.messageService.clear('confirm');
        let req = {
            clientID: this.clientDetail!.clientID,
            clientName: this.clientDetail!.clientName,
            securityID: this.clientDetail!.securityLV.securityLevelId,
            newSecurityID: this.newSecurityID!.value
        } as AA0217Req;
        this.clientService.updateSecurityLVByClient(req).subscribe(async res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.form.reset();
                this.refreshClientDetail();
                this.submitForm();
            }
        });
    }

    // 查詢群組
    queryGroupList() {
        this.groupInfoList = [];
        this.groupInfoListRowcount = this.groupInfoList.length;
        this.selectedGroups = [];
        let ReqBody = {
            keyword: this.keyword!.value,
            securityLevelID: this.clientDetail!.securityLV.securityLevelId,
            clientID: this.clientDetail!.clientID
        } as AA0228Req;
        this.clientService.queryGroupList_v3(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.groupInfoList = res.RespBody.groupInfoList;
                this.groupInfoListRowcount = this.groupInfoList.length;
            }
        });
    }

    moreGroupList() {
        let ReqBody = {
            groupId: this.groupInfoList[this.groupInfoList.length - 1].groupID,
            keyword: this.keyword!.value,
            securityLevelID: this.clientDetail!.securityLV.securityLevelId,
            clientID: this.clientDetail!.clientID
        } as AA0228Req;
        this.clientService.queryGroupList_v3(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.groupInfoList = this.groupInfoList.concat(res.RespBody.groupInfoList);
                this.groupInfoListRowcount = this.groupInfoList.length;
            }
        });
    }

    // 套用群組
    updateClientGroup() {
        let _groupIDList:string[] = [];
        this.selectedGroups.map(item => {
            _groupIDList.push(item.groupID);
        });
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            groupIDList: _groupIDList
        } as AA0216Req;
        this.clientService.addClientGroupByClientId(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.refreshClientDetail();
                // this.changePage('security', this.currentClient);
                this.handleTabSelectd();
            }
        });
    }

    // 刪除群組
    async deleteClientGroup(rowData: AA0228GroupInfo) {
        this.currentDeleteGroup = rowData;
        const codes = ['cfm_del_client_group', 'client_name', 'group_name'];
        const dicts = await this.toolService.getDict(codes);
        // this.messageService.add({ key: 'deleteClientGroup', sticky: true, severity: 'warn', summary: dicts['cfm_del_client_group'], detail: `${dicts['client_name']}:${this.clientDetail!.clientName}，${dicts['group_name']}:${this.currentDeleteGroup.groupName}` });

        this.confirmationService.confirm({
          header: dicts['cfm_del_client_group'],
          message: `${dicts['client_name']}:${this.clientDetail!.clientName}，${dicts['group_name']}:${this.currentDeleteGroup.groupName}`,
          accept: () => {
              this.onDeleteClientGroup();
          }
        });
    }

    onDeleteClientGroup() {
        this.messageService.clear('deleteClientGroup');
        let ReqBody = {
            groupID: this.currentDeleteGroup!.groupID,
            clientID: this.clientDetail!.clientID
        } as AA0227Req;
        // console.log('AA0227 Req:', ReqBody)
        this.clientService.deleteClientGroupByClientId(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.refreshClientDetail();
            }
        });
    }

    // 查詢虛擬群組
    searchVGroupList() {
        this.vgroupInfoList = [];
        this.vgroupInfoListRowcount = this.vgroupInfoList.length;
        this.selectedVGroups = [];
        let ReqBody = {
            keyword: this.keyword!.value,
            securityLevelID: this.clientDetail!.securityLV.securityLevelId,
            clientID: this.clientDetail!.clientID
        } as AA0229Req;
        this.clientService.queryVGroupList(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.vgroupInfoList = res.RespBody.vgroupInfoList;
                this.vgroupInfoListRowcount = this.vgroupInfoList.length;
            }
        });
    }

    moreVGroupList() {
        let ReqBody = {
            vgroupId: this.vgroupInfoList[this.vgroupInfoList.length - 1].vgroupID,
            keyword: this.keyword!.value,
            securityLevelID: this.clientDetail!.securityLV.securityLevelId,
            clientID: this.clientDetail!.clientID
        } as AA0229Req;
        this.clientService.queryVGroupList(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.vgroupInfoList = this.vgroupInfoList.concat(res.RespBody.vgroupInfoList);
                this.vgroupInfoListRowcount = this.vgroupInfoList.length;
            }
        });
    }

    // 套用虛擬群組
    updateClientVGroup() {
        let _vgroupIDList:string[] = [];
        this.selectedVGroups.map(item => {
            _vgroupIDList.push(item.vgroupID);
        });
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            vgroupIDList: _vgroupIDList
        } as AA0226Req;
        this.clientService.addClientVGroupByClientId(ReqBody).subscribe(async res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.refreshClientDetail();
                // this.changePage('security', this.currentClient);
                this.handleTabSelectd();
            }
        });
    }

    async deleteClientVGroup(rowData: AA0229VgroupInfo) {
        this.currentDeleteVGroup = rowData;
        const codes = ['cfm_del_client_vgroup', 'client_name', 'virtul_group_name'];
        const dicts = await this.toolService.getDict(codes);
        // this.messageService.add({ key: 'deleteClientVGroup', sticky: true, severity: 'warn', summary: dicts['cfm_del_client_vgroup'], detail: `${dicts['client_name']}:${this.clientDetail!.clientName}，${dicts['virtul_group_name']}:${this.currentDeleteVGroup.vgroupName}` });

        this.confirmationService.confirm({
          header: dicts['cfm_del_client_vgroup'],
          message: `${dicts['client_name']}:${this.clientDetail!.clientName}，${dicts['virtul_group_name']}:${this.currentDeleteVGroup.vgroupName}`,
          accept: () => {
              this.onDeleteClientVGroup();
          }
        });
    }

    onDeleteClientVGroup() {
        this.messageService.clear('deleteClientVGroup');
        let ReqBody = {
            vgroupID: this.currentDeleteVGroup!.vgroupID,
            clientID: this.clientDetail!.clientID
        } as AA0230Req;
        this.clientService.deleteClientVGroupByClientId(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.refreshClientDetail();
            }
        });
    }

    // 查詢token設定
    getTokenSetting() {
        let ReqBody = {
            clientID: this.clientDetail!.clientID
        } as AA0219Req;
        this.clientService.getTokenSettingByClient(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.authorizedGrantType!.setValue(res.RespBody.authorizedGrantType);
                this.convertTokenExpireTimes('access', res.RespBody.accessTokenValidity);
                this.convertTokenExpireTimes('refresh', res.RespBody.raccessTokenValidity!);
                this.webServerRedirectUri!.setValue(res.RespBody.webServerRedirectUri);
                // this.webServerRedirectUri!.setValidators([ValidatorFns.maxLengthValidator(this.maxlength255.value)])
                this.webServerRedirectUri1!.setValue(res.RespBody.webServerRedirectUri1);
                // this.webServerRedirectUri1!.setValidators([ValidatorFns.maxLengthValidator(this.maxlength255.value)])
                this.webServerRedirectUri2!.setValue(res.RespBody.webServerRedirectUri2);
                // this.webServerRedirectUri2!.setValidators([ValidatorFns.maxLengthValidator(this.maxlength255.value)])
                this.webServerRedirectUri3!.setValue(res.RespBody.webServerRedirectUri3);
                // this.webServerRedirectUri3!.setValidators([ValidatorFns.maxLengthValidator(this.maxlength255.value)])
                this.webServerRedirectUri4!.setValue(res.RespBody.webServerRedirectUri4);
                // this.webServerRedirectUri4!.setValidators([ValidatorFns.maxLengthValidator(this.maxlength255.value)])
                this.webServerRedirectUri5!.setValue(res.RespBody.webServerRedirectUri5);
                // this.webServerRedirectUri5!.setValidators([ValidatorFns.maxLengthValidator(this.maxlength255.value)])
                this.accessTokenQuota!.setValue(res.RespBody.accessTokenQuota);
                this.refreshTokenQuota!.setValue(res.RespBody.refreshTokenQuota);
            }
        });
    }

    // 更新token設定
    updateClientToken() {
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            authorizedGrantType: this.authorizedGrantType!.value,
            accessTokenValidity: this.accessTokenValidity!.value,
            raccessTokenValidity: this.raccessTokenValidity!.value,
            accessTokenValidityTimeUnit: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.accessTokenValidityTimeUnit!.value)) + ',' + this.convertTokenValidityTimeUnitIndex(this.accessTokenValidityTimeUnit!.value),
            raccessTokenValidityTimeUnit: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.raccessTokenValidityTimeUnit!.value)) + ',' + this.convertTokenValidityTimeUnitIndex(this.raccessTokenValidityTimeUnit!.value),
            accessTokenQuota: this.accessTokenQuota!.value,
            refreshTokenQuota: this.refreshTokenQuota!.value,
            webServerRedirectUri: this.webServerRedirectUri!.value,
        } as AA0218Req;
        if(this.webServerRedirectUri1?.value) ReqBody.webServerRedirectUri1 = this.webServerRedirectUri1?.value;
        if(this.webServerRedirectUri2?.value) ReqBody.webServerRedirectUri2 = this.webServerRedirectUri2?.value;
        if(this.webServerRedirectUri3?.value) ReqBody.webServerRedirectUri3 = this.webServerRedirectUri3?.value;
        if(this.webServerRedirectUri4?.value) ReqBody.webServerRedirectUri4 = this.webServerRedirectUri4?.value;
        if(this.webServerRedirectUri5?.value) ReqBody.webServerRedirectUri5 = this.webServerRedirectUri5?.value;

        this.clientService.updateTokenSettingByClient(ReqBody).subscribe(async res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                const code = ['message.update', 'dialog.client_security_setting', 'message.success'];
                const dict = await this.toolService.getDict(code);
                this.messageService.add({ severity: 'success', summary: `${dict['message.update']} ${dict['dialog.client_security_setting']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
                this.getTokenSetting();
            }
        });
    }

    // 更新安全狀態
    updateStatusSetting() {
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            resetFailLoginTreshhold: this.resetFailLoginTreshhold!.value,
            resetPwdFailTimes: this.resetPwdFailTimes!.value.toString(),
            encodeStatus: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.updateStatus!.value)) + ',' + this.convertEncodeStatusIndex(this.updateStatus!.value, 'update')
        } as AA0220Req;
        this.clientService.updateStatusSettingByClient(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.refreshClientDetail();
                this.submitForm();
            }
        });
    }

    // 更新用戶密碼
    updateClientPwd() {
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            resetBlock: this.resetBlock!.value.toString(),
            clientBlock: this.toolService.Base64Encoder(this.clientBlock!.value),
            newClientBlock: this.toolService.Base64Encoder(this.newClientBlock!.value),
            confirmNewClientBlock: this.toolService.Base64Encoder(this.confirmNewClientBlock!.value)
        } as AA0231Req;
        this.clientService.updatePasswordSettingByClient(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.resetBlock!.reset(false);
                this.clientBlock!.reset('');
                this.newClientBlock!.reset('');
                this.confirmNewClientBlock!.reset('');
                this.refreshClientDetail();
            }
        });
    }

    convertTokenExpireTimes(type: string, expTime: number) {
        switch (type) {
            case 'access':
                if (expTime >= 86400) {
                    if (expTime % 86400 == 0) {
                        this.accessTokenValidity!.setValue(expTime / 86400);
                        this.accessTokenValidityTimeUnit!.setValue('d');
                    }
                    else if (expTime % 3600 == 0) {
                        this.accessTokenValidity!.setValue(expTime / 3600);
                        this.accessTokenValidityTimeUnit!.setValue('H');
                    }
                    else if (expTime % 60 == 0) {
                        this.accessTokenValidity!.setValue(expTime / 60);
                        this.accessTokenValidityTimeUnit!.setValue('m');
                    }
                    else {
                        this.accessTokenValidity!.setValue(expTime);
                        this.accessTokenValidityTimeUnit!.setValue('s');
                    }
                }
                else if (86400 > expTime && expTime >= 3600) {
                    if (expTime % 3600 == 0) {
                        this.accessTokenValidity!.setValue(expTime / 3600);
                        this.accessTokenValidityTimeUnit!.setValue('H');
                    }
                    else if (expTime % 60 == 0) {
                        this.accessTokenValidity!.setValue(expTime / 60);
                        this.accessTokenValidityTimeUnit!.setValue('m');
                    }
                    else {
                        this.accessTokenValidity!.setValue(expTime);
                        this.accessTokenValidityTimeUnit!.setValue('s');
                    }
                }
                else if (3600 > expTime && expTime >= 60) {
                    if (expTime % 60 == 0) {
                        this.accessTokenValidity!.setValue(expTime / 60);
                        this.accessTokenValidityTimeUnit!.setValue('m');
                    }
                    else {
                        this.accessTokenValidity!.setValue(expTime);
                        this.accessTokenValidityTimeUnit!.setValue('s');
                    }
                }
                else {
                    this.accessTokenValidity!.setValue(expTime);
                    this.accessTokenValidityTimeUnit!.setValue('s');
                }
                break;
            case 'refresh':
                if (expTime >= 86400) {
                    if (expTime % 86400 == 0) {
                        this.raccessTokenValidity!.setValue(expTime / 86400);
                        this.raccessTokenValidityTimeUnit!.setValue('d');
                    }
                    else if (expTime % 3600 == 0) {
                        this.raccessTokenValidity!.setValue(expTime / 3600);
                        this.raccessTokenValidityTimeUnit!.setValue('H');
                    }
                    else if (expTime % 60 == 0) {
                        this.raccessTokenValidity!.setValue(expTime / 60);
                        this.raccessTokenValidityTimeUnit!.setValue('m');
                    }
                    else {
                        this.raccessTokenValidity!.setValue(expTime);
                        this.raccessTokenValidityTimeUnit!.setValue('s');
                    }
                }
                else if (86400 > expTime && expTime >= 3600) {
                    if (expTime % 3600 == 0) {
                        this.raccessTokenValidity!.setValue(expTime / 3600);
                        this.raccessTokenValidityTimeUnit!.setValue('H');
                    }
                    else if (expTime % 60 == 0) {
                        this.raccessTokenValidity!.setValue(expTime / 60);
                        this.raccessTokenValidityTimeUnit!.setValue('m');
                    }
                    else {
                        this.raccessTokenValidity!.setValue(expTime);
                        this.raccessTokenValidityTimeUnit!.setValue('s');
                    }
                }
                else if (3600 > expTime && expTime >= 60) {
                    if (expTime % 60 == 0) {
                        this.raccessTokenValidity!.setValue(expTime / 60);
                        this.raccessTokenValidityTimeUnit!.setValue('m');
                    }
                    else {
                        this.raccessTokenValidity!.setValue(expTime);
                        this.raccessTokenValidityTimeUnit!.setValue('s');
                    }
                }
                else {
                    this.raccessTokenValidity!.setValue(expTime);
                    this.raccessTokenValidityTimeUnit!.setValue('s');
                }
                break;
        }
    }

    convertTokenValidityTimeUnitIndex(tokenValidityTimeUnit: string): number {
        switch (tokenValidityTimeUnit) {
            case 's': // 秒
                return 0;
            case 'm': // 分鐘
                return 1;
            case 'H': // 小時
                return 2;
            case 'd': // 天
                return 3;
            default:
              return -1;
        }

    }

    onReject() {
        this.messageService.clear();
    }

    refreshClientDetail() {
        this.clientGroupList = [];
        this.clientVGroupList = [];
        this.currentDeleteGroup = {} as AA0228GroupInfo;
        this.currentDeleteVGroup = {} as AA0229VgroupInfo;
        this.securityLevelOptions.map(opt => {
            opt.disabled = false;
        });
        if (this.newSecurityID!.value) {
            this.securityLevelOptions.filter(opt => opt.value == this.newSecurityID!.value)[0].disabled = true;
        }
        let ReqBody = {
            clientID: this.clientDetail!.clientID,
            clientName: this.clientDetail!.clientName
        } as AA0203Req;
        this.clientService.queryClientDetail(ReqBody).subscribe(async res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                const code = ['message.update', 'dialog.client_security_setting', 'message.success'];
                const dict = await this.toolService.getDict(code);
                this.messageService.add({ severity: 'success', summary: `${dict['message.update']} ${dict['dialog.client_security_setting']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
                this.clientDetail = res.RespBody;
                this.clientGroupList = this.clientDetail.groupInfoList;
                this.clientVGroupList = this.clientDetail.vgroupInfoList;
                this.resetFailLoginTreshhold!.setValue(this.clientDetail.failTreshhold);
                this.updateStatus!.setValue(this.clientDetail.status);
                this.resetPwdFailTimes!.setValue(false);
            }
        });
    }

    changeTab(evn) {
      this.changeXAPiKeyStatus();
        this.resetFormValidator(this.form);
        switch (evn.index) {
            case 0: // 安全設定
                this.addSeSecurityLvFormValidator();
                break;
            case 1: // 授權設定
                break;
            case 2: // 虛擬授權設定
                break;
            case 3: // Token設定
                this.getTokenSetting();
                this.addTokenSettingFormValidator();
                break;
            case 4: // x api key setting
                this.queryXApiKeyListByClientId();
                break;
            case 5: // 狀態管理
                this.resetFailLoginTreshhold!.setValue(this.clientDetail!.failTreshhold);
                this.updateStatus!.setValue(this.clientDetail!.status);
                this.resetPwdFailTimes!.setValue(false);
                this.addStatusSettingFormValidator();
                break;
            case 6: // 密碼管理
                this.addPasswordFormValidator();
                // this.form.controls.newClientBlock.setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form, true), ValidatorFns.maxLengthValidator(this.maxlength256.value)]);
                // this.form.controls.confirmNewClientBlock.setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form, true), ValidatorFns.maxLengthValidator(this.maxlength256.value)]);
                this.resetBlock!.setValue(false);
                break;
        }
    }

    async handleTabSelectd() {
        const dict = await this.getDicts();
        switch (this.pageNum) {
            case 7:
                this.securityTab = false;
                this.groupTab = true;
                this.vgroupTab = false;
                this.tokenTab = false;
                this.statusTab = false;
                this.pwdTab = false;
                this.xapikeyTab = false;
                break;
            case 8:
                this.securityTab = false;
                this.groupTab = false;
                this.vgroupTab = true;
                this.tokenTab = false;
                this.statusTab = false;
                this.pwdTab = false;
                this.xapikeyTab = false;
                break;
        }
        this.currentTitle = `${this.title} > ${dict['button.security']}`;
        this.pageNum = 5;
    }

    async changePage(action: string, rowData?: AA0202List) {
        this.currentAction = action;
        const dicts = await this.getDicts();
        this.resetFormValidator(this.form);
        this.clientDetailGroupList = [];
        switch (action) {
            case 'create':
                this.clientService.addClient_before().subscribe(res => {
                    if (this.toolService.checkDpSuccess(res.ResHeader)) {
                        this.addFormValidator(this.form, res.RespBody.constraints);
                        this.currentTitle = `${this.title} > ${dicts['button.create']}`;
                        this.pageNum = 2;
                        this.clientName!.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.stringNameValidator(this.maxlength50.value)]);
                        this.clientAlias!.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(100)]);
                        this.signupNum!.setValidators(ValidatorFns.maxLengthValidator(this.maxlength100.value));
                        this.clientBlock!.setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form), ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.maxlength256.value)]);
                        this.confirmClientBlock!.setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form), ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.maxlength256.value)]);
                        this.owner!.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.maxlength50.value)]);
                        this.createStatus!.setValidators(ValidatorFns.requiredValidator());
                        this.publicFlag!.setValidators(ValidatorFns.requiredValidator());
                        this.emails!.setValidators(ValidatorFns.maxLengthValidator(this.maxlength500.value));
                        this.apiQuota!.setValue(0);
                        this.tps!.setValue(10);
                        this.cPriority!.setValue(5);
                        this.publicFlag!.setValue('2');
                        this.owner?.setValue('Default');
                        this.createStatus?.setValue('1');
                        this.hostList?.setValue([]);
                    }
                });
                break;
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                this.encodeStatus!.setValue('-1');
                break;
            case 'detail':
                this.currentClient = rowData;
                let detailReqBody = {
                    clientID: this.currentClient!.clientId,
                    clientName: this.currentClient!.clientName
                } as AA0203Req;
                this.clientService.queryClientDetail(detailReqBody).subscribe(res => {
                    if (this.toolService.checkDpSuccess(res.ResHeader)) {
                        this.clientDetail = res.RespBody;
                        this.currentTitle = `${this.title} > ${dicts['button.detail']}`;
                        this.pageNum = 6;
                        if (this.clientDetail.groupInfoList && this.clientDetail.groupInfoList.length != 0) {
                            for (let groupInfo of this.clientDetail.groupInfoList) {
                                this.clientDetailGroupList.push(`${groupInfo.groupName} - (${groupInfo.groupAlias ? groupInfo.groupAlias : ''})`);
                            }
                        }
                    }
                });
                break;
            case 'update':
                // this.res_0203 = this.data.data.Res_0203 as AA0203Resp;
                // let regex = /[^\[^\]]+/g;
                // this.res_0203.emails = this.res_0203.emails && this.res_0203.emails.match(regex) ? this.res_0203.emails.match(regex)[0] : '';
                this.timeZoneList =  [];
                this.currentClient = rowData;
                let updateReqBody = {
                    clientID: this.currentClient!.clientId,
                    clientName: this.currentClient!.clientName
                } as AA0203Req;
                this.clientService.queryClientDetail(updateReqBody).subscribe(res => {
                    if (this.toolService.checkDpSuccess(res.ResHeader)) {
                        this.clientService.updateClient_before().subscribe(res => {
                            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                                this.addFormValidator(this.form, res.RespBody.constraints);
                            }
                        });
                        this.clientDetail = res.RespBody;
                        this.currentTitle = `${this.title} > ${dicts['button.update']}`;
                        this.pageNum = 3;
                        this.newClientName!.setValue(this.clientDetail.clientName);
                        this.newClientName!.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.stringNameValidator(this.maxlength50.value)]);
                        this.clientAlias!.setValue(this.clientDetail.clientAlias);
                        this.newClientAlias!.setValue(this.clientDetail.clientAlias);
                        this.newClientAlias!.setValidators(ValidatorFns.maxLengthValidator(100));
                        this.signupNum!.setValue(this.clientDetail.signupNum);
                        this.newSignupNum!.setValue(this.clientDetail.signupNum);
                        this.newSignupNum!.setValidators(ValidatorFns.maxLengthValidator(this.maxlength100.value));
                        this.hostList!.setValue(this.clientDetail.hostList);
                        this.newHostList!.setValue(this.clientDetail.hostList);

                        if(this.clientDetail.timeZone){
                          this.timeZoneList.push({label: this.timezoneOffsetFormat(this.clientDetail.timeZone), value: this.clientDetail.timeZone });
                        }

                        this.timeZone.setValue(this.clientDetail.timeZone);
                        this.newTimeZone.setValue(this.clientDetail.timeZone);




                        if(this.clientDetail.clientStartDate){
                          let localSDate:string = this.getDateString(this.clientDetail.clientStartDate, this.newTimeZone.value);
                          this.clientStartDate!.setValue(this.clientDetail.clientStartDate);
                          this.newClientStartDate!.setValue(localSDate)
                          // console.log(this.clientDetail.clientStartDate)
                          // console.log(localSDate)

                        }

                        if(this.clientDetail.clientEndDate){
                          let localEDate:string = this.getDateString(this.clientDetail.clientEndDate, this.newTimeZone.value);
                          this.clientEndDate!.setValue(this.clientDetail.clientEndDate);
                          this.newClientEndDate!.setValue(localEDate)
                        }



                        if (this.clientDetail.clientStartTimePerDay) {
                          let localsTime = this.getTimeString(this.clientDetail.clientStartTimePerDay, this.newTimeZone.value).split(':')
                          this.clientStartTimePerDay?.setValue(this.clientDetail.clientStartTimePerDay)

                          this.newSvcST!.setValue({ hour: localsTime[0], minute: localsTime[1] });
                        }

                        if(this.clientDetail.clientEndTimePerDay){
                          let localeTime = this.getTimeString(this.clientDetail.clientEndTimePerDay, this.newTimeZone.value).split(':')
                          this.clientEndTimePerDay?.setValue(this.clientDetail.clientEndTimePerDay)

                          this.newSvcET!.setValue({ hour: localeTime[0], minute: localeTime[1] });
                        }



                         // 當現有時區與之前設定的時區不同，增加現有時區資料供調整
                         if(dayjs.tz.guess() !== this.clientDetail.timeZone )
                         {
                           this.timeZoneList.push({label: this.timezoneOffsetFormat(dayjs.tz.guess()), value: dayjs.tz.guess() });
                         }



                        this.apiQuota!.setValue(this.clientDetail.apiQuota);
                        this.newApiQuota!.setValue(parseInt(this.clientDetail.apiQuota));
                        this.tps!.setValue(this.clientDetail.tps);
                        this.newTps!.setValue(parseInt(this.clientDetail.tps));
                        this.cPriority!.setValue(this.clientDetail.cPriority);
                        this.newCPriority!.setValue(parseInt(this.clientDetail.cPriority));
                        this.owner!.setValue(this.clientDetail.owner);
                        this.newOwner!.setValue(this.clientDetail.owner);
                        this.newOwner!.setValidators([ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.maxlength50.value)]);
                        this.publicFlag!.setValue(this.clientDetail.publicFlag);
                        this.newEncodePublicFlag!.setValue(this.clientDetail.publicFlag);
                        this.emails!.setValue(this.clientDetail.emails);
                        this.newEmails!.setValue(this.clientDetail.emails);
                        this.newEmails!.setValidators(ValidatorFns.maxLengthValidator(this.maxlength500.value));
                        this.newRemark!.setValue(this.clientDetail.remark);
                    }
                });
                break;
            case 'delete':
                // this.res_0203 = this.data.data.Res_0203 as AA0203Resp;
                // regex = /[^\[^\]]+/g;
                // this.res_0203.emails = this.res_0203.emails && this.res_0203.emails.match(regex) ? this.res_0203.emails.match(regex)[0] : '';
                this.currentClient = rowData;
                let deleteReqBody = {
                    clientID: this.currentClient!.clientId,
                    clientName: this.currentClient!.clientName
                } as AA0203Req;
                this.clientService.queryClientDetail(deleteReqBody).subscribe(res => {
                    if (this.toolService.checkDpSuccess(res.ResHeader)) {
                        this.clientDetail = res.RespBody;
                        // this.currentTitle = `${this.title} > ${dicts['button.delete']}`;
                        // this.pageNum = 4;
                        this.deleteClientData();
                    }
                });
                break;
            case 'security':
                this.currentClient = rowData;
                let securityReqBody = {
                    clientID: this.currentClient!.clientId,
                    clientName: this.currentClient!.clientName
                } as AA0203Req;
                this.clientService.queryClientDetail(securityReqBody).subscribe(res => {
                    if (this.toolService.checkDpSuccess(res.ResHeader)) {
                        this.clientDetail = res.RespBody;
                        this.securityTab = true;
                        this.groupTab = false;
                        this.vgroupTab = false;
                        this.tokenTab = false;
                        this.statusTab = false;
                        this.pwdTab = false;
                        this.xapikeyTab = false;
                        this.currentTitle = `${this.title} > ${dicts['button.security']}`;
                        this.pageNum = 5;
                        // security Level
                        this.groupAuthService.queryTSecurityLV({}).subscribe(res => {
                            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                                let _securityLevels:{label:string, value:string, disabled:boolean}[] = [];
                                res.RespBody.securityLevelList?.map(scope => {
                                    if (scope.securityLevelId == this.clientDetail?.securityLV.securityLevelId) {
                                        _securityLevels.push({ label: scope.securityLevelName, value: scope.securityLevelId, disabled: true });
                                    }
                                    else {
                                        _securityLevels.push({ label: scope.securityLevelName, value: scope.securityLevelId, disabled: false });
                                    }
                                });
                                this.securityLevelOptions = _securityLevels;
                            }
                        });
                        // group list
                        this.clientGroupList = this.clientDetail.groupInfoList;
                        // vgroup list
                        this.clientVGroupList = this.clientDetail.vgroupInfoList;
                        // 錯誤次數
                        this.resetFailLoginTreshhold!.setValue(this.clientDetail.failTreshhold);
                        // 狀態
                        this.updateStatus!.setValue(this.clientDetail.status);
                        this.addSeSecurityLvFormValidator();
                    }
                });
                break;
            case 'group_list':
                this.currentTitle = `${this.title} > ${dicts['button.security']} > ${dicts['button.manager_oauth_group']}`;
                this.pageNum = 7;
                // this.queryGroupList();
                this.groupInfoList = [];
                this.groupInfoListRowcount = this.groupInfoList.length;
                this.selectedGroups = [];
                break;
            case 'vgroup_list':
                this.currentTitle = `${this.title} > ${dicts['button.security']} > ${dicts['button.manager_oauth_virtual_group']}`;
                this.pageNum = 8;
                // this.searchVGroupList();
                this.vgroupInfoList = [];
                this.vgroupInfoListRowcount = this.vgroupInfoList.length;
                this.selectedVGroups = [];
                break;
            case 'groupInfo_list':
                this.currentTitle = `${this.title} > ${dicts['group_list']}`
                this.pageNum = 9;
                this.queryGroupListData();
                break;
        }
    }

    // 安全等級欄位檢查
    addSeSecurityLvFormValidator() {
        this.clientService.updateSecurityLVByClient_before().subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
            }
        });
    }

    // Token設定欄位檢查
    addTokenSettingFormValidator() {
        this.clientService.updateTokenSettingByClient_before().subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              /** 20230406 因檢核問題故要求先移除該欄位所有檢核 */
              // let filterConstraints = res.RespBody.constraints.filter(item=>{
              //   return item.field != "webServerRedirectUri";
              // })
                this.addFormValidator(this.form, res.RespBody.constraints);
            }
        });
    }

    // 狀態欄位檢查
    addStatusSettingFormValidator() {
        this.clientService.updateStatusSettingByClient_before().subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
            }
        });
    }

    // 密碼欄位檢查
    addPasswordFormValidator() {
        this.clientService.updatePasswordSettingByClient_before().subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.addFormValidator(this.form, res.RespBody.constraints);

              this.form.get('newClientBlock')?.setValidators([ValidatorFns.confirmPasswordValidator(this.form, 'newClientBlock', 'confirmNewClientBlock'), ValidatorFns.maxLengthValidator(this.newClientBlockLimitChar.value)]);
              this.form.get('confirmNewClientBlock')?.setValidators([ValidatorFns.confirmPasswordValidator(this.form, 'newClientBlock', 'confirmNewClientBlock'), ValidatorFns.maxLengthValidator(this.newClientBlockLimitChar.value)]);
            }
        });
    }

    timezoneOffsetFormat(timezone:string)
    {
      const offsetStr = dayjs.tz(new Date,timezone).format().slice(-6);
      return timezone ? `${offsetStr} (${timezone})` : timezone;
    }

    getLocalTimeZone():string{
      return this.toolService.getTimeZone();
    }

    getDateString(date:string,tz?:string):string{
      let parseStr = '';
      if(date &&date!='')
      {
        parseStr = tz ? dayjs(date).tz(tz).format('YYYY-MM-DD') : dayjs.utc(date).local().format('YYYY-MM-DD');
      }
      return parseStr;
    }

    getTimeString(date:string,tz?:string):string{
      let parseStr = '';

      if(date &&date!='')
      {
        parseStr = tz ? dayjs(date).tz(tz).format('HH:mm') : dayjs.utc(date).local().format('HH:mm');
      }

      return parseStr;
    }

    headerReturn(){
      if(this.pageNum == 7 ||this.pageNum == 8 )
      {
        this.handleTabSelectd();
      }
      else
      this.changePage('query');
    }



  queryXApiKeyListByClientId() {
    let req = {
      clientId: this.clientDetail!.clientID
    } as DPB0207Req;
    this.serverService.queryXApiKeyListByClientId(req).subscribe(res => {
      if(this.toolService.checkDpSuccess(res.ResHeader)){
        // console.log(res.RespBody.dataList);
        this.xApiKeyList = res.RespBody.dataList;
      }
      else
        this.xApiKeyList = [];
    })
  }

  formateDate(date:any, formate:string='YYYY/MM/DD HH:mm:ss'){
    return dayjs(parseInt(date)).format(formate) != 'Invalid Date'? dayjs(parseInt(date)).format(formate): '';
  }

  changeXAPiKeyStatus(action: string = 'query') {
    this.resetFormValidator(this.formX)
    switch (action) {
      case 'query':
        this.xApiKeyCreate = false;
        break;
      case 'create':
        this.xApiKeyCreate = true;
        this.serverService.createXApiKey_before().subscribe(res=> {
          if(this.toolService.checkDpSuccess(res.ResHeader)){
            this.addFormValidator(this.formX,res.RespBody.constraints)
            this.clientId.setValue(this.clientDetail?.clientID);
            this.groupIdList.setValue([]);
            this.apiKeyAlias.markAsTouched();
            this.expiredAt.markAsTouched();
            document.getElementById("apiKeyAlias")?.focus();
          }
        })
        break;
      default:
        break;
    }
  }

  async addGroupToList() {
    // console.log(this.clientDetail);
    const code = ['button.manager_oauth_group']
    const dict = await this.toolService.getDict(code);
    const refDialog = this.dialogService.open( ManagerGroupListComponent, {
      header: dict['button.manager_oauth_group'],
      modal: true,
      // data: {
      //   clientID: this.clientDetail?.clientID,
      //   securityLevelID: this.clientDetail?.securityLV.securityLevelId
      // },
      width: '90vw',
      // height: '100vh'
    })


    refDialog.onClose.subscribe(res => {
      if (res) {
        res.forEach(rowData => {
          let tmp = this.groupIdList.value;
          if(tmp.findIndex(item=> item.groupID ===rowData.groupID)==-1){
            tmp.push(rowData);
            this.groupIdList.setValue(tmp);
          }
        });
      }
    })

  }

  removeGroup(selRowData:AA0228GroupInfo){
    let filterData = this.groupIdList.value.filter((rowdata:AA0228GroupInfo)=> rowdata.groupID != selRowData.groupID)
    this.groupIdList.setValue(filterData);
  }


  createXApiKey(){
    let req = {
      clientId: this.clientId.value,
      apiKeyAlias: this.apiKeyAlias.value,
      // effectiveAt: this.effectiveAt.value ?
      expiredAt: new Date(this.expiredAt.value).getTime().toString(),
      groupIdList: this.groupIdList.value.map((rowData:AA0228GroupInfo) => rowData.groupID)
    } as DPB0208Req;
    if(this.effectiveAt.value) req.effectiveAt = new Date(this.effectiveAt.value).getTime().toString();
    this.ngxSrvice.start();
    this.serverService.createXApiKey(req).subscribe( async res=>{
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success']
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.create']} X-Api-Key`,
          detail: `${dict['message.create']} ${dict['message.success']}!` });
          this.changeXAPiKeyStatus();
          this.queryXApiKeyListByClientId();
      }
      this.ngxSrvice.stop();
    })
  }

  onClearClick(ctrlName:string){
    this.formX.get(ctrlName)!.setValue('');
  }

  async deleteXApiKeyById(rowData:DPB0207RespItem){
    const code = ['message.delete', 'message.success', 'cfm_del', 'x_api_key.alias']
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: dict['cfm_del'],
      message: `ID: ${rowData.id}, ${dict['x_api_key.alias']}: ${rowData.apiKeyAlias}`,
      accept: () => {

        this.serverService.deleteXApiKey({ id: rowData.id }).subscribe(async res => {

          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success', summary: `${dict['message.delete']} X-Api-Key`,
              detail: `${dict['message.delete']} ${dict['message.success']}!`
            });
            this.queryXApiKeyListByClientId();
          }
        })

      }
    });
  }

  public get keyword() { return this.form.get('keyword'); };
  public get groupID() { return this.form.get('groupID'); };
  public get groupName() { return this.form.get('groupName'); };
  public get encodeStatus() { return this.form.get('encodeStatus'); };
  public get clientID() { return this.form.get('clientID'); };
  public get clientName() { return this.form.get('clientName'); };
  public get signupNum() { return this.form.get('signupNum'); };
  public get clientAlias() { return this.form.get('clientAlias'); };
  public get clientBlock() { return this.form.get('clientBlock'); };
  public get confirmClientBlock() { return this.form.get('confirmClientBlock'); };
  public get hostList() { return this.form.get('hostList'); };
  public get clientStartDate() { return this.form.get('clientStartDate'); };
  public get clientEndDate() { return this.form.get('clientEndDate'); };
  public get svcST() { return this.form.get('svcST'); };
  public get svcET() { return this.form.get('svcET'); };
  public get apiQuota() { return this.form.get('apiQuota'); };
  public get tps() { return this.form.get('tps'); };
  public get cPriority() { return this.form.get('cPriority'); };
  public get owner() { return this.form.get('owner'); };
  public get createStatus() { return this.form.get('createStatus'); };
  public get publicFlag() { return this.form.get('publicFlag'); };
  public get emails() { return this.form.get('emails'); };
  public get newClientName() { return this.form.get('newClientName'); };
  public get newClientAlias() { return this.form.get('newClientAlias'); };
  public get newSignupNum() { return this.form.get('newSignupNum'); };
  public get newHostList() { return this.form.get('newHostList'); };
  public get newClientStartDate() { return this.form.get('newClientStartDate'); };
  public get newClientEndDate() { return this.form.get('newClientEndDate'); };
  public get newSvcST() { return this.form.get('newSvcST'); };
  public get newSvcET() { return this.form.get('newSvcET'); };
  public get newApiQuota() { return this.form.get('newApiQuota'); };
  public get newTps() { return this.form.get('newTps'); };
  public get newCPriority() { return this.form.get('newCPriority'); };
  public get newOwner() { return this.form.get('newOwner'); };
  public get newEncodePublicFlag() { return this.form.get('newEncodePublicFlag'); };
  public get newEmails() { return this.form.get('newEmails'); };
  public get newSecurityID() { return this.form.get('newSecurityID'); };
  public get authorizedGrantType() { return this.form.get('authorizedGrantType'); };
  public get accessTokenValidity() { return this.form.get('accessTokenValidity'); };
  public get raccessTokenValidity() { return this.form.get('raccessTokenValidity'); };
  public get webServerRedirectUri() { return this.form.get('webServerRedirectUri'); };
  public get webServerRedirectUri1() { return this.form.get('webServerRedirectUri1'); };
  public get webServerRedirectUri2() { return this.form.get('webServerRedirectUri2'); };
  public get webServerRedirectUri3() { return this.form.get('webServerRedirectUri3'); };
  public get webServerRedirectUri4() { return this.form.get('webServerRedirectUri4'); };
  public get webServerRedirectUri5() { return this.form.get('webServerRedirectUri5'); };
  public get accessTokenValidityTimeUnit() { return this.form.get('accessTokenValidityTimeUnit'); };
  public get raccessTokenValidityTimeUnit() { return this.form.get('raccessTokenValidityTimeUnit'); };
  public get updateStatus() { return this.form.get('updateStatus'); };
  public get resetFailLoginTreshhold() { return this.form.get('resetFailLoginTreshhold'); };
  public get resetPwdFailTimes() { return this.form.get('resetPwdFailTimes'); };
  public get newClientBlock() { return this.form.get('newClientBlock'); };
  public get resetBlock() { return this.form.get('resetBlock'); };
  public get confirmNewClientBlock() { return this.form.get('confirmNewClientBlock'); };
  public get accessTokenQuota() { return this.form.get('accessTokenQuota'); };
  public get refreshTokenQuota() { return this.form.get('refreshTokenQuota'); };
  public get remark() { return this.form.get('remark'); };
  public get newRemark() { return this.form.get('newRemark'); };
  //202209029新增
  public get clientStartTimePerDay() { return this.form.get('clientStartTimePerDay')!; };
  public get newClientStartTimePerDay() { return this.form.get('newClientStartTimePerDay')!; };
  public get clientEndTimePerDay() { return this.form.get('clientEndTimePerDay')!; };
  public get newClientEndTimePerDay() { return this.form.get('newClientEndTimePerDay')!; };
  public get timeZone() { return this.form.get('timeZone')!; };
  public get newTimeZone() { return this.form.get('newTimeZone')!; };


  public get clientId() { return this.formX.get('clientId')!; };
  public get apiKeyAlias() { return this.formX.get('apiKeyAlias')!; };
  public get effectiveAt() { return this.formX.get('effectiveAt')!; };
  public get expiredAt() { return this.formX.get('expiredAt')!; };
  public get groupIdList() { return this.formX.get('groupIdList')!; };
}
