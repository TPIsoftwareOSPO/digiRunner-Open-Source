import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { GroupAuthService } from 'src/app/shared/services/api-group-auth.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AA1109Req, AA1109Resp } from 'src/app/models/api/GroupAuthService/aa1109.interface';
import { AA1110Req } from 'src/app/models/api/GroupAuthService/aa1110.interface';
import { AA1115GroupAuthorities, AA1115Req } from 'src/app/models/api/GroupAuthService/aa1115.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import { AA1106Req } from 'src/app/models/api/GroupAuthService/aa1106.interface';
import { AA1108Req } from 'src/app/models/api/GroupAuthService/aa1108.interface';

@Component({
    selector: 'app-ac1107',
    templateUrl: './ac1107.component.html',
    styleUrls: ['./ac1107.component.css'],
    providers: [MessageService, ConfirmationService]
})
export class Ac1107Component extends BaseComponent implements OnInit {

    form!: FormGroup;
    cols: { field: string; header: string; }[] = [];
    groupAuthoritiesList: Array<AA1115GroupAuthorities> = [];
    groupAuthoritiesListRowCount: number = 0;
    canCreate: boolean = false;
    canDetail: boolean = false;
    canUpdate: boolean = false;
    canDelete: boolean = false;
    pageNum: number = 1; // 1: 查詢、2: 建立、3: 更新、4: 明細資料
    currentTitle: string = this.title;
    groupAuthoritiesDetail?: AA1109Resp;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private translate: TranslateService,
        private groupAuthService: GroupAuthService,
        private tool: ToolService,
        private message: MessageService,
        private roleService: RoleService,
        private confirmationService:ConfirmationService

    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.form = this.fb.group({
            keyword: new FormControl(''),
            groupAuthoritieId: new FormControl(''),
            groupAuthoritieName: new FormControl(''),
            groupAuthoritieDesc: new FormControl(''),
            groupAuthoritieLevel: new FormControl(''),
            oriGroupAuthoritieName: new FormControl(''),
            newGroupAuthoritieName: new FormControl(''),
            newGroupAuthoritieDesc: new FormControl(''),
            newGroupAuthoritieLevel: new FormControl('')
        });
        this.roleService.queryRTMapByUk({ txIdList: ['AA1106', 'AA1108', 'AA1109', 'AA1110'] } as DPB0115Req).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.canCreate = res.RespBody.dataList.find(item => item.txId === 'AA1106')?.available??false;
                this.canUpdate = res.RespBody.dataList.find(item => item.txId === 'AA1108')?.available??false;
                this.canDetail = res.RespBody.dataList.find(item => item.txId === 'AA1109')?.available??false;
                this.canDelete = res.RespBody.dataList.find(item => item.txId === 'AA1110')?.available??false;
            }
        });
        const codes = ['group_auth_id', 'group_auth_name', 'group_auth_desc', 'group_auth_level', 'button.create', 'button.update', 'button.delete', 'active', 'inactive'];
        this.translate.get(codes).subscribe(i18n => this.init(i18n));
    }

    init(i18n) {
        this.cols = [
            { field: 'groupAuthoritiesId', header: i18n['group_auth_id'] },
            { field: 'groupAuthoritiesName', header: i18n['group_auth_name'] },
            { field: 'groupAuthoritiesDesc', header: i18n['group_auth_desc'] },
            { field: 'groupAuthoritiesLevel', header: i18n['group_auth_level'] }
        ];
        this.groupAuthoritiesList = [];
        this.groupAuthoritiesListRowCount = this.groupAuthoritiesList.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as AA1115Req;
        this.groupAuthService.queryScopeAuthorities_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.groupAuthoritiesList = res.RespBody.groupAuthoritiesList;
                this.groupAuthoritiesListRowCount = this.groupAuthoritiesList.length;
            }
        });
    }

    queryScopeAuthoritiesList() {
        this.groupAuthoritiesList = [];
        this.groupAuthoritiesListRowCount = this.groupAuthoritiesList.length;
        let ReqBody = {
            keyword: this.keyword!.value
        } as AA1115Req;
        this.groupAuthService.queryScopeAuthorities(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.groupAuthoritiesList = res.RespBody.groupAuthoritiesList;
                this.groupAuthoritiesListRowCount = this.groupAuthoritiesList.length;
            }
        });
    }

    moreScopeAuthoritiesList() {
        let ReqBody = {
            lastGroupAuthoritieId: this.groupAuthoritiesList[this.groupAuthoritiesList.length - 1].groupAuthoritiesId,
            keyword: this.keyword!.value
        } as AA1115Req;
        this.groupAuthService.queryScopeAuthorities(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.groupAuthoritiesList = this.groupAuthoritiesList.concat(res.RespBody.groupAuthoritiesList);
                this.groupAuthoritiesListRowCount = this.groupAuthoritiesList.length;
            }
        });
    }

    create() {
        let ReqBody = {
            groupAuthoritieId: this.groupAuthoritieId!.value,
            groupAuthoritieName: this.groupAuthoritieName!.value,
            groupAuthoritieDesc: this.groupAuthoritieDesc!.value,
            groupAuthoritieLevel: this.groupAuthoritieLevel!.value
        } as AA1106Req;
        this.groupAuthService.addTGroupAuthority(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.create', 'message.authority_type', 'message.success'];
                const dict = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.authority_type']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
                this.queryScopeAuthoritiesList();
                this.changePage('query');
            }
        });
    }

    update() {
        let ReqBody = {
            groupAuthoritieId: this.groupAuthoritiesDetail?.groupAuthoritieId,
            oriGroupAuthoritieName: this.groupAuthoritiesDetail?.groupAuthoritieName,
            newGroupAuthoritieName: this.newGroupAuthoritieName!.value,
            newGroupAuthoritieDesc: this.newGroupAuthoritieDesc!.value,
            newGroupAuthoritieLevel: this.newGroupAuthoritieLevel!.value
        } as AA1108Req;
        this.groupAuthService.updateTGroupAuthority(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.update', 'message.authority_type', 'message.success'];
                const dict = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dict['message.update']} ${dict['message.authority_type']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
                this.queryScopeAuthoritiesList();
                this.changePage('query');
            }
        });
    }

    deleteProc(rowData: AA1115GroupAuthorities) {
        this.message.clear();
        let ReqBody = {
            groupAuthoritieId: rowData.groupAuthoritiesId
        } as AA1109Req;
        this.groupAuthService.queryTGroupAuthorityDetail(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.groupAuthoritiesDetail = res.RespBody;
                const codes = ['cfm_del_role_role_mapping', 'cfm_del_auth_type'];
                const dict = await this.tool.getDict(codes);
                // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_auth_type'] });
                this.confirmationService.confirm({
                  header: ' ',
                  message: dict['cfm_del_auth_type'],
                  accept: () => {
                      this.onConfirm();
                  }
                });
            }
        });
    }

    onConfirm() {
        this.message.clear();
        let req = {
            groupAuthoritieId: this.groupAuthoritiesDetail?.groupAuthoritieId,
            groupAuthoritieName: this.groupAuthoritiesDetail?.groupAuthoritieName
        } as AA1110Req;
        this.groupAuthService.deleteTGroupAuthority(req).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.delete', 'message.authority_type', 'message.success'];
                const dict = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dict['message.delete']} ${dict['message.authority_type']}`, detail: `${dict['message.delete']} ${dict['message.success']}!` });
                this.queryScopeAuthoritiesList();
            }
        });
    }

    onReject() {
        this.message.clear();
    }

    async changePage(action: string, rowData?: AA1115GroupAuthorities) {
        const code = ['button.create', 'button.update', 'button.detail'];
        const dict = await this.tool.getDict(code);
        this.resetFormValidator(this.form);
        switch (action) {
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                break;
            case 'create':
                this.groupAuthService.addTGroupAuthority_before().subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.addFormValidator(this.form, res.RespBody.constraints);
                        this.currentTitle = `${this.title} > ${dict['button.create']}`;
                        this.pageNum = 2;
                        this.groupAuthoritieId!.enable();
                    }
                });
                break;
            case 'update':
                let updateReqBody = {
                    groupAuthoritieId: rowData?.groupAuthoritiesId
                } as AA1109Req;
                this.groupAuthService.queryTGroupAuthorityDetail(updateReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.groupAuthoritiesDetail = res.RespBody;
                        this.groupAuthService.updateTGroupAuthority_before().subscribe(resp => {
                            if (this.tool.checkDpSuccess(resp.ResHeader)) {
                                this.addFormValidator(this.form, resp.RespBody.constraints);
                                this.currentTitle = `${this.title} > ${dict['button.update']}`;
                                this.pageNum = 3;
                                this.groupAuthoritieId!.disable();
                                this.groupAuthoritieId!.setValue(this.groupAuthoritiesDetail?.groupAuthoritieId);
                                this.oriGroupAuthoritieName!.setValue(this.groupAuthoritiesDetail?.groupAuthoritieName);
                                this.newGroupAuthoritieName!.setValue(this.groupAuthoritiesDetail?.groupAuthoritieName);
                                this.newGroupAuthoritieDesc!.setValue(this.groupAuthoritiesDetail?.groupAuthoritieDesc);
                                this.newGroupAuthoritieLevel!.setValue(this.groupAuthoritiesDetail?.groupAuthoritieLevel);
                            }
                        });
                    }
                });
                break;
            case 'detail':
                let detailReqBody = {
                    groupAuthoritieId: rowData?.groupAuthoritiesId
                } as AA1109Req;
                this.groupAuthService.queryTGroupAuthorityDetail(detailReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
                        this.pageNum = 4;
                        this.groupAuthoritiesDetail = res.RespBody;
                    }
                });
                break;
        }
    }

    headerReturn() {
      this.changePage('query');
    }

    public get keyword() { return this.form.get('keyword'); };
    public get groupAuthoritieId() { return this.form.get('groupAuthoritieId'); };
    public get groupAuthoritieName() { return this.form.get('groupAuthoritieName'); };
    public get groupAuthoritieDesc() { return this.form.get('groupAuthoritieDesc'); };
    public get groupAuthoritieLevel() { return this.form.get('groupAuthoritieLevel'); };
    public get oriGroupAuthoritieName() { return this.form.get('oriGroupAuthoritieName'); };
    public get newGroupAuthoritieName() { return this.form.get('newGroupAuthoritieName'); };
    public get newGroupAuthoritieDesc() { return this.form.get('newGroupAuthoritieDesc'); };
    public get newGroupAuthoritieLevel() { return this.form.get('newGroupAuthoritieLevel'); };

}
