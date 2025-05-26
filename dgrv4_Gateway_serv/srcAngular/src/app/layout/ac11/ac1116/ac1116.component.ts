import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AA1101Req } from 'src/app/models/api/SecurityService/aa1101.interface';
import { AA1103Req } from 'src/app/models/api/SecurityService/aa1103.interface';
import { AA1104Req } from 'src/app/models/api/SecurityService/aa1104.interface';
import { AA1105Req, AA1105Resp } from 'src/app/models/api/SecurityService/aa1105.interface';
import { AA1116Item, AA1116Req } from 'src/app/models/api/SecurityService/aa1116.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { SecurityService } from 'src/app/shared/services/api-security.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { BaseComponent } from '../../base-component';

@Component({
    selector: 'app-ac1116',
    templateUrl: './ac1116.component.html',
    styleUrls: ['./ac1116.component.css']
})
export class Ac1116Component extends BaseComponent implements OnInit {

    currentTitle: string = this.title;
    pageNum: number = 1; // 1: 查詢、2: 建立、3: 更新、4: 刪除
    queryForm!: FormGroup;
    canCreate: boolean = false;
    canUpdate: boolean = false;
    canDelete: boolean = false;
    cols: { field: string; header: string; }[] = [];
    dataList: Array<AA1116Item> = [];
    rowcount: number = 0;
    createForm!: FormGroup;
    updateForm!: FormGroup;
    securityLevelDetail?: AA1105Resp;
    deleteForm!: FormGroup;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private roleService: RoleService,
        private toolSerive: ToolService,
        private securityService: SecurityService,
        private messageService: MessageService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.queryForm = this.fb.group({
            keyword: new FormControl('')
        });
        this.createForm = this.fb.group({
            securityLevelId: new FormControl(''),
            securityLevelName: new FormControl(''),
            securityLevelDesc: new FormControl('')
        });
        this.updateForm = this.fb.group({
            securityLevelId: new FormControl(''),
            newSecurityLevelName: new FormControl(''),
            newSecurityLevelDesc: new FormControl('')
        });
        this.deleteForm = this.fb.group({
            securityLevelId: new FormControl(''),
            securityLevelName: new FormControl(''),
            securityLevelDesc: new FormControl('')
        });
        // 功能權限
        this.roleService.queryRTMapByUk({ txIdList: ['AA1101', 'AA1103', 'AA1104'] }).subscribe(res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                this.canCreate = res.RespBody.dataList.find(item => item.txId === 'AA1101')?.available??false;
                this.canUpdate = res.RespBody.dataList.find(item => item.txId === 'AA1103')?.available??false;
                this.canDelete = res.RespBody.dataList.find(item => item.txId === 'AA1104')?.available??false;
            }
        });
        this.init();
    }

    async init() {
        const code = ['security_level_id', 'security_level_name', 'security_level_desc'];
        const dict = await this.toolSerive.getDict(code);
        this.cols = [
            { field: 'securityLevelId', header: dict['security_level_id'] },
            { field: 'securityLevelName', header: dict['security_level_name'] },
            { field: 'securityLevelDesc', header: dict['security_level_desc'] }
        ];
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.q_keyword!.value
        } as AA1116Req;
        this.securityService.querySecurityLevelList_ignore1298(ReqBody).subscribe(res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    querySecurityList() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.q_keyword!.value
        } as AA1116Req;
        this.securityService.querySecurityLevelList(ReqBody).subscribe(res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    moreSecurityList() {
        let ReqBody = {
            securityLevelId: this.dataList[this.dataList.length - 1].securityLevelId,
            keyword: this.q_keyword!.value
        } as AA1116Req;
        this.securityService.querySecurityLevelList(ReqBody).subscribe(res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    createSecurityLevel() {
        let ReqBody = {
            securityLevelId: this.c_securityLevelId!.value,
            securityLevelName: this.c_securityLevelName!.value,
            securityLevelDesc: this.c_securityLevelDesc!.value
        } as AA1101Req;
        this.securityService.addSecurityLevel(ReqBody).subscribe(async res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                const code = ['message.create', 'security_level', 'message.success'];
                const dict = await this.toolSerive.getDict(code);
                this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['security_level']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
                this.querySecurityList();
                this.changePage('query');
            }
        });
    }

    updateSecurityLevel() {
        let ReqBody = {
            securityLevelId: this.securityLevelDetail?.securityLevelId,
            oriSecurityLevelName: this.securityLevelDetail?.securityLevelName,
            newSecurityLevelName: this.u_newSecurityLevelName!.value,
            newSecurityLevelDesc: this.u_newSecurityLevelDesc!.value
        } as AA1103Req;
        this.securityService.updateSecurityLevel(ReqBody).subscribe(async res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                const code = ['message.update', 'security_level', 'message.success'];
                const dict = await this.toolSerive.getDict(code);
                this.messageService.add({ severity: 'success', summary: `${dict['message.update']} ${dict['security_level']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
                this.querySecurityList();
                this.changePage('query');
            }
        });
    }

    deleteSecurityLevel() {
        let ReqBody = {
            securityLevelId: this.securityLevelDetail?.securityLevelId,
            securityLevelName: this.securityLevelDetail?.securityLevelName
        } as AA1104Req;
        this.securityService.deleteSecurityLevel(ReqBody).subscribe(async res => {
            if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                const code = ['message.delete', 'security_level', 'message.success'];
                const dict = await this.toolSerive.getDict(code);
                this.messageService.add({ severity: 'success', summary: `${dict['message.delete']} ${dict['security_level']}`, detail: `${dict['message.delete']} ${dict['message.success']}!` });
                this.querySecurityList();
                this.changePage('query');
            }
        });
    }

    async changePage(action: string, rowData?: AA1116Item) {
        const code = ['button.create', 'button.update', 'button.delete'];
        const dict = await this.toolSerive.getDict(code);
        switch (action) {
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                break;
            case 'create':
                this.currentTitle = `${this.title} > ${dict['button.create']}`;
                this.pageNum = 2;
                this.resetFormValidator(this.createForm);
                this.securityService.addSecurityLevel_before().subscribe(res => {
                    if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                        this.addFormValidator(this.createForm, res.RespBody.constraints);
                    }
                });
                break;
            case 'update':
            case 'delete':
                this.securityLevelDetail = {} as AA1105Resp;
                let ReqBody = {
                    securityLevelId: rowData?.securityLevelId,
                    securityLevelName: rowData?.securityLevelName
                } as AA1105Req;
                this.securityService.querySecurityLevelDetail(ReqBody).subscribe(res => {
                    if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                        this.securityLevelDetail = res.RespBody;
                        if (action == 'update') {
                            this.currentTitle = `${this.title} > ${dict['button.update']}`;
                            this.pageNum = 3;
                            this.resetFormValidator(this.updateForm);
                            this.u_securityLevelId!.setValue(this.securityLevelDetail.securityLevelId);
                            this.u_newSecurityLevelName!.setValue(this.securityLevelDetail.securityLevelName);
                            this.u_newSecurityLevelDesc!.setValue(this.securityLevelDetail.securityLevelDesc);
                            this.securityService.updateSecurityLevel_before().subscribe(res => {
                                if (this.toolSerive.checkDpSuccess(res.ResHeader)) {
                                    this.addFormValidator(this.updateForm, res.RespBody.constraints);
                                }
                            });
                        }
                        else {
                            this.currentTitle = `${this.title} > ${dict['button.delete']}`;
                            this.pageNum = 4;
                            this.resetFormValidator(this.deleteForm!);
                            this.d_securityLevelId!.setValue(this.securityLevelDetail.securityLevelId);
                            this.d_securityLevelName!.setValue(this.securityLevelDetail.securityLevelName);
                            this.d_securityLevelDesc!.setValue(this.securityLevelDetail.securityLevelDesc);
                        }
                    }
                });
                break;
        }
    }

    headerReturn(){
      this.changePage('query')
    }

    public get q_keyword() { return this.queryForm.get('keyword'); }
    public get c_securityLevelId() { return this.createForm.get('securityLevelId'); }
    public get c_securityLevelName() { return this.createForm.get('securityLevelName'); }
    public get c_securityLevelDesc() { return this.createForm.get('securityLevelDesc'); }
    public get u_securityLevelId() { return this.updateForm.get('securityLevelId'); }
    public get u_newSecurityLevelName() { return this.updateForm.get('newSecurityLevelName'); }
    public get u_newSecurityLevelDesc() { return this.updateForm.get('newSecurityLevelDesc'); }
    public get d_securityLevelId() { return this.deleteForm.get('securityLevelId'); }
    public get d_securityLevelName() { return this.deleteForm.get('securityLevelName'); }
    public get d_securityLevelDesc() { return this.deleteForm.get('securityLevelDesc'); }

}
