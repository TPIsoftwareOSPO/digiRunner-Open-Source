import { TranslateService } from '@ngx-translate/core';
import { SidebarService } from 'src/app/layout/components/sidebar/sidebar.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, FormGroupDirective } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormOperate } from 'src/app/models/common.enum';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { BaseComponent } from '../../base-component';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AA0020List, AA0020Req } from 'src/app/models/api/RoleService/aa0020.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { AA0014Req } from 'src/app/models/api/UserService/aa0014.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { Menu } from 'src/app/models/menu.model';
import { AA0011Req } from 'src/app/models/api/UserService/aa0011.interface';
import { ListGroupsComponent } from 'src/app/shared/list-group/list-groups.component';
import { AA0013Req } from 'src/app/models/api/UserService/aa0013.interface';

@Component({
    selector: 'app-ac0012',
    templateUrl: './ac0012.component.html',
    styleUrls: ['./ac0012.component.css'],
    providers: [MessageService, ConfirmationService]
})
export class Ac0012Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;
    @ViewChild('listgroup') listGroup!: ListGroupsComponent;

    form: FormGroup;
    formOperate = FormOperate;
    cols: ({ field: string; header: string; display?: undefined; } | { field: string; header: string; display: string; })[] = [];
    rowcount: number = 0;
    data: Array<AA0020List> = [];
    roleDetailList: Array<AA0020List> = new Array<AA0020List>();
    currentRole?: AA0014Req;
    /**
     * 控制dialog是否顯示的布林值
     */
    display: boolean;
    dialogTitle: string = '';
    pageNum: number = 1;
    form_page2: FormGroup;
    selected: any = [];
    menus?: Menu[];
    roleNameLimitChar = { value: 30 };
    roleAliasLimitChar = { value: 255 };

    selectedCities: string[] = [];
    selecteds: { name: string; code: string; }[] = [];
    funcs: { name: string; code: string; }[] = [];
    menus_page3: Menu[]= [];
    data_page3?: AA0020List;
    roleName_page3: string = '';
    roleAlias_page3: string = '';

    form_page4: FormGroup;
    data_page4?: AA0020List;
    selectedFuncList: any = [];
    newRoleAliasLimitChar = { value: 255 };
    menus_page4: Menu[] = [];
    roleName_page4: string = '';

    isDefault:boolean = true;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private messageService: MessageService,
        private tool: ToolService,
        private siderbar: SidebarService,
        private translate: TranslateService,
        private roleService: RoleService,
        private confirmationService: ConfirmationService,
    ) {
        super(route, tr);
        this.display = false;
        this.form = this.fb.group({
            keyword: new FormControl('')
        });
        this.form_page2 = this.fb.group({
            roleName: '',
            roleAlias: '',
            funcCodeList: ''
        });
        this.form_page4 = this.fb.group({
            newRoleAlias: '',

            // roleAlias: '',
            // funcCodeList: ''
        });
        const codes = ['role_name', 'role_desc'];
        this.translate.get(codes).subscribe(dict => {
            this.cols = [
                { field: 'roleName', header: dict['role_name'] },
                { field: 'roleAlias', header: dict['role_desc'] },
            ];
        })
    }

    ngOnInit() {
        this.roleDetailList = [];
        this.rowcount = this.roleDetailList.length;
        //預埋api
        let ReqBody = {
            keyword: this.form.get('keyword')!.value,
            funcFlag: true,
            authorityFlag: false
        } as AA0020Req;
        this.roleService.queryTRoleList_v3_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.roleDetailList = res.RespBody.roleDetailList;
                this.rowcount = this.roleDetailList.length;
            }
        });
    }

    async create() {
        const codes = ['dialog.create', 'message.role', 'message.create', 'message.success'];
        const dict = await this.tool.getDict(codes);
        this.form_page2 = this.fb.group({
            roleName: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringNameValidator(this.roleNameLimitChar.value)]),
            roleAlias: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.roleAliasLimitChar.value)]),
            funcCodeList: new FormControl([], [ValidatorFns.requiredValidator()])
        });
        // this.form.statusChanges.subscribe(r => console.log(this.form.value))
        this.selected = [];
        //預埋api
        let tFuncsDetails = this.tool.getFuncList();
        let menus = this.siderbar.transform(tFuncsDetails);
        this.menus = menus;
        this.title = `${this.title} > ${dict['dialog.create']}`;
        this.pageNum = 2;

        // this.update('')
        // this.selected = Object.keys('').filter(menu => menu.length > 4 && menus[menu] == true).map(m => m);
        // console.log(this.listGroup);
    }

    submitForm() {
        this.roleDetailList = [];
        this.rowcount = this.roleDetailList.length;
        //預埋api
        let ReqBody = {
            keyword: this.form.get('keyword')!.value,
            funcFlag: true,
            authorityFlag: false
        } as AA0020Req;
        this.roleService.queryTRoleList_v3(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.roleDetailList = res.RespBody.roleDetailList;
                this.rowcount = this.roleDetailList.length;
            }
        });
    }

    moreData() {
        let ReqBody = {
            roleId: this.roleDetailList[this.roleDetailList.length - 1].roleID,
            keyword: this.form.get('keyword')!.value,
            funcFlag: true,
            authorityFlag: false
        } as AA0020Req;
        this.roleService.queryTRoleList_v3(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.roleDetailList = this.roleDetailList.concat(res.RespBody.roleDetailList);
                this.rowcount = this.roleDetailList.length;
            }
        });
    }

    showDialog(rowData: AA0020List, operation: FormOperate) {
        const codes = ['fun_list', 'dialog.edit', 'role_detail', 'message.update', 'cfm_del', 'roles_name', 'role_desc', 'message.success']
        this.translate.get(codes).pipe(
            switchMap(dict => this.openDialog$(rowData, operation, dict))
        ).subscribe();
        return false;
    }

    openDialog$(rowData: AA0020List, operation: FormOperate, dict: any): Observable<boolean> {
        return Observable.create(obser => {
            switch (operation) {
                case FormOperate.detail:
                    const codes = ['role_detail']
                    this.translate.get(codes).subscribe(dicts => {
                        this.data_page3 = rowData;
                        this.roleName_page3 = rowData.roleName
                        this.roleAlias_page3 = rowData.roleAlias
                        let tFuncsDetails = this.tool.getFuncList();
                        let menus_page3 = this.siderbar.transform(tFuncsDetails);
                        this.menus_page3 = menus_page3;
                        this.title = `${this.title} > ${dicts['role_detail']}`;
                        this.pageNum = 3
                    })
                    break;
                case FormOperate.update:
                    const codes_update = ['message.update']
                    this.data_page4 = rowData
                    this.roleName_page4 = rowData.roleName
                    let tFuncsDetails_page4 = this.tool.getFuncList();
                    let menus = this.siderbar.transform(tFuncsDetails_page4);
                    this.menus_page4 = menus;
                    let _funcCheckbox = {};
                    this.data_page4.funcCodeList.map(func => {
                        _funcCheckbox[func] = true;
                    });


                    this.form_page4 = this.fb.group({
                        newRoleAlias: new FormControl(this.data_page4.roleAlias, [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.newRoleAliasLimitChar.value)]),
                        funcCodeList: new FormControl([],ValidatorFns.requiredValidator())
                    });
                    this.update_page4(_funcCheckbox);
                    this.translate.get(codes_update).subscribe(dicts => {
                        this.title = `${this.title} > ${dicts['message.update']}`;
                        this.pageNum = 4
                    })
                    break;
                case FormOperate.delete: {
                    this.currentRole = {
                        roleId: rowData.roleID,
                        roleName: rowData.roleName
                    };
                    // this.messageService.clear();
                    // this.messageService.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_role'], detail: `${dict['roles_name']} : ${this.currentRole.roleName} , ${dict['role_desc']} : ${rowData.roleAlias}` });
                    // obser.next(true);


                    this.confirmationService.confirm({
                      header: dict['cfm_del'],
                      message: `${dict['roles_name']} : ${this.currentRole.roleName} , ${dict['role_desc']} : ${rowData.roleAlias}`,
                      accept: () => {
                          this.onDeleteConfirm();
                      }
                    });


                }
            }
        });
    }

    onDeleteConfirm(): void {
        // this.messageService.clear();
        const codes = ['message.delete', 'message.role', 'message.success']
        this.translate.get(codes).subscribe(dicts => {
            this.roleService.deleteTRole(this.currentRole!).subscribe(res => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                    this.messageService.clear();
                    this.messageService.add({ severity: 'success', summary: `${dicts['message.delete']} ${dicts['message.role']}`, detail: `${dicts['message.delete']} ${dicts['message.success']}!` });
                    this.submitForm();
                }
            });
        });
    }

    onReject(): void {
        this.messageService.clear();
    }

    submitForm_page2(formDirective: FormGroupDirective) {
        const codes = ['message.create', 'message.role', 'message.success',];
        this.translate.get(codes).subscribe(dict => {
            //預埋api
            let ReqBody = {
                roleName: this.form_page2.get('roleName')!.value,
                roleAlias: this.form_page2.get('roleAlias')!.value,
                funcCodeList: this.form_page2.get('funcCodeList')!.value
            } as AA0011Req;
            this.roleService.addTRole(ReqBody).subscribe(res => {
                if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                    this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.role']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
                    this.submitForm();
                    this.title = `${this.title.split('>')[0]}`;
                    this.pageNum = 1;
                }
            });
        });
    }

    update(_selected) {
        this.funcCodeList!.markAsTouched();
        this.selected = Object.keys(_selected).filter(key => key.length > 4 && _selected[key] == true);
        if (!this.selected || this.selected.length == 0) {
            this.selected = [];
            this.funcCodeList!.setErrors({ error: 'required' })
            // console.log(this.funcCodeList);
        }
        this.funcCodeList!.setValue(this.selected);

    }

    reture_to_page1() {
        this.title = `${this.title.split('>')[0]}`;
        this.pageNum = 1;
    }

    update_page4(menus) {
        this.selectedFuncList = Object.keys(menus).filter(menu => menu.length > 4 && menus[menu] == true).map(m => m);
        this.form_page4.get('funcCodeList')?.setValue(this.selectedFuncList);
    }

    async submitForm_page4() {
        const codes = ['message.update', 'role_info', 'message.success'];
        const dict = await this.tool.getDict(codes);
        let req = {
            roleId: this.data_page4!.roleID,
            roleName: this.data_page4!.roleName,
            newRoleAlias: this.newRoleAlias!.value,
            newFuncCodeList: this.selectedFuncList
        } as AA0013Req
        this.roleService.updateTRoleFunc(req).subscribe(res => {
            if (res && this.tool.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({ severity: 'success', summary: `${dict['message.update']} ${dict['role_info']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
                this.title = `${this.title.split('>')[0]}`;
                this.submitForm();
                this.pageNum = 1;
            }
        });
    }

    headerReturn(){
      this.reture_to_page1();
    }

    public get roleName() { return this.form_page2.get('roleName'); };
    public get roleAlias() { return this.form_page2.get('roleAlias'); };
    public get funcCodeList() { return this.form_page2.get('funcCodeList'); };

    public get newRoleAlias() { return this.form_page4.get('newRoleAlias'); };
}
