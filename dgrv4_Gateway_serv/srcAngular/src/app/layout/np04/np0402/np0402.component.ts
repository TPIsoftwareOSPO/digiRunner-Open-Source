import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DPB0064Items } from 'src/app/models/api/LayerService/dpb0064.interface';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { LayerService } from 'src/app/shared/services/api-layer.service';
import { MessageService } from 'primeng/api';
import { BaseComponent } from '../../base-component';
import { DPB0063Req } from 'src/app/models/api/LayerService/dpb0063.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';

@Component({
    selector: 'app-np0402',
    templateUrl: './np0402.component.html',
    styleUrls: ['./np0402.component.css']
})
export class Np0402Component extends BaseComponent implements OnInit {

    form!: FormGroup;
    roles: { label: string; value: string; }[] = [];
    dataMap: { API_APPLICATION: { 1: Array<DPB0064Items>; 2: Array<DPB0064Items>; }, API_ON_OFF: { 1: Array<DPB0064Items>; 2: Array<DPB0064Items>; }, CLIENT_REG: { 1: Array<DPB0064Items>; 2: Array<DPB0064Items>; }, OPEN_API_KEY: { 1: Array<DPB0064Items>; 2: Array<DPB0064Items>; } } = { API_APPLICATION: { 1: [], 2: [] }, API_ON_OFF: { 1: [], 2: [] }, CLIENT_REG: { 1: [], 2: [] }, OPEN_API_KEY: { 1: [], 2: [] } };
    layers: Array<{ label: string; value: string; }> = new Array<{ label: string; value: string; }>();

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private layer: LayerService,
        private message: MessageService,
        private role: RoleService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.role.queryRoleRoleList({ paging: 'N' }).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let set = new Set();
                let _roles:{label:string, value:string}[] = [];
                res.RespBody.roleRoleMappingList.map(role => {
                    // item.mappingList.map(role => {
                    if (!set.has(role.roleId)) {
                        set.add(role.roleId);
                        _roles.push({ label: role.roleAlias, value: role.roleId });
                    }
                    // });
                });
                this.roles = _roles;
            }
        });
        this.layers = [];
        this.form = this.fb.group({});
        this.layer.queryAllLayer_ignore1298().subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                Object.keys(res.RespBody.typeMap)
                .filter(fieldName =>{
                  //移除不使用的項目
                  const removeList = ['API_APPLICATION', 'API_ON_OFF', 'CLIENT_REG']
                  return removeList.indexOf(fieldName) == -1;
                })
                .map(key => {
                    this.layers.push({ label: res.RespBody.typeMap[key], value: key });
                });
                Object.keys(res.RespBody.dataMap).map(type => {
                    // if (Object.keys(res.RespBody.dataMap[type]).length == 0) {
                    this.form.addControl(`${type}_1`, new FormControl(''));
                    this.form.addControl(`${type}_2`, new FormControl(''));
                    // }
                    Object.keys(res.RespBody.dataMap[type]).map(layer => {
                        this.dataMap[type][layer] = res.RespBody.dataMap[type][layer];
                        let _roles:any[] = [];
                        for (let data of res.RespBody.dataMap[type][layer]) {
                            _roles.push(data.roleId);
                        }
                        // this.form.addControl(`${type}_${layer}`, new FormControl(''));
                        this.form.controls[`${type}_${layer}`].setValue(_roles);
                    });
                });
            }
        });
    }

    query() {
        this.layers = [];
        this.form = this.fb.group({});
        this.layer.queryAllLayer().subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                Object.keys(res.RespBody.typeMap)
                .filter(fieldName =>{
                  //移除不使用的項目
                  const removeList = ['API_APPLICATION', 'API_ON_OFF', 'CLIENT_REG']
                  return removeList.indexOf(fieldName) == -1;
                })
                .map(key => {
                    this.layers.push({ label: res.RespBody.typeMap[key], value: key });
                });
                Object.keys(res.RespBody.dataMap).map(type => {
                    // if (Object.keys(res.RespBody.dataMap[type]).length == 0) {
                    this.form.addControl(`${type}_1`, new FormControl(''));
                    this.form.addControl(`${type}_2`, new FormControl(''));
                    // }
                    Object.keys(res.RespBody.dataMap[type]).map(layer => {
                        this.dataMap[type][layer] = res.RespBody.dataMap[type][layer];
                        let _roles:any[] = [];
                        for (let data of res.RespBody.dataMap[type][layer]) {
                            _roles.push(data.roleId);
                        }
                        // this.form.addControl(`${type}_${layer}`, new FormControl(''));
                        this.form.controls[`${type}_${layer}`].setValue(_roles);
                    });
                });
            }
        });
    }

    save() {
        let dataMap:any = {
            API_APPLICATION: {
                // 0: [],
                // 1: [],
                // 2: []
            },
            API_ON_OFF: {},
            CLIENT_REG: {},
            OPEN_API_KEY: {}
        };
        let applicationLayer_1:any[] = [];
        let applicationLayer_2:any[] = [];
        let shelvesLayer_1:any[] = [];
        let shelvesLayer_2:any[] = [];
        let clientReqLayer_1:any[] = [];
        let clientReqLayer_2:any[] = [];
        let openApiKeyLayer_1:any[] = [];
        let openApiKeyLayer_2:any[] = [];
        if (this.API_APPLICATION_1!.value) {
            this.API_APPLICATION_1!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.API_APPLICATION[1].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                applicationLayer_1.push({ reviewType: 'API_APPLICATION', layer: 1, roleId: roleId, lv: _lv });
            });
            dataMap.API_APPLICATION[1] = applicationLayer_1;
        }
        if (this.API_APPLICATION_2!.value) {
            this.API_APPLICATION_2!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.API_APPLICATION[2].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                applicationLayer_2.push({ reviewType: 'API_APPLICATION', layer: 2, roleId: roleId, lv: _lv });
            });
            dataMap.API_APPLICATION[2] = applicationLayer_2;
        }
        if (this.API_ON_OFF_1!.value) {
            this.API_ON_OFF_1!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.API_ON_OFF[1].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                shelvesLayer_1.push({ reviewType: 'API_ON_OFF', layer: 1, roleId: roleId, lv: _lv });
            });
            dataMap.API_ON_OFF[1] = shelvesLayer_1;
        }
        if (this.API_ON_OFF_2!.value) {
            this.API_ON_OFF_2!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.API_ON_OFF[2].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                shelvesLayer_2.push({ reviewType: 'API_ON_OFF', layer: 2, roleId: roleId, lv: _lv });
            });
            dataMap.API_ON_OFF[2] = shelvesLayer_2;
        }
        if (this.CLIENT_REG_1!.value) {
            this.CLIENT_REG_1!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.CLIENT_REG[1].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                clientReqLayer_1.push({ reviewType: 'CLIENT_REG', layer: 1, roleId: roleId, lv: _lv });
            });
            dataMap.CLIENT_REG[1] = clientReqLayer_1;
        }
        if (this.CLIENT_REG_2!.value) {
            this.CLIENT_REG_2!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.CLIENT_REG[2].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                clientReqLayer_2.push({ reviewType: 'CLIENT_REG', layer: 2, roleId: roleId, lv: _lv });
            });
            dataMap.CLIENT_REG[2] = clientReqLayer_2;
        }
        if (this.OPEN_API_KEY_1!.value) {
            this.OPEN_API_KEY_1!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.OPEN_API_KEY[1].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                openApiKeyLayer_1.push({ reviewType: 'OPEN_API_KEY', layer: 1, roleId: roleId, lv: _lv });
            });
            dataMap.OPEN_API_KEY[1] = openApiKeyLayer_1;
        }
        if (this.OPEN_API_KEY_2!.value) {
            this.OPEN_API_KEY_2!.value.map(roleId => {
                let _lv:any = null;
                this.dataMap.OPEN_API_KEY[2].map(item => {
                    if (roleId == item.roleId) {
                        _lv = item.lv;
                    }
                });
                openApiKeyLayer_2.push({ reviewType: 'OPEN_API_KEY', layer: 2, roleId: roleId, lv: _lv });
            });
            dataMap.OPEN_API_KEY[2] = openApiKeyLayer_2;
        }
        if (Object.keys(dataMap.API_APPLICATION).length == 0) {
            delete dataMap.API_APPLICATION;
        }
        if (Object.keys(dataMap.API_ON_OFF).length == 0) {
            delete dataMap.API_ON_OFF;
        }
        if (Object.keys(dataMap.CLIENT_REG).length == 0) {
            delete dataMap.CLIENT_REG;
        }
        if (Object.keys(dataMap.OPEN_API_KEY).length == 0) {
            delete dataMap.OPEN_API_KEY;
        }
        // dataMap['API_APPLICATION'] = {
        //     0: applicationLayer_1,
        //     1: applicationLayer_2
        // }
        // dataMap['API_ON_OFF'] = {
        //     0: shelvesLayer_1,
        //     1: shelvesLayer_2
        // }
        // TODO for v3.6 dynamic req
        // Object.keys(this.form.value).map((key, index) => {
        //     let reviewType = key.substring(0, key.length - 2);
        //     let layer = key.substr(key.length - 1);
        //     let arr = [];
        //     let obj = {};
        //     this.form.get(key).value.map(roleId => {
        //         arr.push({ reviewType: reviewType, layer: parseInt(layer), roleId: roleId, lv: null });
        //     });
        //     obj[layer] = arr;
        //     console.log(obj)
        //     dataMap[reviewType] = Object.assign({}, obj);
        //     console.log(dataMap)
        // });
        // TODO for v3.6 dynamic req
        let ReqBody = {
            dataMap: dataMap
        } as DPB0063Req;
        // console.log('ReqBody :', ReqBody)
        this.layer.saveLayer(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.update', 'message.layer', 'message.setting', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dicts['message.update']} ${dicts['message.layer']} ${dicts['message.setting']}`, detail: `${dicts['message.update']} ${dicts['message.success']}!` });
                this.query();
            }
        });
    }

    reset() {
        this.form.reset();
        this.query();
    }

    public get API_APPLICATION_1() { return this.form.get("API_APPLICATION_1"); }
    public get API_APPLICATION_2() { return this.form.get("API_APPLICATION_2"); }
    public get API_ON_OFF_1() { return this.form.get("API_ON_OFF_1"); }
    public get API_ON_OFF_2() { return this.form.get("API_ON_OFF_2"); }
    public get CLIENT_REG_1() { return this.form.get("CLIENT_REG_1"); }
    public get CLIENT_REG_2() { return this.form.get("CLIENT_REG_2"); }
    public get OPEN_API_KEY_1() { return this.form.get("OPEN_API_KEY_1"); }
    public get OPEN_API_KEY_2() { return this.form.get("OPEN_API_KEY_2"); }

}
